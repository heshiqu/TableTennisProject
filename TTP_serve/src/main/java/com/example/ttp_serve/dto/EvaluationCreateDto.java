package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.EvaluationType;
import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class EvaluationCreateDto {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "评价者ID不能为空")
    private Long fromUserId;

    @NotNull(message = "被评价者ID不能为空")
    private Long toUserId;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    @Min(value = 1, message = "评分必须大于0")
    @Max(value = 5, message = "评分必须小于等于5")
    private Integer rating;

    @NotNull(message = "评价类型不能为空")
    private EvaluationType type;
}