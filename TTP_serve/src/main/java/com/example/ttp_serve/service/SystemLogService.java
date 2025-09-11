package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogService {

    SystemLog createLog(SystemLog log);
    SystemLog getLog(Long id);
    List<SystemLog> getLogsByUser(Long userId);
    List<SystemLog> getLogsByModule(String module);
    List<SystemLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end);
    Page<SystemLog> getLogs(Pageable pageable);
    List<SystemLog> searchLogs(String keyword);
    Long countLogs();
}