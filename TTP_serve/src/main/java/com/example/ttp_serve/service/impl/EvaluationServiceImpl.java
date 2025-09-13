package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.EvaluationCreateDto;
import com.example.ttp_serve.dto.EvaluationDto;
import com.example.ttp_serve.dto.EvaluationUpdateDTO;
import com.example.ttp_serve.entity.Course;
import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.CourseStatus;
import com.example.ttp_serve.enums.EvaluationType;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.CourseRepository;
import com.example.ttp_serve.repository.EvaluationRepository;
import com.example.ttp_serve.repository.UserRepository;
import com.example.ttp_serve.service.EvaluationService;
import com.example.ttp_serve.util.EvaluationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Evaluation createEvaluation(Evaluation evaluation) {
        // 检查课程是否存在
        Course course = courseRepository.findById(evaluation.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + evaluation.getCourse().getId() + "' 不存在"));

        // 检查评价者是否存在
        User fromUser = userRepository.findById(evaluation.getFromUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("评价者ID '" + evaluation.getFromUser().getId() + "' 不存在"));

        // 检查被评价者是否存在
        User toUser = userRepository.findById(evaluation.getToUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("被评价者ID '" + evaluation.getToUser().getId() + "' 不存在"));

        // 检查课程是否已完成
        if (course.getStatus() != CourseStatus.COMPLETED) {
            throw new BusinessException("只能对已完成的课程进行评价");
        }

        // 检查是否已评价过
        if (existsEvaluationByCourseAndFromUser(
                evaluation.getCourse().getId(),
                evaluation.getFromUser().getId(),
                evaluation.getType()
        )) {
            throw new BusinessException("您已经对该课程进行过评价");
        }

        // 验证评价类型和用户角色是否匹配
        if (!validateEvaluationType(evaluation.getType(), fromUser.getUserType(), toUser.getUserType())) {
            throw new BusinessException("评价类型与用户角色不匹配");
        }

        // 设置创建时间
        evaluation.setCreatedAt(LocalDateTime.now());

        return evaluationRepository.save(evaluation);
    }

    @Override
    @Transactional
    public Evaluation updateEvaluation(Long id, Evaluation evaluation) {
        Evaluation existingEvaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价ID '" + id + "' 不存在"));

        // 只能更新评价内容和评分
        if (evaluation.getContent() != null) {
            existingEvaluation.setContent(evaluation.getContent());
        }

        if (evaluation.getRating() != null) {
            existingEvaluation.setRating(evaluation.getRating());
        }

        return evaluationRepository.save(existingEvaluation);
    }

    @Override
    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价ID '" + id + "' 不存在"));

        evaluationRepository.delete(evaluation);
    }

    // 新的DTO方法实现
    @Override
    @Transactional
    public EvaluationDto createEvaluation(EvaluationCreateDto evaluationCreateDto) {
        Evaluation evaluation = EvaluationConverter.toEntity(evaluationCreateDto);
        Evaluation createdEvaluation = createEvaluation(evaluation);
        return EvaluationConverter.toDto(createdEvaluation);
    }

    @Override
    @Transactional
    public EvaluationDto updateEvaluation(Long id, EvaluationUpdateDTO evaluationUpdateDto) {
        Evaluation existingEvaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价ID '" + id + "' 不存在"));

        // 只能更新评价内容和评分
        if (evaluationUpdateDto.getContent() != null) {
            existingEvaluation.setContent(evaluationUpdateDto.getContent());
        }

        if (evaluationUpdateDto.getRating() != null) {
            existingEvaluation.setRating(evaluationUpdateDto.getRating());
        }

        Evaluation updatedEvaluation = evaluationRepository.save(existingEvaluation);
        return EvaluationConverter.toDto(updatedEvaluation);
    }

    @Override
    public EvaluationDto getEvaluation(Long id) {
        Evaluation evaluation = getEvaluationEntity(id);
        return EvaluationConverter.toDto(evaluation);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByCourse(Long courseId) {
        List<Evaluation> evaluations = getEvaluationsEntityByCourse(courseId);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByFromUser(Long fromUserId) {
        List<Evaluation> evaluations = getEvaluationsEntityByFromUser(fromUserId);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByToUser(Long toUserId) {
        List<Evaluation> evaluations = getEvaluationsEntityByToUser(toUserId);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByType(EvaluationType type) {
        List<Evaluation> evaluations = getEvaluationsEntityByType(type);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByFromUserAndType(Long fromUserId, EvaluationType type) {
        List<Evaluation> evaluations = getEvaluationsEntityByFromUserAndType(fromUserId, type);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByToUserAndType(Long toUserId, EvaluationType type) {
        List<Evaluation> evaluations = getEvaluationsEntityByToUserAndType(toUserId, type);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public List<EvaluationDto> getEvaluationsByCourseAndType(Long courseId, EvaluationType type) {
        List<Evaluation> evaluations = getEvaluationsEntityByCourseAndType(courseId, type);
        return EvaluationConverter.toDtoList(evaluations);
    }

    @Override
    public Page<EvaluationDto> getEvaluations(Pageable pageable) {
        Page<Evaluation> evaluations = getEvaluationsEntity(pageable);
        return evaluations.map(EvaluationConverter::toDto);
    }

    // 以下方法保留用于兼容旧代码
    @Override
    @Deprecated
    public Evaluation getEvaluationEntity(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价ID '" + id + "' 不存在"));
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("课程ID '" + courseId + "' 不存在");
        }
        return evaluationRepository.findByCourseId(courseId);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByFromUser(Long fromUserId) {
        if (!userRepository.existsById(fromUserId)) {
            throw new ResourceNotFoundException("用户ID '" + fromUserId + "' 不存在");
        }
        return evaluationRepository.findByFromUserId(fromUserId);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByToUser(Long toUserId) {
        if (!userRepository.existsById(toUserId)) {
            throw new ResourceNotFoundException("用户ID '" + toUserId + "' 不存在");
        }
        return evaluationRepository.findByToUserId(toUserId);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByType(EvaluationType type) {
        return evaluationRepository.findByType(type);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByFromUserAndType(Long fromUserId, EvaluationType type) {
        if (!userRepository.existsById(fromUserId)) {
            throw new ResourceNotFoundException("用户ID '" + fromUserId + "' 不存在");
        }
        return evaluationRepository.findByFromUserIdAndType(fromUserId, type);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByToUserAndType(Long toUserId, EvaluationType type) {
        if (!userRepository.existsById(toUserId)) {
            throw new ResourceNotFoundException("用户ID '" + toUserId + "' 不存在");
        }
        return evaluationRepository.findByToUserIdAndType(toUserId, type);
    }

    @Override
    @Deprecated
    public List<Evaluation> getEvaluationsEntityByCourseAndType(Long courseId, EvaluationType type) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("课程ID '" + courseId + "' 不存在");
        }
        return evaluationRepository.findByCourseIdAndType(courseId, type);
    }

    @Override
    @Deprecated
    public Page<Evaluation> getEvaluationsEntity(Pageable pageable) {
        return evaluationRepository.findAll(pageable);
    }

    @Override
    public Double calculateAverageRating(Long userId) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return evaluationRepository.calculateAverageRating(userId);
    }

    @Override
    public Long countEvaluationsByToUser(Long userId) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return evaluationRepository.countByToUserId(userId);
    }

    @Override
    public boolean existsEvaluationByCourseAndFromUser(Long courseId, Long fromUserId, EvaluationType type) {
        return evaluationRepository.existsByCourseIdAndFromUserIdAndType(courseId, fromUserId, type);
    }

    @Override
    public List<Long> getCoursesNeedEvaluation(Long userId) {
        // 获取用户已完成但未评价的课程
        List<Course> completedCourses = courseRepository.findByStatusAndStudentIdOrCoachId(
                CourseStatus.COMPLETED, userId, userId);

        return completedCourses.stream()
                .filter(course -> {
                    // 检查用户是否已经评价过该课程
                    EvaluationType type = course.getStudent().getId().equals(userId) ?
                            EvaluationType.STUDENT_TO_COACH : EvaluationType.COACH_TO_STUDENT;

                    return !existsEvaluationByCourseAndFromUser(course.getId(), userId, type);
                })
                .map(Course::getId)
                .collect(Collectors.toList());
    }

    /**
     * 验证评价类型和用户角色是否匹配
     */
    private boolean validateEvaluationType(EvaluationType type, UserType fromUserType, UserType toUserType) {
        switch (type) {
            case STUDENT_TO_COACH:
                return fromUserType == UserType.STUDENT && toUserType == UserType.COACH;
            case COACH_TO_STUDENT:
                return fromUserType == UserType.COACH && toUserType == UserType.STUDENT;
            default:
                return false;
        }
    }
}