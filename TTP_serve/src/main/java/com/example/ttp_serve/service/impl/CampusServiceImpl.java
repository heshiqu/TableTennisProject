package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.CampusRequestDTO;
import com.example.ttp_serve.dto.CampusResponseDTO;
import com.example.ttp_serve.dto.CampusStatsDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final UserRepository userRepository;

    @Override
    public List<CampusResponseDTO> getAllCampuses() {
        List<Campus> campuses = campusRepository.findAll();
        return campuses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<CampusResponseDTO> getAllCampuses(Pageable pageable) {
        Page<Campus> campuses = campusRepository.findAll(pageable);
        return campuses.map(this::convertToDTO);
    }

    @Override
    public CampusResponseDTO getCampusById(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + id + "' 不存在"));
        return convertToDTO(campus);
    }

    @Override
    @Transactional
    public CampusResponseDTO createCampus(CampusRequestDTO campusRequestDTO) {
        // 检查校区名称是否已存在
        if (isCampusNameExists(campusRequestDTO.getName())) {
            throw new DuplicateResourceException("校区名称 '" + campusRequestDTO.getName() + "' 已存在");
        }

        Campus campus = new Campus();
        campus.setName(campusRequestDTO.getName());
        campus.setAddress(campusRequestDTO.getAddress());
        campus.setContactPerson(campusRequestDTO.getContactPerson());
        campus.setContactPhone(campusRequestDTO.getContactPhone());
        campus.setEmail(campusRequestDTO.getEmail());

        // 检查父校区是否存在（如果指定了父校区）
        if (campusRequestDTO.getParentId() != null) {
            Campus parentCampus = campusRepository.findById(campusRequestDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父校区不存在"));

            campus.setParent(parentCampus);

            // 检查是否形成循环引用
            if (isCircularReference(campus, parentCampus)) {
                throw new BusinessException("不能创建循环引用的校区关系");
            }
        }

        campus.setCreatedAt(LocalDateTime.now());
        campus.setUpdatedAt(LocalDateTime.now());

        Campus savedCampus = campusRepository.save(campus);
        return convertToDTO(savedCampus);
    }

    @Override
    @Transactional
    public CampusResponseDTO updateCampus(Long id, CampusRequestDTO campusRequestDTO) {
        Campus existingCampus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + id + "' 不存在"));

        // 检查校区名称是否与其他校区冲突
        if (campusRequestDTO.getName() != null && !campusRequestDTO.getName().equals(existingCampus.getName())) {
            if (isCampusNameExists(campusRequestDTO.getName(), id)) {
                throw new DuplicateResourceException("校区名称 '" + campusRequestDTO.getName() + "' 已存在");
            }
            existingCampus.setName(campusRequestDTO.getName());
        }

        // 更新地址信息
        if (campusRequestDTO.getAddress() != null) {
            existingCampus.setAddress(campusRequestDTO.getAddress());
        }

        // 更新联系人信息
        if (campusRequestDTO.getContactPerson() != null) {
            existingCampus.setContactPerson(campusRequestDTO.getContactPerson());
        }

        if (campusRequestDTO.getContactPhone() != null) {
            existingCampus.setContactPhone(campusRequestDTO.getContactPhone());
        }

        if (campusRequestDTO.getEmail() != null) {
            existingCampus.setEmail(campusRequestDTO.getEmail());
        }

        // 更新父校区关系
        if (campusRequestDTO.getParentId() != null) {
            Campus parentCampus = campusRepository.findById(campusRequestDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父校区不存在"));

            // 检查是否形成循环引用
            if (isCircularReference(existingCampus, parentCampus)) {
                throw new BusinessException("不能创建循环引用的校区关系");
            }

            existingCampus.setParent(parentCampus);
        } else {
            // 清除父校区关系
            existingCampus.setParent(null);
        }

        existingCampus.setUpdatedAt(LocalDateTime.now());

        Campus updatedCampus = campusRepository.save(existingCampus);
        return convertToDTO(updatedCampus);
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
        List<User> usersInCampus = page.getContent();
        if (!usersInCampus.isEmpty()) {
            throw new BusinessException("该校区下有用户，无法删除");
        }

        campusRepository.delete(campus);
    }

    @Override
    public List<CampusResponseDTO> getTopLevelCampuses() {
        List<Campus> campuses = campusRepository.findByParentIsNull();
        return campuses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampusResponseDTO> getChildCampuses(Long parentId) {
        List<Campus> campuses = campusRepository.findByParentId(parentId);
        return campuses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampusResponseDTO> searchCampusesByName(String name) {
        Optional<Campus> campusOpt = campusRepository.findByName(name);
        return campusOpt.map(campus -> Collections.singletonList(convertToDTO(campus)))
                .orElse(Collections.emptyList());
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
    public List<CampusResponseDTO> getCampusWithChildren(Long campusId) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + campusId + "' 不存在"));

        // 获取所有子校区（包括子校区的子校区）
        List<Campus> allChildren = getAllChildren(campus);
        return allChildren.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampusResponseDTO> getCampusTree() {
        List<Campus> topLevelCampuses = campusRepository.findByParentIsNull();
        return topLevelCampuses.stream().map(this::convertToTreeDTO).collect(Collectors.toList());
    }

    @Override
    public CampusStatsDTO getCampusStats(Long campusId) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + campusId + "' 不存在"));

        CampusStatsDTO stats = new CampusStatsDTO();
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

    /**
     * 将Campus实体转换为CampusResponseDTO
     */
    private CampusResponseDTO convertToDTO(Campus campus) {
        CampusResponseDTO dto = new CampusResponseDTO();
        dto.setId(campus.getId());
        dto.setName(campus.getName());
        dto.setAddress(campus.getAddress());
        dto.setContactPerson(campus.getContactPerson());
        dto.setContactPhone(campus.getContactPhone());
        dto.setEmail(campus.getEmail());
        dto.setCreatedAt(campus.getCreatedAt());
        dto.setUpdatedAt(campus.getUpdatedAt());

        if (campus.getParent() != null) {
            dto.setParentId(campus.getParent().getId());
            dto.setParentName(campus.getParent().getName());
        }

        return dto;
    }

    /**
     * 将Campus实体转换为树形结构的CampusResponseDTO
     */
    private CampusResponseDTO convertToTreeDTO(Campus campus) {
        CampusResponseDTO dto = convertToDTO(campus);

        // 递归转换子校区
        if (campus.getChildren() != null && !campus.getChildren().isEmpty()) {
            List<CampusResponseDTO> childrenDTOs = campus.getChildren().stream()
                    .map(this::convertToTreeDTO)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        }

        return dto;
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
     * 递归构建校区树形结构
     */
    private void buildCampusTree(Campus campus) {
        List<Campus> children = campusRepository.findByParentId(campus.getId());
        campus.setChildren(children);

        for (Campus child : children) {
            buildCampusTree(child);
        }
    }
}