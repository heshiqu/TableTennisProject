package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.CoachStudentRelation;
import com.example.ttp_serve.enums.RelationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachStudentRelationRepository extends JpaRepository<CoachStudentRelation, Long> {

    // 根据教练ID和学员ID查找关系
    Optional<CoachStudentRelation> findByCoachIdAndStudentId(Long coachId, Long studentId);

    // 根据教练ID查找关系
    List<CoachStudentRelation> findByCoachId(Long coachId);

    // 根据学员ID查找关系
    List<CoachStudentRelation> findByStudentId(Long studentId);

    // 根据状态查找关系
    List<CoachStudentRelation> findByStatus(RelationStatus status);

    // 根据教练ID和状态查找关系
    List<CoachStudentRelation> findByCoachIdAndStatus(Long coachId, RelationStatus status);

    // 根据学员ID和状态查找关系
    List<CoachStudentRelation> findByStudentIdAndStatus(Long studentId, RelationStatus status);

    // 统计教练的学员数量
    Long countByCoachIdAndStatus(Long coachId, RelationStatus status);

    // 统计学员的教练数量
    Long countByStudentIdAndStatus(Long studentId, RelationStatus status);

    // 检查是否存在特定状态的关系
    boolean existsByCoachIdAndStudentIdAndStatus(Long coachId, Long studentId, RelationStatus status);
}