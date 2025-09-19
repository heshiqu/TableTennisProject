package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.CourseRequestDTO;
import com.example.ttp_serve.entity.Course;
import com.example.ttp_serve.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;



public interface CourseService {

    // 创建课程预约
    Course createCourse(Course course);

    // 更新课程信息
    Course updateCourse(Long id, Course course);

    // 取消课程
    Course cancelCourse(Long id, String reason, Long cancelledBy);

    // 确认课程
    Course confirmCourse(Long id);

    // 完成课程
    Course completeCourse(Long id);

    // 获取课程详情
    Course getCourse(Long id);

    // 获取教练的课程
    List<Course> getCoursesByCoach(Long coachId);

    // 获取学员的课程
    List<Course> getCoursesByStudent(Long studentId);

    // 获取特定状态的课程
    List<Course> getCoursesByStatus(CourseStatus status);

    // 根据学员ID和状态获取课程
    List<Course> getCoursesByStudentAndStatus(Long studentId, CourseStatus status);

    // 根据教练ID和状态获取课程
    List<Course> getCoursesByCoachAndStatus(Long coachId, CourseStatus status);

    // 获取日期范围内的课程
    List<Course> getCoursesByDateRange(LocalDateTime start, LocalDateTime end);

    // 获取教练在日期范围内的课程
    List<Course> getCoachCoursesByDateRange(Long coachId, LocalDateTime start, LocalDateTime end);

    // 获取学员在日期范围内的课程
    List<Course> getStudentCoursesByDateRange(Long studentId, LocalDateTime start, LocalDateTime end);

    // 检查时间冲突
    boolean checkTimeConflict(Long coachId, LocalDateTime startTime, LocalDateTime endTime);

    // 检查球台时间冲突
    boolean checkCourtTimeConflict(Long courtId, LocalDateTime startTime, LocalDateTime endTime);

    // 统计课程数量
    Long countCourses();

    // 统计教练的课程数量
    Long countCoursesByCoach(Long coachId);

    // 统计学员的课程数量
    Long countCoursesByStudent(Long studentId);

    // 分页获取所有课程
    Page<Course> getAllCourses(Pageable pageable);

    // 获取即将开始的课程（用于提醒）
    List<Course> getUpcomingCourses(int hours);

    // 批量完成已结束的课程
    @Transactional
    void completeExpiredCourses(Long userId);

    // 获取需要评价的课程
    List<Course> getCoursesNeedEvaluation(Long userId);

    // 使用DTO创建课程预约
    Course createCourseFromRequest(CourseRequestDTO courseRequest);

    // 使用DTO更新课程信息
    Course updateCourseFromRequest(Long id, CourseRequestDTO courseRequest);

    /**
     * 教练拒绝课程预约
     * 
     * @param id 课程ID
     * @param coachId 教练ID
     * @param reason 拒绝原因
     * @return 被拒绝的课程
     */
    Course rejectCourse(Long id, Long coachId, String reason);
    
    /**
     * 获取教练本月收入
     * 
     * @param coachId 教练ID
     * @return 本月已完成课程的总收入
     */
    BigDecimal getCoachMonthlyIncome(Long coachId);
    
    /**
     * 获取今日已确认课程数目
     * 
     * @return 今日已确认的课程数量
     */
    Long countTodayConfirmedCourses();
    
    /**
     * 获取指定教练今日已确认课程数目
     * 
     * @param coachId 教练ID
     * @return 指定教练今日已确认的课程数量
     */
    Long countTodayConfirmedCoursesByCoach(Long coachId);
    
    /**
     * 获取指定学员今日已确认课程数目
     * 
     * @param studentId 学员ID
     * @return 指定学员今日已确认的课程数量
     */
    Long countTodayConfirmedCoursesByStudent(Long studentId);
    
    /**
     * 获取指定校区今日已确认课程数目
     * 
     * @param campusId 校区ID
     * @return 指定校区今日已确认的课程数量
     */
    Long countTodayConfirmedCoursesByCampus(Long campusId);
    
    /**
     * 获取所有课程列表
     * 
     * @return 所有课程列表
     */
    List<Course> getAllCoursesList();
    
    /**
     * 根据校区ID获取所有课程
     * 
     * @param campusId 校区ID
     * @return 指定校区的所有课程列表
     */
    List<Course> getCoursesByCampusId(Long campusId);
}