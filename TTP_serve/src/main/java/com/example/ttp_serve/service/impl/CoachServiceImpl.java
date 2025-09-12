package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.CoachDTO;
import com.example.ttp_serve.entity.Coach;
import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.repository.CoachRepository;
import com.example.ttp_serve.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachServiceImpl implements CoachService {

    @Autowired
    private CoachRepository coachRepository;

    @Override
    public List<CoachDTO> findCoachesByCriteria(String name, String gender, Integer age, Long campusId) {
        // 验证至少有一个查询条件
        if (!StringUtils.hasText(name) && !StringUtils.hasText(gender) && age == null) {
            throw new IllegalArgumentException("至少需要提供一个查询条件");
        }

        List<Coach> coaches = coachRepository.findByCriteria(name, gender, age, campusId);
        return coaches.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CoachDTO> findAllCoachesByCampus(Long campusId) {
        List<Coach> coaches = coachRepository.findByCampusId(campusId);
        return coaches.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public CoachDTO getCoachDetail(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + coachId + " 的教练"));
        return convertToDTO(coach);
    }

    @Override
    public List<Coach> findCoachesByLevel(CoachLevel level) {
        return coachRepository.findByLevel(level);
    }

    @Override
    public Coach saveOrUpdateCoach(Coach coach) {
        return coachRepository.save(coach);
    }

    @Override
    public void deleteCoach(Long coachId) {
        coachRepository.deleteById(coachId);
    }

    @Override
    public long getTotalCoachCount() {
        return coachRepository.count();
    }

    @Override
    public long getCoachCountByCampus(Long campusId) {
        return coachRepository.countByCampusId(campusId);
    }

    /**
     * 将Coach实体转换为CoachDTO
     */
    private CoachDTO convertToDTO(Coach coach) {
        CoachDTO dto = new CoachDTO();
        dto.setId(coach.getId());
        dto.setUsername(coach.getUsername());
        dto.setRealName(coach.getRealName());
        dto.setGender(coach.getGender());
        dto.setAge(coach.getAge());
        dto.setPhone(coach.getPhone());
        dto.setEmail(coach.getEmail());
        dto.setAvatar(coach.getAvatar()); // 教练照片
        dto.setLevel(coach.getLevel());
        dto.setAwards(coach.getAwards()); // 获奖信息
        dto.setHourlyRate(coach.getHourlyRate());
        dto.setCurrentStudents(coach.getCurrentStudents());
        dto.setMaxStudents(coach.getMaxStudents());
        return dto;
    }
}