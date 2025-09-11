package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.enums.EvaluationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EvaluationService {

    // 创建评价
    Evaluation createEvaluation(Evaluation evaluation);

    // 更新评价
    Evaluation updateEvaluation(Long id, Evaluation evaluation);

    // 删除评价
    void deleteEvaluation(Long id);

    // 获取评价详情
    Evaluation getEvaluation(Long id);

    // 根据课程ID获取评价
    List<Evaluation> getEvaluationsByCourse(Long courseId);

    // 根据评价者ID获取评价
    List<Evaluation> getEvaluationsByFromUser(Long fromUserId);

    // 根据被评价者ID获取评价
    List<Evaluation> getEvaluationsByToUser(Long toUserId);

    // 根据类型获取评价
    List<Evaluation> getEvaluationsByType(EvaluationType type);

    // 根据评价者和类型获取评价
    List<Evaluation> getEvaluationsByFromUserAndType(Long fromUserId, EvaluationType type);

    // 根据被评价者和类型获取评价
    List<Evaluation> getEvaluationsByToUserAndType(Long toUserId, EvaluationType type);

    // 根据课程ID和类型获取评价
    List<Evaluation> getEvaluationsByCourseAndType(Long courseId, EvaluationType type);

    // 分页获取评价
    Page<Evaluation> getEvaluations(Pageable pageable);

    // 计算被评价者的平均评分
    Double calculateAverageRating(Long userId);

    // 统计被评价者的评价数量
    Long countEvaluationsByToUser(Long userId);

    // 检查是否已存在评价
    boolean existsEvaluationByCourseAndFromUser(Long courseId, Long fromUserId, EvaluationType type);

    // 获取需要评价的课程
    List<Long> getCoursesNeedEvaluation(Long userId);
}