package com.shuyu.model;

import java.time.LocalDate;

/**
 * 股票 K 線資料 Record
 * 
 * 使用 Java 21 的 Record 特性，自動產生：
 * - Constructor
 * - Getter methods
 * - equals(), hashCode(), toString()
 * 
 * @param code                  股票代號（例如：2330）
 * @param name                  股票名稱（例如：台積電）
 * @param date                  交易日期
 * @param open                  開盤價
 * @param high                  最高價
 * @param low                   最低價
 * @param close                 收盤價
 * @param volume                成交股數
 * @param investmentTrustNetBuy 投信買賣超張數
 */
public record StockKLine(
        String code,
        String name,
        LocalDate date,
        double open,
        double high,
        double low,
        double close,
        long volume,
        long investmentTrustNetBuy) {
    /**
     * 計算當日漲跌幅（百分比）
     * 
     * @return 漲跌幅（例如：3.5 代表上漲 3.5%）
     */
    public double getPriceChangePercent() {
        if (open == 0)
            return 0;
        return ((close - open) / open) * 100;
    }

    /**
     * 判斷是否為紅 K（收盤價 > 開盤價）
     * 
     * @return true 如果是紅 K
     */
    public boolean isRedCandle() {
        return close > open;
    }

    /**
     * 判斷是否為黑 K（收盤價 < 開盤價）
     * 
     * @return true 如果是黑 K
     */
    public boolean isBlackCandle() {
        return close < open;
    }
}