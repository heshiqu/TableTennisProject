package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CourseStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private Long coachId;
    private String coachName;
    private Long studentId;
    private String studentName;
    private Long courtId;
    private String courtNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal duration;
    private BigDecimal fee;
    private CourseStatus status;
    private String cancelReason;
    private Long cancelByUserId;
    private String cancelByUserName;
    private LocalDateTime cancelTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}