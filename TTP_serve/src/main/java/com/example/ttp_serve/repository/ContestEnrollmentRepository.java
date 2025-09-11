package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.ContestEnrollment;
import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestEnrollmentRepository extends JpaRepository<ContestEnrollment, Long> {

    // 根据月赛ID查找报名记录
    List<ContestEnrollment> findByContestId(Long contestId);

    // 根据学员ID查找报名记录
    List<ContestEnrollment> findByStudentId(Long studentId);

    // 根据状态查找报名记录
    List<ContestEnrollment> findByStatus(EnrollmentStatus status);

    // 根据月赛ID和组别查找报名记录
    List<ContestEnrollment> findByContestIdAndGroupType(Long contestId, ContestGroup groupType);

    // 根据月赛ID和状态查找报名记录
    List<ContestEnrollment> findByContestIdAndStatus(Long contestId, EnrollmentStatus status);

    // 根据月赛ID和学员ID查找报名记录
    Optional<ContestEnrollment> findByContestIdAndStudentId(Long contestId, Long studentId);

    // 统计月赛的报名人数
    Long countByContestId(Long contestId);

    // 统计月赛特定组别的报名人数
    Long countByContestIdAndGroupType(Long contestId, ContestGroup groupType);

    // 检查学员是否已报名月赛
    boolean existsByContestIdAndStudentId(Long contestId, Long studentId);
}