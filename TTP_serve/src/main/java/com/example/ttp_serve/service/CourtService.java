package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.Court;
import java.util.List;

/**
 * 球台管理业务接口
 * 提供球台相关的业务逻辑处理
 */
public interface CourtService {

    /**
     * 根据校区ID获取所有球台
     * @param campusId 校区ID
     * @return 该校区下的所有球台列表
     */
    List<Court> getCourtsByCampusId(Long campusId);

    /**
     * 获取所有球台
     * @return 所有球台列表
     */
    List<Court> getAllCourts();

    /**
     * 根据状态获取球台
     * @param status 球台状态
     * @return 指定状态的球台列表
     */
    List<Court> getCourtsByStatus(String status);

    /**
     * 根据校区ID和状态获取球台
     * @param campusId 校区ID
     * @param status 球台状态
     * @return 指定校区和状态的球台列表
     */
    List<Court> getCourtsByCampusIdAndStatus(Long campusId, String status);

    /**
     * 获取球台详情
     * @param id 球台ID
     * @return 球台实体
     */
    Court getCourtById(Long id);

    /**
     * 根据球台编号获取球台
     * @param courtNumber 球台编号
     * @return 球台实体
     */
    Court getCourtByNumber(String courtNumber);

    /**
     * 统计校区球台数量
     * @param campusId 校区ID
     * @return 该校区下的球台数量
     */
    Long countCourtsByCampusId(Long campusId);

    /**
     * 统计校区内特定状态的球台数量
     * @param campusId 校区ID
     * @param status 球台状态
     * @return 该校区下指定状态的球台数量
     */
    Long countCourtsByCampusIdAndStatus(Long campusId, String status);
}