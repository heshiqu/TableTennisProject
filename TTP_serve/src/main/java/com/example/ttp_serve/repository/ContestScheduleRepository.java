package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.ContestSchedule;
import com.example.ttp_serve.enums.ContestScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestScheduleRepository extends JpaRepository<ContestSchedule, Long> {

    // 根据月赛ID查找比赛安排
    List<ContestSchedule> findByContestId(Long contestId);

    // 根据轮次查找比赛安排
    List<ContestSchedule> findByRoundNumber(Integer roundNumber);

    // 根据状态查找比赛安排
    List<ContestSchedule> findByStatus(ContestScheduleStatus status);

    // 根据月赛ID和轮次查找比赛安排
    List<ContestSchedule> findByContestIdAndRoundNumber(Long contestId, Integer roundNumber);

    // 根据月赛ID和状态查找比赛安排
    List<ContestSchedule> findByContestIdAndStatus(Long contestId, ContestScheduleStatus status);

    // 根据学员ID查找比赛安排（作为选手1）
    List<ContestSchedule> findByPlayer1Id(Long playerId);

    // 根据学员ID查找比赛安排（作为选手2）
    List<ContestSchedule> findByPlayer2Id(Long playerId);

    // 根据学员ID查找比赛安排（作为任一选手）
    @Query("SELECT cs FROM ContestSchedule cs WHERE cs.player1.id = :playerId OR cs.player2.id = :playerId")
    List<ContestSchedule> findByPlayerId(@Param("playerId") Long playerId);

    // 统计月赛的比赛场次
    Long countByContestId(Long contestId);

    // 统计月赛特定轮次的比赛场次
    Long countByContestIdAndRoundNumber(Long contestId, Integer roundNumber);
}