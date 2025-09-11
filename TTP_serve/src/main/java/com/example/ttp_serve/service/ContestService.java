package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.ContestEnrollment;
import com.example.ttp_serve.entity.ContestSchedule;
import com.example.ttp_serve.entity.MonthlyContest;
import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.ContestStatus;
import com.example.ttp_serve.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface ContestService {

    // 创建月赛
    MonthlyContest createContest(MonthlyContest contest);

    // 更新月赛信息
    MonthlyContest updateContest(Long id, MonthlyContest contest);

    // 删除月赛
    void deleteContest(Long id);

    // 获取月赛详情
    MonthlyContest getContest(Long id);

    // 获取校区所有月赛
    List<MonthlyContest> getContestsByCampus(Long campusId);

    // 获取特定状态的月赛
    List<MonthlyContest> getContestsByStatus(ContestStatus status);

    // 获取即将开始的月赛
    List<MonthlyContest> getUpcomingContests();

    // 报名月赛
    ContestEnrollment enrollContest(ContestEnrollment enrollment);

    // 更新报名状态
    ContestEnrollment updateEnrollmentStatus(Long id, EnrollmentStatus status);

    // 获取月赛的报名记录
    List<ContestEnrollment> getEnrollmentsByContest(Long contestId);

    // 获取学员的报名记录
    List<ContestEnrollment> getEnrollmentsByStudent(Long studentId);

    // 检查学员是否已报名月赛
    boolean isStudentEnrolled(Long contestId, Long studentId);

    // 创建比赛安排
    ContestSchedule createSchedule(ContestSchedule schedule);

    // 更新比赛安排
    ContestSchedule updateSchedule(Long id, ContestSchedule schedule);

    // 获取月赛的比赛安排
    List<ContestSchedule> getSchedulesByContest(Long contestId);

    // 获取轮次的比赛安排
    List<ContestSchedule> getSchedulesByRound(Long contestId, Integer roundNumber);

    // 获取学员的比赛安排
    List<ContestSchedule> getSchedulesByPlayer(Long playerId);

    // 生成比赛安排
    void generateContestSchedule(Long contestId, ContestGroup groupType);

    // 统计月赛的报名人数
    Long countEnrollmentsByContest(Long contestId);

    // 统计月赛组别的报名人数
    Long countEnrollmentsByGroup(Long contestId, ContestGroup groupType);

    // 更新比赛结果
    ContestSchedule updateMatchResult(Long scheduleId, Long winnerId);
}