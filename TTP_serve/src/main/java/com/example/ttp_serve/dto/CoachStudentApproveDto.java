package com.example.ttp_serve.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class CoachStudentApproveDto {

    @NotNull(message = "关系ID不能为空")
    private Long relationId;

    private String reason;
}