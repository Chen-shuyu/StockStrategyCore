package com.shuyu.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuyu.collector.dto.TwseApiResponse;
import com.shuyu.model.StockKLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 台灣證交所資料採集器
 * 
 * 使用證交所官方 API 抓取股票日成交資訊
 * API 文件: https://www.twse.com.tw/
 */
public class TwseStockDataCollector implements StockDataCollector {

    private static final String BASE_URL = "https://www.twse.com.tw/exchangeReport/STOCK_DAY";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyy/MM/dd");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TwseStockDataCollector() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<StockKLine> fetchHistoricalData(
            String stockCode,
            LocalDate startDate,
            LocalDate endDate) {
        System.out.println("正在抓取 " + stockCode + " 的資料...");

        List<StockKLine> allData = new ArrayList<>();

        // 計算需要抓取的月份
        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        YearMonth current = start;

        while (!current.isAfter(end)) {
            try {
                // 抓取單月資料
                List<StockKLine> monthData = fetchMonthData(stockCode, current);

                // 過濾日期範圍
                monthData.stream()
                        .filter(k -> !k.date().isBefore(startDate) && !k.date().isAfter(endDate))
                        .forEach(allData::add);

                // 禮貌性等待，避免被擋
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("執行緒被中斷");
                break;
            } catch (Exception e) {
                System.err.println("抓取 " + stockCode + " " + current + " 資料失敗: " + e.getMessage());
            }

            current = current.plusMonths(1);
        }

        System.out.println("✅ " + stockCode + " 資料抓取完成，共 " + allData.size() + " 筆");
        return allData;
    }

    /**
     * 抓取單月資料
     */
    private List<StockKLine> fetchMonthData(String stockCode, YearMonth yearMonth)
            throws IOException, InterruptedException {

        // 建立 URL（日期格式：YYYYMM01）
        String dateStr = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")) + "01";
        String url = String.format("%s?response=json&date=%s&stockNo=%s",
                BASE_URL, dateStr, stockCode);

        // 建立 HTTP 請求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET()
                .build();

        // 發送請求
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        // 檢查 HTTP 狀態碼
        if (response.statusCode() != 200) {
            throw new IOException("HTTP 錯誤: " + response.statusCode());
        }

        // 解析 JSON
        TwseApiResponse apiResponse = objectMapper.readValue(
                response.body(),
                TwseApiResponse.class);

        // 檢查 API 回傳狀態
        if (!apiResponse.isSuccess()) {
            System.out.println("⚠️ " + stockCode + " " + yearMonth + " 無資料");
            return List.of();
        }

        // 轉換成 StockKLine
        return parseApiResponse(stockCode, apiResponse);
    }

    /**
     * 將 API 回應轉換成 StockKLine 物件
     */
    private List<StockKLine> parseApiResponse(String stockCode, TwseApiResponse response) {
        List<StockKLine> result = new ArrayList<>();

        // 從 title 取得股票名稱（格式：113年01月 2330 台積電 各日成交資訊）
        String stockName = extractStockName(response.title());

        for (List<String> row : response.data()) {
            try {
                // 欄位順序：日期, 成交股數, 成交金額, 開盤價, 最高價, 最低價, 收盤價, 漲跌價差, 成交筆數
                LocalDate date = parseTaiwanDate(row.get(0));
                long volume = parseNumber(row.get(1)).longValue();
                double open = parseNumber(row.get(3)).doubleValue();
                double high = parseNumber(row.get(4)).doubleValue();
                double low = parseNumber(row.get(5)).doubleValue();
                double close = parseNumber(row.get(6)).doubleValue();

                StockKLine kline = new StockKLine(
                        stockCode,
                        stockName,
                        date,
                        open,
                        high,
                        low,
                        close,
                        volume,
                        0L // 投信買賣超需要另外抓取
                );

                result.add(kline);

            } catch (Exception e) {
                System.err.println("解析資料失敗: " + row + " - " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 從標題中提取股票名稱
     * 例如："113年01月 2330 台積電 各日成交資訊" -> "台積電"
     */
    private String extractStockName(String title) {
        String[] parts = title.split(" ");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "未知";
    }

    /**
     * 解析台灣日期格式 (民國年)
     * 例如："113/01/02" -> LocalDate(2024, 1, 2)
     */
    private LocalDate parseTaiwanDate(String dateStr) {
        String[] parts = dateStr.split("/");
        int rocYear = Integer.parseInt(parts[0]); // 民國年
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        int adYear = rocYear + 1911; // 轉換成西元年
        return LocalDate.of(adYear, month, day);
    }

    /**
     * 解析數字（去除千分位逗號）
     * 例如："50,123,456" -> 50123456
     */
    private Number parseNumber(String numStr) {
        String cleaned = numStr.replace(",", "").trim();

        // 處理特殊情況
        if (cleaned.isEmpty() || "--".equals(cleaned)) {
            return 0;
        }

        // 判斷是整數還是小數
        if (cleaned.contains(".")) {
            return Double.parseDouble(cleaned);
        } else {
            return Long.parseLong(cleaned);
        }
    }
}