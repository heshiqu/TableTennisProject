package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.Court;
import com.example.ttp_serve.enums.CourtStatus;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.CourtRepository;
import com.example.ttp_serve.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 球台管理业务实现类
 * 实现球台相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;

    @Override
    public List<Court> getCourtsByCampusId(Long campusId) {
        return courtRepository.findByCampusId(campusId);
    }

    @Override
    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    @Override
    public List<Court> getCourtsByStatus(String status) {
        try {
            CourtStatus courtStatus = CourtStatus.valueOf(status.toUpperCase());
            return courtRepository.findByStatus(courtStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的球台状态: " + status);
        }
    }

    @Override
    public List<Court> getCourtsByCampusIdAndStatus(Long campusId, String status) {
        try {
            CourtStatus courtStatus = CourtStatus.valueOf(status.toUpperCase());
            return courtRepository.findByCampusIdAndStatus(campusId, courtStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的球台状态: " + status);
        }
    }

    @Override
    public Court getCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("球台ID '" + id + "' 不存在"));
    }

    @Override
    public Court getCourtByNumber(String courtNumber) {
        return courtRepository.findByCourtNumber(courtNumber)
                .orElseThrow(() -> new ResourceNotFoundException("球台编号 '" + courtNumber + "' 不存在"));
    }

    @Override
    public Long countCourtsByCampusId(Long campusId) {
        return courtRepository.countByCampusId(campusId);
    }

    @Override
    public Long countCourtsByCampusIdAndStatus(Long campusId, String status) {
        try {
            CourtStatus courtStatus = CourtStatus.valueOf(status.toUpperCase());
            return courtRepository.countByCampusIdAndStatus(campusId, courtStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的球台状态: " + status);
        }
    }
}