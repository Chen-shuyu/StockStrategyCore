package com.shuyu.collector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 證交所 API 回應的資料結構
 */
public record TwseApiResponse(
        @JsonProperty("stat") String status,
        @JsonProperty("date") String date,
        @JsonProperty("title") String title,
        @JsonProperty("fields") List<String> fields,
        @JsonProperty("data") List<List<String>> data,
        @JsonProperty("notes") List<String> notes,
        @JsonProperty("total") String total) {
    /**
     * 檢查 API 是否回傳成功
     */
    public boolean isSuccess() {
        return "OK".equals(status);
    }
}