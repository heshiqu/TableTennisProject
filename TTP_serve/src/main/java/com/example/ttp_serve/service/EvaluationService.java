package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.EvaluationCreateDto;
import com.example.ttp_serve.dto.EvaluationDto;
import com.example.ttp_serve.dto.EvaluationUpdateDTO;
import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.enums.EvaluationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EvaluationService {

    // 创建评价（使用DTO）
    EvaluationDto createEvaluation(EvaluationCreateDto evaluationCreateDto);

    // 更新评价（使用DTO）
    EvaluationDto updateEvaluation(Long id, EvaluationUpdateDTO evaluationUpdateDto);

    // 删除评价
    void deleteEvaluation(Long id);

    // 获取评价详情（使用DTO）
    EvaluationDto getEvaluation(Long id);

    // 根据课程ID获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByCourse(Long courseId);

    // 根据评价者ID获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByFromUser(Long fromUserId);

    // 根据被评价者ID获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByToUser(Long toUserId);

    // 根据类型获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByType(EvaluationType type);

    // 根据评价者和类型获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByFromUserAndType(Long fromUserId, EvaluationType type);

    // 根据被评价者和类型获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByToUserAndType(Long toUserId, EvaluationType type);

    // 根据课程ID和类型获取评价（使用DTO）
    List<EvaluationDto> getEvaluationsByCourseAndType(Long courseId, EvaluationType type);

    // 分页获取评价（使用DTO）
    Page<EvaluationDto> getEvaluations(Pageable pageable);

    // 计算被评价者的平均评分
    Double calculateAverageRating(Long userId);

    // 统计被评价者的评价数量
    Long countEvaluationsByToUser(Long userId);

    // 检查是否已存在评价
    boolean existsEvaluationByCourseAndFromUser(Long courseId, Long fromUserId, EvaluationType type);

    // 获取需要评价的课程
    List<Long> getCoursesNeedEvaluation(Long userId);

    // 以下方法保留用于内部使用或兼容旧代码
    @Deprecated
    Evaluation createEvaluation(Evaluation evaluation);

    @Deprecated
    Evaluation updateEvaluation(Long id, Evaluation evaluation);

    @Deprecated
    Evaluation getEvaluationEntity(Long id);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByCourse(Long courseId);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByFromUser(Long fromUserId);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByToUser(Long toUserId);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByType(EvaluationType type);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByFromUserAndType(Long fromUserId, EvaluationType type);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByToUserAndType(Long toUserId, EvaluationType type);

    @Deprecated
    List<Evaluation> getEvaluationsEntityByCourseAndType(Long courseId, EvaluationType type);

    @Deprecated
    Page<Evaluation> getEvaluationsEntity(Pageable pageable);
}