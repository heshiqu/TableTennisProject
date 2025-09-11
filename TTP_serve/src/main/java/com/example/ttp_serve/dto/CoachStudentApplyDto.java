package com.example.ttp_serve.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class CoachStudentApplyDto {

    @NotNull(message = "教练ID不能为空")
    private Long coachId;

    @NotNull(message = "学员ID不能为空")
    private Long studentId;
}