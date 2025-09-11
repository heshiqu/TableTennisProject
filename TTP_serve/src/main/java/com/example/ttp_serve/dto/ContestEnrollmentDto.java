package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.EnrollmentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContestEnrollmentDto {

    private Long id;
    private Long contestId;
    private LocalDate contestDate;
    private ContestGroup contestGroup;
    private Long studentId;
    private String studentName;
    private ContestGroup groupType;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
}