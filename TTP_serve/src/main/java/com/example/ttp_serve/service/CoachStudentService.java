package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.CoachStudentRelation;
import com.example.ttp_serve.enums.RelationStatus;

import java.util.List;

public interface CoachStudentService {

    // 申请建立教练学员关系
    CoachStudentRelation applyRelation(Long coachId, Long studentId);

    // 批准关系申请
    CoachStudentRelation approveRelation(Long relationId, Long coachId);

    // 拒绝关系申请
    CoachStudentRelation rejectRelation(Long relationId, Long coachId, String reason);

    // 获取关系详情
    CoachStudentRelation getRelation(Long relationId);

    // 获取教练的所有关系
    List<CoachStudentRelation> getRelationsByCoach(Long coachId);

    // 获取学员的所有关系
    List<CoachStudentRelation> getRelationsByStudent(Long studentId);

    // 获取特定状态的关系
    List<CoachStudentRelation> getRelationsByStatus(RelationStatus status);

    // 获取教练的特定状态的关系
    List<CoachStudentRelation> getCoachRelationsByStatus(Long coachId, RelationStatus status);

    // 获取学员的特定状态的关系
    List<CoachStudentRelation> getStudentRelationsByStatus(Long studentId, RelationStatus status);

    // 检查是否已存在关系
    boolean existsRelation(Long coachId, Long studentId);

    // 删除关系
    void deleteRelation(Long relationId);

    // 统计教练的学员数量
    Long countStudentsByCoach(Long coachId);

    // 统计学员的教练数量
    Long countCoachesByStudent(Long studentId);

    // 检查教练是否已满员
    boolean isCoachFull(Long coachId);

    // 检查学员是否已达到最大教练数
    boolean hasMaxCoaches(Long studentId);
}