package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.CampusRequestDTO;
import com.example.ttp_serve.dto.CampusResponseDTO;
import com.example.ttp_serve.dto.CampusStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampusService {

    // 获取所有校区
    List<CampusResponseDTO> getAllCampuses();

    // 分页获取所有校区
    Page<CampusResponseDTO> getAllCampuses(Pageable pageable);

    // 根据ID获取校区
    CampusResponseDTO getCampusById(Long id);

    // 创建校区
    CampusResponseDTO createCampus(CampusRequestDTO campusRequestDTO);

    // 更新校区
    CampusResponseDTO updateCampus(Long id, CampusRequestDTO campusRequestDTO);

    // 删除校区
    void deleteCampus(Long id);

    // 获取所有顶级校区（无父校区）
    List<CampusResponseDTO> getTopLevelCampuses();

    // 根据父校区ID获取子校区
    List<CampusResponseDTO> getChildCampuses(Long parentId);

    // 根据名称搜索校区
    List<CampusResponseDTO> searchCampusesByName(String name);

    // 统计校区数量
    Long countCampuses();

    // 统计子校区数量
    Long countChildCampuses(Long parentId);

    // 检查校区名称是否已存在
    boolean isCampusNameExists(String name);

    // 检查校区名称是否已存在（排除指定ID）
    boolean isCampusNameExists(String name, Long excludeId);

    // 获取校区及其所有子校区
    List<CampusResponseDTO> getCampusWithChildren(Long campusId);

    List<CampusResponseDTO> getCampusTree();

    CampusStatsDTO getCampusStats(Long campusId);
}