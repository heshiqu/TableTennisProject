package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.MonthlyContest;
import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyContestRepository extends JpaRepository<MonthlyContest, Long> {

    // 根据校区ID查找月赛
    List<MonthlyContest> findByCampusId(Long campusId);

    // 根据状态查找月赛
    List<MonthlyContest> findByStatus(ContestStatus status);

    // 根据日期查找月赛
    List<MonthlyContest> findByContestDate(LocalDate contestDate);

    // 根据日期范围查找月赛
    List<MonthlyContest> findByContestDateBetween(LocalDate start, LocalDate end);

    // 根据校区ID和日期查找月赛
    Optional<MonthlyContest> findByCampusIdAndContestDate(Long campusId, LocalDate contestDate);

    // 根据校区ID和状态查找月赛
    List<MonthlyContest> findByCampusIdAndStatus(Long campusId, ContestStatus status);

    // 根据校区ID和组别查找月赛
    List<MonthlyContest> findByCampusIdAndGroupType(Long campusId, ContestGroup groupType);

    // 查找最近的月赛
    @Query("SELECT mc FROM MonthlyContest mc WHERE mc.contestDate >= :today ORDER BY mc.contestDate ASC")
    List<MonthlyContest> findUpcomingContests(@Param("today") LocalDate today);

    // 统计校区内的月赛数量
    Long countByCampusId(Long campusId);
}