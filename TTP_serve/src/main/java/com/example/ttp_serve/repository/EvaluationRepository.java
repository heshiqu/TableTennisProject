package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.enums.EvaluationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // 根据课程ID查找评价
    List<Evaluation> findByCourseId(Long courseId);

    // 根据评价者ID查找评价
    List<Evaluation> findByFromUserId(Long fromUserId);

    // 根据被评价者ID查找评价
    List<Evaluation> findByToUserId(Long toUserId);

    // 根据类型查找评价
    List<Evaluation> findByType(EvaluationType type);

    // 根据评价者和类型查找评价
    List<Evaluation> findByFromUserIdAndType(Long fromUserId, EvaluationType type);

    // 根据被评价者和类型查找评价
    List<Evaluation> findByToUserIdAndType(Long toUserId, EvaluationType type);

    // 根据课程ID和类型查找评价
    List<Evaluation> findByCourseIdAndType(Long courseId, EvaluationType type);

    // 计算被评价者的平均评分
    @Query("SELECT COALESCE(AVG(e.rating), 0) FROM Evaluation e WHERE e.toUser.id = :userId AND e.rating IS NOT NULL")
    Double calculateAverageRating(@Param("userId") Long userId);

    // 计算被评价者的评价数量
    Long countByToUserId(Long userId);

    // 分页查询评价
    Page<Evaluation> findAll(Pageable pageable);

    boolean existsByCourseIdAndFromUserIdAndType(Long courseId, Long fromUserId, EvaluationType type);
}