package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CampusService {

    // 获取所有校区
    List<Campus> getAllCampuses();

    // 分页获取所有校区
    Page<Campus> getAllCampuses(Pageable pageable);

    // 根据ID获取校区
    Optional<Campus> getCampusById(Long id);

    // 创建校区
    Campus createCampus(Campus campus);

    // 更新校区
    Campus updateCampus(Long id, Campus campus);

    // 删除校区
    void deleteCampus(Long id);

    // 获取所有顶级校区（无父校区）
    List<Campus> getTopLevelCampuses();

    // 根据父校区ID获取子校区
    List<Campus> getChildCampuses(Long parentId);

    // 根据名称搜索校区
    List<Campus> searchCampusesByName(String name);

    // 统计校区数量
    Long countCampuses();

    // 统计子校区数量
    Long countChildCampuses(Long parentId);

    // 检查校区名称是否已存在
    boolean isCampusNameExists(String name);

    // 检查校区名称是否已存在（排除指定ID）
    boolean isCampusNameExists(String name, Long excludeId);

    // 获取校区及其所有子校区
    List<Campus> getCampusWithChildren(Long campusId);

    List<Campus> getCampusTree();

    CampusStats getCampusStats(Long campusId);

    /**
     * 校区统计信息类
     */
    public static class CampusStats {
        private Long campusId;
        private String campusName;
        private Long userCount;
        private Long studentCount;
        private Long coachCount;
        private Long adminCount;
        private Long childCampusCount;

        // 构造函数、getter和setter方法
        public CampusStats() {}

        public Long getCampusId() { return campusId; }
        public void setCampusId(Long campusId) { this.campusId = campusId; }

        public String getCampusName() { return campusName; }
        public void setCampusName(String campusName) { this.campusName = campusName; }

        public Long getUserCount() { return userCount; }
        public void setUserCount(Long userCount) { this.userCount = userCount; }

        public Long getStudentCount() { return studentCount; }
        public void setStudentCount(Long studentCount) { this.studentCount = studentCount; }

        public Long getCoachCount() { return coachCount; }
        public void setCoachCount(Long coachCount) { this.coachCount = coachCount; }

        public Long getAdminCount() { return adminCount; }
        public void setAdminCount(Long adminCount) { this.adminCount = adminCount; }

        public Long getChildCampusCount() { return childCampusCount; }
        public void setChildCampusCount(Long childCampusCount) { this.childCampusCount = childCampusCount; }
    }
}