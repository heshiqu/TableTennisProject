package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CourtStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 球台数据传输对象
 * 用于返回球台信息
 */
@Data
public class CourtDTO {
    private Long id;
    private Long campusId;
    private String campusName;
    private String courtNumber;
    private CourtStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}