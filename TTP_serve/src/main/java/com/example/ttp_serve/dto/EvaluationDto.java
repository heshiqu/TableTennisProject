package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.EvaluationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationDto {

    private Long id;
    private Long courseId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private String content;
    private Integer rating;
    private EvaluationType type;
    private LocalDateTime createdAt;
}