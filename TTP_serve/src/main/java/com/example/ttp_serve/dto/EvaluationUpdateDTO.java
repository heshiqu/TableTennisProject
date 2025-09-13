package com.example.ttp_serve.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 评价更新数据传输对象
 * 用于更新评价时的数据传输
 */
@Data
public class EvaluationUpdateDTO {

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 1000, message = "评价内容长度不能超过1000个字符")
    private String content;

    @Min(value = 1, message = "评分必须大于等于1")
    @Max(value = 5, message = "评分必须小于等于5")
    private Integer rating;
}