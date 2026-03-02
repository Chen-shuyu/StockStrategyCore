package com.shuyu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.shuyu.collector.TwseStockDataCollector;
import com.shuyu.model.StockKLine;

/**
 * GOGOGO 股市戰情室 - 主程式入口
 * 
 * @author ShuYu
 * @version 1.0
 * @since 2026-02-12
 */
public class App {

    private static final String VERSION = "1.0-SNAPSHOT";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        printBanner();

        System.out.println("啟動時間: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("Java 版本: " + System.getProperty("java.version"));
        System.out.println("系統狀態: 就緒");
        System.out.println();

        // 顯示 Virtual Threads 支援狀態
        checkVirtualThreadsSupport();

        System.out.println("系統初始化成功！");
        System.out.println();
        System.out.println("═".repeat(50));
        // ⭐ 新增：測試 StockKLine Record
        // testStockKLineRecord();

        testRealCrawler();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                                                  ║");
        System.out.println("║             GOGOGO 股市戰情室                    ║");
        System.out.println("║        Stock Strategy Core Engine                ║");
        System.out.println("║                                                  ║");
        System.out.println("║        Version: " + String.format("%-33s", VERSION) + "║");
        System.out.println("║        Powered by Java 21 Virtual Threads        ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void checkVirtualThreadsSupport() {
        try {
            // Java 21 Virtual Thread 測試
            Thread virtualThread = Thread.ofVirtual().start(() -> {
                // Empty task
            });
            virtualThread.join();
            System.out.println("Virtual Threads:  支援");
        } catch (Exception e) {
            System.out.println("Virtual Threads:  不支援");
        }
    }

    /**
     * 測試 StockKLine Record 是否正常運作
     */
    private static void testStockKLineRecord() {
        System.out.println("═".repeat(50));
        System.out.println("測試 StockKLine 資料模型");
        System.out.println("═".repeat(50));

        // 建立測試資料：台積電的模擬 K 線
        StockKLine tsmc = new StockKLine(
                "2330", // 股票代號
                "台積電", // 股票名稱
                LocalDate.now(), // 今天日期
                580.0, // 開盤價
                595.0, // 最高價
                578.0, // 最低價
                590.0, // 收盤價
                50_000_000L, // 成交量 5000 萬股
                1500L // 投信買超 1500 張
        );

        // 輸出測試結果
        System.out.println("股票代號: " + tsmc.code());
        System.out.println("股票名稱: " + tsmc.name());
        System.out.println("交易日期: " + tsmc.date());
        System.out.println("開盤價: " + tsmc.open());
        System.out.println("收盤價: " + tsmc.close());
        System.out.println("漲跌幅: " + String.format("%.2f%%", tsmc.getPriceChangePercent()));
        System.out.println("K線類型: " + (tsmc.isRedCandle() ? "紅K" : "黑K"));
        System.out.println();

        System.out.println("StockKLine Record 測試成功！");
        System.out.println("準備開始機械化選股作業...");
        System.out.println();
    }

    private static void testRealCrawler() {
        System.out.println("═".repeat(50));
        System.out.println("🕷️ 測試證交所爬蟲");
        System.out.println("═".repeat(50));

        var collector = new TwseStockDataCollector();

        // 測試抓取台積電最近 30 天資料
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(40);

        System.out.println("抓取區間: " + startDate + " ~ " + endDate);
        System.out.println();

        long startTime = System.currentTimeMillis();

        List<StockKLine> data = collector.fetchHistoricalData("2330", startDate, endDate);

        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("抓取完成！");
        System.out.println("耗時: " + (endTime - startTime) + " ms");
        System.out.println("資料筆數: " + data.size());

        // 顯示前 5 筆資料
        if (!data.isEmpty()) {
            System.out.println();
            System.out.println("前 5 筆資料預覽：");
            data.stream()
                    .limit(5)
                    .forEach(k -> System.out.printf(
                            "%s | 開:%7.2f 高:%7.2f 低:%7.2f 收:%7.2f | %s%n",
                            k.date(), k.open(), k.high(), k.low(), k.close(),
                            k.isRedCandle() ? "紅" : "綠"));
        }
    }
}