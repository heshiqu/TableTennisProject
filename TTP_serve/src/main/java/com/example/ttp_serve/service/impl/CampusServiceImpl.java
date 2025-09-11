package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.Campus;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.exception.DuplicateResourceException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.repository.CampusRepository;
import com.example.ttp_serve.repository.UserRepository;
import com.example.ttp_serve.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final UserRepository userRepository;

    @Override
    public List<Campus> getAllCampuses() {
        return campusRepository.findAll();
    }

    @Override
    public Page<Campus> getAllCampuses(Pageable pageable) {
        return campusRepository.findAll(pageable);
    }

    @Override
    public Optional<Campus> getCampusById(Long id) {
        return campusRepository.findById(id);
    }

    @Override
    @Transactional
    public Campus createCampus(Campus campus) {
        // 检查校区名称是否已存在
        if (isCampusNameExists(campus.getName())) {
            throw new DuplicateResourceException("校区名称 '" + campus.getName() + "' 已存在");
        }

        // 检查父校区是否存在（如果指定了父校区）
        if (campus.getParent() != null && campus.getParent().getId() != null) {
            Campus parentCampus = campusRepository.findById(campus.getParent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("父校区不存在"));

            campus.setParent(parentCampus);

            // 检查是否形成循环引用
            if (isCircularReference(campus, parentCampus)) {
                throw new BusinessException("不能创建循环引用的校区关系");
            }
        }

        campus.setCreatedAt(LocalDateTime.now());
        campus.setUpdatedAt(LocalDateTime.now());

        return campusRepository.save(campus);
    }

    @Override
    @Transactional
    public Campus updateCampus(Long id, Campus campus) {
        Campus existingCampus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + id + "' 不存在"));

        // 检查校区名称是否与其他校区冲突
        if (campus.getName() != null && !campus.getName().equals(existingCampus.getName())) {
            if (isCampusNameExists(campus.getName(), id)) {
                throw new DuplicateResourceException("校区名称 '" + campus.getName() + "' 已存在");
            }
            existingCampus.setName(campus.getName());
        }

        // 更新地址信息
        if (campus.getAddress() != null) {
            existingCampus.setAddress(campus.getAddress());
        }

        // 更新联系人信息
        if (campus.getContactPerson() != null) {
            existingCampus.setContactPerson(campus.getContactPerson());
        }

        if (campus.getContactPhone() != null) {
            existingCampus.setContactPhone(campus.getContactPhone());
        }

        if (campus.getEmail() != null) {
            existingCampus.setEmail(campus.getEmail());
        }

        // 更新父校区关系
        if (campus.getParent() != null && campus.getParent().getId() != null) {
            Campus parentCampus = campusRepository.findById(campus.getParent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("父校区不存在"));

            // 检查是否形成循环引用
            if (isCircularReference(existingCampus, parentCampus)) {
                throw new BusinessException("不能创建循环引用的校区关系");
            }

            existingCampus.setParent(parentCampus);
        } else if (campus.getParent() == null) {
            // 清除父校区关系
            existingCampus.setParent(null);
        }

        existingCampus.setUpdatedAt(LocalDateTime.now());

        return campusRepository.save(existingCampus);
    }

    @Override
    @Transactional
    public void deleteCampus(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + id + "' 不存在"));

        // 检查是否有子校区
        if (!campus.getChildren().isEmpty()) {
            throw new BusinessException("该校区下有子校区，无法删除");
        }

        // 检查是否有用户关联到此校区
        Page<User> page = userRepository.findByCampusId(id, Pageable.unpaged());
        List<User> usersInCampus =page.getContent(); // 提取 List<User>
        if (!usersInCampus.isEmpty()) {
            throw new BusinessException("该校区下有用户，无法删除");
        }

        campusRepository.delete(campus);
    }

    @Override
    public List<Campus> getTopLevelCampuses() {
        return campusRepository.findByParentIsNull();
    }

    @Override
    public List<Campus> getChildCampuses(Long parentId) {
        return campusRepository.findByParentId(parentId);
    }

    @Override
    public List<Campus> searchCampusesByName(String name) {
        return campusRepository.findByName(name)
                .map(Collections::singletonList)  // 转换为单元素列表
                .orElse(Collections.emptyList()); // 或返回空列表
    }

    @Override
    public Long countCampuses() {
        return campusRepository.count();
    }

    @Override
    public Long countChildCampuses(Long parentId) {
        return campusRepository.countByParentId(parentId);
    }

    @Override
    public boolean isCampusNameExists(String name) {
        return campusRepository.findByName(name).isPresent();
    }

    @Override
    public boolean isCampusNameExists(String name, Long excludeId) {
        Optional<Campus> campusOpt = campusRepository.findByName(name);
        return campusOpt.isPresent() && !campusOpt.get().getId().equals(excludeId);
    }

    @Override
    public List<Campus> getCampusWithChildren(Long campusId) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + campusId + "' 不存在"));

        // 获取所有子校区（包括子校区的子校区）
        return getAllChildren(campus);
    }

    /**
     * 检查是否形成循环引用
     */
    private boolean isCircularReference(Campus campus, Campus potentialParent) {
        // 如果校区和潜在父校区是同一个，则形成循环引用
        if (campus.getId() != null && campus.getId().equals(potentialParent.getId())) {
            return true;
        }

        // 检查潜在父校区的父级是否包含当前校区
        Campus currentParent = potentialParent.getParent();
        while (currentParent != null) {
            if (currentParent.getId().equals(campus.getId())) {
                return true;
            }
            currentParent = currentParent.getParent();
        }

        return false;
    }

    /**
     * 递归获取所有子校区
     */
    private List<Campus> getAllChildren(Campus campus) {
        List<Campus> allChildren = new java.util.ArrayList<>();

        // 获取直接子校区
        List<Campus> directChildren = campusRepository.findByParentId(campus.getId());
        allChildren.addAll(directChildren);

        // 递归获取子校区的子校区
        for (Campus child : directChildren) {
            allChildren.addAll(getAllChildren(child));
        }

        return allChildren;
    }

    /**
     * 获取校区树形结构
     */
    public List<Campus> getCampusTree() {
        List<Campus> topLevelCampuses = getTopLevelCampuses();

        // 为每个顶级校区构建树形结构
        for (Campus campus : topLevelCampuses) {
            buildCampusTree(campus);
        }

        return topLevelCampuses;
    }

    /**
     * 递归构建校区树形结构
     */
    private void buildCampusTree(Campus campus) {
        List<Campus> children = campusRepository.findByParentId(campus.getId());
        campus.setChildren(children);

        for (Campus child : children) {
            buildCampusTree(child);
        }
    }

    /**
     * 获取校区统计信息
     */
    public CampusStats getCampusStats(Long campusId) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + campusId + "' 不存在"));

        CampusStats stats = new CampusStats();
        stats.setCampusId(campusId);
        stats.setCampusName(campus.getName());

        // 统计用户数量
        stats.setUserCount(userRepository.countByCampusId(campusId));

        // 统计学员数量
        stats.setStudentCount(userRepository.countByCampusIdAndUserType(campusId, UserType.STUDENT));

        // 统计教练数量
        stats.setCoachCount(userRepository.countByCampusIdAndUserType(campusId, UserType.COACH));

        // 统计管理员数量
        stats.setAdminCount(userRepository.countByCampusIdAndUserTypeIn(
                campusId,
                List.of(UserType.SUPER_ADMIN, UserType.CAMPUS_ADMIN)
        ));

        // 统计子校区数量
        stats.setChildCampusCount(campusRepository.countByParentId(campusId));

        return stats;
    }


}