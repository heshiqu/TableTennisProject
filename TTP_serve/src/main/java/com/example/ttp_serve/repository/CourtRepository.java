package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Court;
import com.example.ttp_serve.enums.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    // 根据校区ID查找球台
    List<Court> findByCampusId(Long campusId);

    // 根据状态查找球台
    List<Court> findByStatus(CourtStatus status);

    // 根据校区ID和状态查找球台
    List<Court> findByCampusIdAndStatus(Long campusId, CourtStatus status);

    // 根据球台编号查找球台
    Optional<Court> findByCourtNumber(String courtNumber);

    // 查找指定时间段内可用的球台
    @Query("SELECT c FROM Court c WHERE c.campus.id = :campusId AND c.status = 'AVAILABLE' " +
            "AND c.id NOT IN (SELECT co.court.id FROM Course co WHERE " +
            "(co.startTime < :endTime AND co.endTime > :startTime) AND co.status <> 'CANCELLED')")
    List<Court> findAvailableCourts(@Param("campusId") Long campusId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    // 统计校区内的球台数量
    Long countByCampusId(Long campusId);

    // 统计校区内特定状态的球台数量
    Long countByCampusIdAndStatus(Long campusId, CourtStatus status);
}