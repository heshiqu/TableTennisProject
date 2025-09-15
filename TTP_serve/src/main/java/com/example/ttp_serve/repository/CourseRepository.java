package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Course;
import com.example.ttp_serve.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 根据教练ID查找课程
    List<Course> findByCoachId(Long coachId);

    // 根据学员ID查找课程
    List<Course> findByStudentId(Long studentId);

    // 根据状态查找课程
    List<Course> findByStatus(CourseStatus status);

    // 根据教练ID和状态查找课程
    List<Course> findByCoachIdAndStatus(Long coachId, CourseStatus status);

    // 根据学员ID和状态查找课程
    List<Course> findByStudentIdAndStatus(Long studentId, CourseStatus status);

    // 根据校区ID查找课程
    @Query("SELECT c FROM Course c WHERE c.coach.campus.id = :campusId OR c.student.campus.id = :campusId")
    List<Course> findByCampusId(@Param("campusId") Long campusId);

    // 根据日期范围查找课程
    List<Course> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // 根据教练ID和日期范围查找课程
    List<Course> findByCoachIdAndStartTimeBetween(Long coachId, LocalDateTime start, LocalDateTime end);

    // 根据学员ID和日期范围查找课程
    List<Course> findByStudentIdAndStartTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);

    // 根据教练ID、状态和时间段查找课程
    List<Course> findByCoachIdAndStatusAndStartTimeBetween(Long coachId, CourseStatus status, LocalDateTime start, LocalDateTime end);

    // 检查时间冲突
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE " +
            "c.coach.id = :coachId AND c.status <> 'CANCELLED' AND " +
            "(c.startTime < :endTime AND c.endTime > :startTime)")
    boolean existsTimeConflict(@Param("coachId") Long coachId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    // 检查球台时间冲突
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE " +
            "c.court.id = :courtId AND c.status <> 'CANCELLED' AND " +
            "(c.startTime < :endTime AND c.endTime > :startTime)")
    boolean isCourtAvailable(@Param("courtId") Long courtId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    // 查找需要提醒的课程（上课前一小时）
    @Query("SELECT c FROM Course c WHERE c.status = 'CONFIRMED' AND " +
            "c.startTime BETWEEN :startTime AND :endTime")
    List<Course> findCoursesToRemind(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    // 统计教练的课程数量
    Long countByCoachId(Long coachId);

    // 统计学员的课程数量
    Long countByStudentId(Long studentId);

    // 分页查询课程
    Page<Course> findAll(Pageable pageable);

    List<Course> findByStatusAndEndTimeBeforeAndEvaluationsEmpty(CourseStatus status, LocalDateTime endTime);

    List<Course> findByStatusAndStudentIdOrCoachId(
            CourseStatus status,
            Long studentId,
            Long coachId
    );
}