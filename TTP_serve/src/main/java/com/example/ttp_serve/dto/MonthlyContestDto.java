package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.ContestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MonthlyContestDto {

    private Long id;
    private Long campusId;
    private String campusName;
    private LocalDate contestDate;
    private ContestGroup groupType;
    private ContestStatus status;
    private LocalDateTime createdAt;

    // 统计信息
    private Long enrollmentCount;
    private Long scheduleCount;
}