// CoachStudentServiceImpl.java
package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.Coach;
import com.example.ttp_serve.entity.CoachStudentRelation;
import com.example.ttp_serve.entity.Student;
import com.example.ttp_serve.enums.RelationStatus;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.CoachRepository;
import com.example.ttp_serve.repository.CoachStudentRelationRepository;
import com.example.ttp_serve.repository.StudentRepository;
import com.example.ttp_serve.service.CoachStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachStudentServiceImpl implements CoachStudentService {

    private final CoachStudentRelationRepository relationRepository;
    private final CoachRepository coachRepository;
    private final StudentRepository studentRepository;

    // 学员最多选择的教练数量
    private static final int MAX_COACHES_PER_STUDENT = 2;

    @Override
    @Transactional
    public CoachStudentRelation applyRelation(Long coachId, Long studentId) {
        // 检查教练是否存在
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("教练ID '" + coachId + "' 不存在"));

        // 检查学员是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学员ID '" + studentId + "' 不存在"));

        // 检查是否已存在关系
        if (relationRepository.existsByCoachIdAndStudentIdAndStatus(coachId, studentId, RelationStatus.PENDING)) {
            throw new BusinessException("已向该教练提交过申请");
        }

        // 检查教练是否已满员
        if (isCoachFull(coachId)) {
            throw new BusinessException("该教练学员已满");
        }

        // 检查学员是否已达到最大教练数
        if (hasMaxCoaches(studentId)) {
            throw new BusinessException("每位学员最多只能选择两位教练");
        }

        // 创建关系申请
        CoachStudentRelation relation = new CoachStudentRelation();
        relation.setCoach(coach);
        relation.setStudent(student);
        relation.setStatus(RelationStatus.PENDING);
        relation.setApplyTime(LocalDateTime.now());

        return relationRepository.save(relation);
    }

    @Override
    @Transactional
    public CoachStudentRelation approveRelation(Long relationId, Long coachId) {
        CoachStudentRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("关系申请ID '" + relationId + "' 不存在"));

        // 检查操作权限
        if (!relation.getCoach().getId().equals(coachId)) {
            throw new BusinessException("无权操作此申请");
        }

        // 检查申请状态
        if (relation.getStatus() != RelationStatus.PENDING) {
            throw new BusinessException("申请状态不正确");
        }

        // 检查教练是否已满员
        if (isCoachFull(coachId)) {
            throw new BusinessException("该教练学员已满，无法批准申请");
        }

        // 更新关系状态
        relation.setStatus(RelationStatus.APPROVED);
        relation.setApproveTime(LocalDateTime.now());

        // 更新教练当前学员数
        Coach coach = relation.getCoach();
        coach.setCurrentStudents(coach.getCurrentStudents() + 1);
        coachRepository.save(coach);

        return relationRepository.save(relation);
    }

    @Override
    @Transactional
    public CoachStudentRelation rejectRelation(Long relationId, Long coachId) {
        CoachStudentRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("关系申请ID '" + relationId + "' 不存在"));

        // 检查操作权限
        if (!relation.getCoach().getId().equals(coachId)) {
            throw new BusinessException("无权操作此申请");
        }

        // 检查申请状态
        if (relation.getStatus() != RelationStatus.PENDING) {
            throw new BusinessException("申请状态不正确");
        }

        // 更新关系状态
        relation.setStatus(RelationStatus.REJECTED);
        relation.setApproveTime(LocalDateTime.now());

        return relationRepository.save(relation);
    }

    @Override
    public CoachStudentRelation getRelation(Long relationId) {
        return relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("关系申请ID '" + relationId + "' 不存在"));
    }

    @Override
    public List<CoachStudentRelation> getRelationsByCoach(Long coachId) {
        // 检查教练是否存在
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return relationRepository.findByCoachId(coachId);
    }

    @Override
    public List<CoachStudentRelation> getRelationsByStudent(Long studentId) {
        // 检查学员是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return relationRepository.findByStudentId(studentId);
    }

    @Override
    public List<CoachStudentRelation> getRelationsByStatus(RelationStatus status) {
        return relationRepository.findByStatus(status);
    }

    @Override
    public List<CoachStudentRelation> getCoachRelationsByStatus(Long coachId, RelationStatus status) {
        // 检查教练是否存在
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return relationRepository.findByCoachIdAndStatus(coachId, status);
    }

    @Override
    public List<CoachStudentRelation> getStudentRelationsByStatus(Long studentId, RelationStatus status) {
        // 检查学员是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return relationRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public boolean existsRelation(Long coachId, Long studentId) {
        return relationRepository.existsByCoachIdAndStudentIdAndStatus(coachId, studentId, RelationStatus.PENDING)
                || relationRepository.existsByCoachIdAndStudentIdAndStatus(coachId, studentId, RelationStatus.APPROVED)
                || relationRepository.existsByCoachIdAndStudentIdAndStatus(coachId, studentId, RelationStatus.REJECTED);
    }

    @Override
    @Transactional
    public void deleteRelation(Long relationId) {
        CoachStudentRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("关系申请ID '" + relationId + "' 不存在"));

        // 如果是已批准的关系，需要更新教练当前学员数
        if (relation.getStatus() == RelationStatus.APPROVED) {
            Coach coach = relation.getCoach();
            coach.setCurrentStudents(coach.getCurrentStudents() - 1);
            coachRepository.save(coach);
        }

        relationRepository.delete(relation);
    }

    @Override
    public Long countStudentsByCoach(Long coachId) {
        // 检查教练是否存在
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return relationRepository.countByCoachIdAndStatus(coachId, RelationStatus.APPROVED);
    }

    @Override
    public Long countCoachesByStudent(Long studentId) {
        // 检查学员是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return relationRepository.countByStudentIdAndStatus(studentId, RelationStatus.APPROVED);
    }

    @Override
    public boolean isCoachFull(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("教练ID '" + coachId + "' 不存在"));

        Long currentStudents = relationRepository.countByCoachIdAndStatus(coachId, RelationStatus.APPROVED);
        return currentStudents >= coach.getMaxStudents();
    }

    @Override
    public boolean hasMaxCoaches(Long studentId) {
        Long currentCoaches = relationRepository.countByStudentIdAndStatus(studentId, RelationStatus.APPROVED);
        return currentCoaches >= MAX_COACHES_PER_STUDENT;
    }
}