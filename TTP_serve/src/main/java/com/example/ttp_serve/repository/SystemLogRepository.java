package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    // 根据用户ID查找日志
    List<SystemLog> findByUserId(Long userId);

    // 根据模块查找日志
    List<SystemLog> findByModule(String module);

    // 根据操作查找日志
    List<SystemLog> findByOperation(String operation);

    // 根据日期范围查找日志
    List<SystemLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 根据用户ID和日期范围查找日志
    List<SystemLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 根据模块和日期范围查找日志
    List<SystemLog> findByModuleAndCreatedAtBetween(String module, LocalDateTime start, LocalDateTime end);

    // 搜索包含关键字的操作日志
    @Query("SELECT sl FROM SystemLog sl WHERE sl.operation LIKE %:keyword%")
    List<SystemLog> searchByOperationKeyword(@Param("keyword") String keyword);

    // 分页查询日志
    Page<SystemLog> findAll(Pageable pageable);

    // 统计日志数量
    @Query("SELECT COUNT(sl) FROM SystemLog sl")
    Long countAll();
}