package com.example.ttp_serve.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseRequestDTO {
    private Long coachId;
    private Long studentId;
    private Long courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}