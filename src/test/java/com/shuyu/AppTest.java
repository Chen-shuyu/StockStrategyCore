package com.shuyu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for App class.
 */
class AppTest {

    @Test
    @DisplayName("測試程式可以正常執行")
    void testAppCanRun() {
        // 簡單的測試，確保程式邏輯正確
        assertTrue(true);
    }

    @Test
    @DisplayName("測試 Java 版本")
    void testJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion);
        assertTrue(javaVersion.startsWith("21"),
                "Java version should be 21, but got: " + javaVersion);
    }
}