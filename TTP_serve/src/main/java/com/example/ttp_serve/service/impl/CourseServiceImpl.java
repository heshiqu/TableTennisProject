package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.CourseRequestDTO;
import com.example.ttp_serve.entity.*;
import com.example.ttp_serve.enums.CourseStatus;
import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentType;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.*;
import com.example.ttp_serve.service.CourseService;
import com.example.ttp_serve.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final PaymentService paymentService;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Course createCourseFromRequest(CourseRequestDTO courseRequest) {
        Course course = new Course();

        // 检查教练是否存在
        Coach coach = coachRepository.findById(courseRequest.getCoachId())
                .orElseThrow(() -> new ResourceNotFoundException("教练ID '" + courseRequest.getCoachId() + "' 不存在"));
        course.setCoach(coach);

        // 检查学员是否存在
        Student student = studentRepository.findById(courseRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("学员ID '" + courseRequest.getStudentId() + "' 不存在"));
        course.setStudent(student);

        // 检查球台是否存在（如果指定了球台）
        if (courseRequest.getCourtId() != null) {
            Court court = courtRepository.findById(courseRequest.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("球台ID '" + courseRequest.getCourtId() + "' 不存在"));
            course.setCourt(court);
        }

        course.setStartTime(courseRequest.getStartTime());
        course.setEndTime(courseRequest.getEndTime());


        // 检查时间冲突
        if (checkTimeConflict(courseRequest.getCoachId(), courseRequest.getStartTime(), courseRequest.getEndTime())) {
            throw new BusinessException("该时间段已有预约");
        }


        // 检查球台时间冲突（如果指定了球台）
        if (courseRequest.getCourtId() != null && checkCourtTimeConflict(
                courseRequest.getCourtId(),
                courseRequest.getStartTime(),
                courseRequest.getEndTime()
        )) {
            throw new BusinessException("该球台在该时间段已被占用");
        }

        // 计算课程时长（小时）
        long minutes = java.time.Duration.between(courseRequest.getStartTime(), courseRequest.getEndTime()).toMinutes();
        BigDecimal duration = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        course.setDuration(duration);

        // 计算课程费用（使用教练的小时费率）
        course.setFee(coach.getHourlyRate().multiply(duration));

        // 设置默认状态
        course.setStatus(CourseStatus.PENDING);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourseFromRequest(Long id, CourseRequestDTO courseRequest) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));

        // 只能更新未确认的课程
        if (existingCourse.getStatus() != CourseStatus.PENDING) {
            throw new BusinessException("只能修改未确认的课程");
        }

        // 更新允许修改的字段
        if (courseRequest.getStartTime() != null) {
            existingCourse.setStartTime(courseRequest.getStartTime());
        }

        if (courseRequest.getEndTime() != null) {
            existingCourse.setEndTime(courseRequest.getEndTime());
        }

        if (courseRequest.getCourtId() != null) {
            Court court = courtRepository.findById(courseRequest.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("球台ID '" + courseRequest.getCourtId() + "' 不存在"));
            existingCourse.setCourt(court);
        }

        // 重新计算时长和费用
        if (courseRequest.getStartTime() != null || courseRequest.getEndTime() != null) {
            long minutes = java.time.Duration.between(
                    existingCourse.getStartTime(),
                    existingCourse.getEndTime()
            ).toMinutes();

            BigDecimal duration = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
            existingCourse.setDuration(duration);

            Coach coachInfo = existingCourse.getCoach();
            if (coachInfo != null) {
                existingCourse.setFee(coachInfo.getHourlyRate().multiply(duration));
            }
        }

        existingCourse.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(existingCourse);
    }

    // 原有方法保持不变，但需要将createCourse和updateCourse方法标记为过时或删除
    @Override
    @Transactional
    @Deprecated
    public Course createCourse(Course course) {
        // 保持原有实现，但标记为过时
        return createCourseFromRequest(convertToRequestDTO(course));
    }

    @Override
    @Transactional
    @Deprecated
    public Course updateCourse(Long id, Course course) {
        // 保持原有实现，但标记为过时
        return updateCourseFromRequest(id, convertToRequestDTO(course));
    }

    // 辅助方法：将Course实体转换为CourseRequestDTO
    private CourseRequestDTO convertToRequestDTO(Course course) {
        CourseRequestDTO dto = new CourseRequestDTO();
        dto.setCoachId(course.getCoach().getId());
        dto.setStudentId(course.getStudent().getId());
        if (course.getCourt() != null) {
            dto.setCourtId(course.getCourt().getId());
        }
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        return dto;
    }

    @Override
    @Transactional
    public Course cancelCourse(Long id, String reason, Long cancelledBy) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));

        // 检查是否可以取消（提前24小时）
        if (LocalDateTime.now().isAfter(course.getStartTime().minusHours(24))) {
            throw new BusinessException("课程开始前24小时内不能取消");
        }

        // 设置取消信息
        User cancelUser = userRepository.findById(cancelledBy)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + cancelledBy + "' 不存在"));

        // 处理退款（如果课程已支付）
        if (course.getStatus() == CourseStatus.CONFIRMED) {
            Payment payment = paymentRepository.findByRelatedId(course.getId());
            // 创建支付记录
            Payment refundPayment=paymentService.generatePaymentOrder(
                    PaymentType.REFUND,
                    payment.getPaymentMethod(),
                    payment.getUser().getId(),
                    payment.getAmount(),
                    payment.getRelatedId()// related_id设置为课程ID
            );
            paymentService.processRefund(refundPayment.getOrderId());
        }

        course.setStatus(CourseStatus.CANCELLED);
        course.setCancelReason(reason);
        course.setCancelBy(cancelUser);
        course.setCancelTime(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course confirmCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));

        if (course.getStatus() != CourseStatus.PENDING) {
            throw new BusinessException("只能确认待确认的课程");
        }

        // 检查学员余额是否足够
        Student student = studentRepository.findById(course.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("学员不存在"));

        if (student.getBalance().compareTo(course.getFee()) < 0) {
            throw new BusinessException("学员余额不足，无法确认课程");
        }

        // 扣除课程费用
        student.setBalance(student.getBalance().subtract(course.getFee()));
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        // 创建支付记录
        paymentService.generatePaymentOrder(
                PaymentType.COURSE_FEE,
                PaymentMethod.OFFLINE, // 或者根据实际情况选择支付方式
                course.getStudent().getId(),
                course.getFee(),
                course.getId() // related_id设置为课程ID
        );

        course.setStatus(CourseStatus.CONFIRMED);
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course completeCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));

        if (course.getStatus() != CourseStatus.CONFIRMED) {
            throw new BusinessException("只能完成已确认的课程");
        }

        course.setStatus(CourseStatus.COMPLETED);
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Override
    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));
    }

    @Override
    public List<Course> getCoursesByCoach(Long coachId) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return courseRepository.findByCoachId(coachId);
    }

    @Override
    public List<Course> getCoursesByCoachAndStatus(Long coachId, CourseStatus status) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return courseRepository.findByCoachIdAndStatus(coachId, status);
    }

    @Override
    public List<Course> getCoursesByStudent(Long studentId) {
        // 检查学员是否存在
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return courseRepository.findByStudentId(studentId);
    }

    @Override
    public List<Course> getCoursesByStatus(CourseStatus status) {
        return courseRepository.findByStatus(status);
    }

    @Override
    public List<Course> getCoursesByDateRange(LocalDateTime start, LocalDateTime end) {
        return courseRepository.findByStartTimeBetween(start, end);
    }

    @Override
    public List<Course> getCoachCoursesByDateRange(Long coachId, LocalDateTime start, LocalDateTime end) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return courseRepository.findByCoachIdAndStartTimeBetween(coachId, start, end);
    }

    @Override
    public List<Course> getStudentCoursesByDateRange(Long studentId, LocalDateTime start, LocalDateTime end) {
        // 检查学员是否存在
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return courseRepository.findByStudentIdAndStartTimeBetween(studentId, start, end);
    }

    @Override
    public List<Course> getCoursesByStudentAndStatus(Long studentId, CourseStatus status) {
        // 检查学员是否存在
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return courseRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public boolean checkTimeConflict(Long coachId, LocalDateTime startTime, LocalDateTime endTime) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return courseRepository.existsTimeConflict(coachId, startTime, endTime);
    }

    @Override
    public boolean checkCourtTimeConflict(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        return courseRepository.isCourtAvailable(courtId, startTime, endTime);
    }

    @Override
    public Long countCourses() {
        return courseRepository.count();
    }

    @Override
    public Long countCoursesByCoach(Long coachId) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        return courseRepository.countByCoachId(coachId);
    }

    @Override
    public Long countCoursesByStudent(Long studentId) {
        // 检查学员是否存在
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("学员ID '" + studentId + "' 不存在");
        }

        return courseRepository.countByStudentId(studentId);
    }

    @Override
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public List<Course> getUpcomingCourses(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);

        return courseRepository.findCoursesToRemind(now, endTime);
    }

    @Override
    public List<Course> getCoursesNeedEvaluation(Long userId) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // 获取已完成但未评价的课程
        return courseRepository.findByStatusAndEndTimeBeforeAndEvaluationsEmpty(
                CourseStatus.COMPLETED, yesterday);
    }

    @Override
    @Transactional
    public Course rejectCourse(Long id, Long coachId, String reason) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程ID '" + id + "' 不存在"));

        // 检查是否是该教练的课程
        if (!course.getCoach().getId().equals(coachId)) {
            throw new BusinessException("只能拒绝自己的课程预约");
        }

        // 检查课程状态是否允许拒绝（只能是待确认状态）
        if (course.getStatus() != CourseStatus.PENDING) {
            throw new BusinessException("只能拒绝待确认的课程预约");
        }

        // 设置取消信息
        course.setStatus(CourseStatus.CANCELLED);
        course.setCancelReason("教练拒绝: " + reason);
        
        // 获取教练信息作为取消操作者
        User coachUser = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("教练ID '" + coachId + "' 不存在"));
        course.setCancelBy(coachUser);
        course.setCancelTime(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Override
    public BigDecimal getCoachMonthlyIncome(Long coachId) {
        // 检查教练是否存在
        if (!userRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("教练ID '" + coachId + "' 不存在");
        }

        // 获取当前月份的开始和结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        // 查询教练本月已完成的课程
        List<Course> completedCourses = courseRepository.findByCoachIdAndStatusAndStartTimeBetween(
                coachId, CourseStatus.COMPLETED, startOfMonth, endOfMonth);

        // 计算总收入
        return completedCourses.stream()
                .map(Course::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}