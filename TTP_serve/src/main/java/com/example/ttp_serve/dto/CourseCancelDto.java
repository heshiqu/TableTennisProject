package com.example.ttp_serve.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CourseCancelDto {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "取消原因不能为空")
    private String reason;
}