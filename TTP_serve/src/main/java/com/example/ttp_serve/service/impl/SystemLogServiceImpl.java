package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.SystemLog;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.repository.SystemLogRepository;
import com.example.ttp_serve.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Autowired
    public SystemLogServiceImpl(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public SystemLog createLog(SystemLog log) {
        return systemLogRepository.save(log);
    }

    @Override
    public SystemLog getLog(Long id) {
        Optional<SystemLog> log = systemLogRepository.findById(id);
        return log.orElse(null);
    }

    @Override
    public List<SystemLog> getLogsByUser(Long userId) {
        return systemLogRepository.findByUserId(userId);
    }

    @Override
    public List<SystemLog> getLogsByModule(String module) {
        return systemLogRepository.findByModule(module);
    }

    @Override
    public List<SystemLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return systemLogRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public Page<SystemLog> getLogs(Pageable pageable) {
        return systemLogRepository.findAll(pageable);
    }

    @Override
    public List<SystemLog> searchLogs(String keyword) {
        return systemLogRepository.searchByOperationKeyword(keyword);
    }

    @Override
    public Long countLogs() {
        return systemLogRepository.countAll();
    }
}