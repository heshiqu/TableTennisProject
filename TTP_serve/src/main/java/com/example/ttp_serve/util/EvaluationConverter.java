package com.example.ttp_serve.util;

import com.example.ttp_serve.dto.EvaluationCreateDto;
import com.example.ttp_serve.dto.EvaluationDto;
import com.example.ttp_serve.entity.Course;
import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.entity.User;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价实体与DTO转换工具类
 */
public class EvaluationConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 将Evaluation实体转换为EvaluationDto
     */
    public static EvaluationDto toDto(Evaluation evaluation) {
        if (evaluation == null) {
            return null;
        }

        EvaluationDto dto = new EvaluationDto();
        dto.setId(evaluation.getId());
        dto.setContent(evaluation.getContent());
        dto.setRating(evaluation.getRating());
        dto.setType(evaluation.getType());
        dto.setCreatedAt(evaluation.getCreatedAt());
        
        if (evaluation.getCreatedAt() != null) {
            dto.setFormattedDate(evaluation.getCreatedAt().format(DATE_FORMATTER));
        }

        // 设置课程相关信息
        if (evaluation.getCourse() != null) {
            Course course = evaluation.getCourse();
            dto.setCourseId(course.getId());
            // 这里可以根据需要设置课程名称等其他信息
        }

        // 设置评价者信息
        if (evaluation.getFromUser() != null) {
            User fromUser = evaluation.getFromUser();
            dto.setFromUserId(fromUser.getId());
            dto.setFromUserName(fromUser.getRealName() != null ? fromUser.getRealName() : fromUser.getUsername());
            dto.setFromUserAvatar(fromUser.getAvatar());
        }

        // 设置被评价者信息
        if (evaluation.getToUser() != null) {
            User toUser = evaluation.getToUser();
            dto.setToUserId(toUser.getId());
            dto.setToUserName(toUser.getRealName() != null ? toUser.getRealName() : toUser.getUsername());
            dto.setToUserAvatar(toUser.getAvatar());
        }

        return dto;
    }

    /**
     * 将EvaluationCreateDto转换为Evaluation实体
     */
    public static Evaluation toEntity(EvaluationCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setContent(createDto.getContent());
        evaluation.setRating(createDto.getRating());
        evaluation.setType(createDto.getType());

        // 设置关联实体（这里只设置ID，实际关联在service层处理）
        Course course = new Course();
        course.setId(createDto.getCourseId());
        evaluation.setCourse(course);

        User fromUser = new User();
        fromUser.setId(createDto.getFromUserId());
        evaluation.setFromUser(fromUser);

        User toUser = new User();
        toUser.setId(createDto.getToUserId());
        evaluation.setToUser(toUser);

        return evaluation;
    }

    /**
     * 将Evaluation实体列表转换为EvaluationDto列表
     */
    public static List<EvaluationDto> toDtoList(List<Evaluation> evaluations) {
        if (evaluations == null) {
            return null;
        }
        return evaluations.stream()
                .map(EvaluationConverter::toDto)
                .collect(Collectors.toList());
    }
}