package com.shuyu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        System.out.println("準備開始機械化選股作業...");
        System.out.println();
        System.out.println("═".repeat(50));
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
}