package com.shuyu.collector;

import com.shuyu.model.StockKLine;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * 股票資料採集器介面
 */
public interface StockDataCollector {

    /**
     * 抓取單一股票的歷史 K 線資料
     */
    List<StockKLine> fetchHistoricalData(
            String stockCode,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 抓取多支股票的歷史資料（使用 Virtual Threads 並行處理）
     */
    default Map<String, List<StockKLine>> fetchMultipleStocks(
            List<String> stockCodes,
            LocalDate startDate,
            LocalDate endDate) {
        var results = new ConcurrentHashMap<String, List<StockKLine>>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = stockCodes.stream()
                    .map(code -> executor.submit(() -> {
                        var data = fetchHistoricalData(code, startDate, endDate);
                        results.put(code, data);
                        return data;
                    }))
                    .toList();

            // 等待所有任務完成
            for (var future : futures) {
                future.get();
            }
        } catch (Exception e) {
            System.err.println("抓取資料時發生錯誤: " + e.getMessage());
        }

        return results;
    }
}