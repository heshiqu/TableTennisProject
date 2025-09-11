package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.CoachDTO;
import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.entity.Coach;

import java.util.List;

public interface CoachService {

    /**
     * 根据条件查询教练
     * @param name 教练姓名(可选)
     * @param gender 性别(可选)
     * @param age 年龄(可选)
     * @param campusId 校区ID
     * @return 符合条件的教练DTO列表
     */
    List<CoachDTO> findCoachesByCriteria(String name, String gender, Integer age, Long campusId);

    /**
     * 获取校区所有教练
     * @param campusId 校区ID
     * @return 校区所有教练DTO列表
     */
    List<CoachDTO> findAllCoachesByCampus(Long campusId);

    /**
     * 根据ID获取教练详情
     * @param userId 教练ID
     * @return 教练详情DTO
     */
    CoachDTO getCoachDetail(Long userId);

    /**
     * 根据教练级别查询
     * @param level 教练级别
     * @return 符合条件的教练列表
     */
    List<Coach> findCoachesByLevel(CoachLevel level);

    /**
     * 保存或更新教练信息
     * @param coach 教练实体
     * @return 保存后的教练实体
     */
    Coach saveOrUpdateCoach(Coach coach);

    /**
     * 删除教练
     * @param coachId 教练ID
     */
    void deleteCoach(Long coachId);
}