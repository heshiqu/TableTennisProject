package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.EvaluationType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价数据传输对象
 * 用于返回评价信息给前端
 */
@Data
public class EvaluationDto {

    private Long id;
    private Long courseId;
    private String courseName; // 课程名称
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar; // 评价者头像
    private Long toUserId;
    private String toUserName;
    private String toUserAvatar; // 被评价者头像
    private String content;
    private Integer rating;
    private EvaluationType type;
    private LocalDateTime createdAt;
    private String formattedDate; // 格式化后的日期字符串
}