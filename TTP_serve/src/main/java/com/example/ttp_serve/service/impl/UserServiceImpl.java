package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.UserRequestDTO;
import com.example.ttp_serve.entity.Campus;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.exception.DuplicateResourceException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.CampusRepository;
import com.example.ttp_serve.repository.UserRepository;
import com.example.ttp_serve.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampusRepository campusRepository;

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User createUserFromRequest(UserRequestDTO userRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException("用户名 '" + userRequest.getUsername() + "' 已存在");
        }

        // 检查手机号是否已存在
        if (userRequest.getPhone() != null && userRepository.findByPhone(userRequest.getPhone()).isPresent()) {
            throw new DuplicateResourceException("手机号 '" + userRequest.getPhone() + "' 已存在");
        }

        // 检查邮箱是否已存在
        if (userRequest.getEmail() != null && userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("邮箱 '" + userRequest.getEmail() + "' 已存在");
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRealName(userRequest.getRealName());
        user.setGender(userRequest.getGender());
        user.setAge(userRequest.getAge());
        user.setPhone(userRequest.getPhone());
        user.setEmail(userRequest.getEmail());
        user.setAvatar(userRequest.getAvatar());
        user.setUserType(userRequest.getUserType());

        // 设置校区
        if (userRequest.getCampusId() != null) {
            Campus campus = campusRepository.findById(userRequest.getCampusId())
                    .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + userRequest.getCampusId() + "' 不存在"));
            user.setCampus(campus);
        }

        // 设置默认状态
        if (userRequest.getStatus() != null) {
            user.setStatus(userRequest.getStatus());
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        // 设置创建和更新时间
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserFromRequest(Long id, UserRequestDTO userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        // 检查用户名是否与其他用户冲突
        if (userRequest.getUsername() != null && !userRequest.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(userRequest.getUsername())) {
                throw new DuplicateResourceException("用户名 '" + userRequest.getUsername() + "' 已存在");
            }
            existingUser.setUsername(userRequest.getUsername());
        }

        // 检查手机号是否与其他用户冲突
        if (userRequest.getPhone() != null && !userRequest.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.findByPhone(userRequest.getPhone()).isPresent()) {
                throw new DuplicateResourceException("手机号 '" + userRequest.getPhone() + "' 已存在");
            }
            existingUser.setPhone(userRequest.getPhone());
        }

        // 检查邮箱是否与其他用户冲突
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                throw new DuplicateResourceException("邮箱 '" + userRequest.getEmail() + "' 已存在");
            }
            existingUser.setEmail(userRequest.getEmail());
        }

        // 更新允许修改的字段
        if (userRequest.getRealName() != null) {
            existingUser.setRealName(userRequest.getRealName());
        }

        if (userRequest.getGender() != null) {
            existingUser.setGender(userRequest.getGender());
        }

        if (userRequest.getAge() != null) {
            existingUser.setAge(userRequest.getAge());
        }

        if (userRequest.getAvatar() != null) {
            existingUser.setAvatar(userRequest.getAvatar());
        }

        if (userRequest.getUserType() != null) {
            existingUser.setUserType(userRequest.getUserType());
        }

        if (userRequest.getCampusId() != null) {
            Campus campus = campusRepository.findById(userRequest.getCampusId())
                    .orElseThrow(() -> new ResourceNotFoundException("校区ID '" + userRequest.getCampusId() + "' 不存在"));
            existingUser.setCampus(campus);
        }

        if (userRequest.getStatus() != null) {
            existingUser.setStatus(userRequest.getStatus());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    // 原有方法保持不变，但需要将createUser和updateUser方法标记为过时或删除
    @Override
    @Transactional
    @Deprecated
    public User createUser(User user) {
        // 保持原有实现，但标记为过时
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setRealName(user.getRealName());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setUserType(user.getUserType());
        if (user.getCampus() != null) {
            dto.setCampusId(user.getCampus().getId());
        }
        dto.setStatus(user.getStatus());

        return createUserFromRequest(dto);
    }

    @Override
    @Transactional
    @Deprecated
    public User updateUser(Long id, User user) {
        // 保持原有实现，但标记为过时
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setRealName(user.getRealName());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setUserType(user.getUserType());
        if (user.getCampus() != null) {
            dto.setCampusId(user.getCampus().getId());
        }
        dto.setStatus(user.getStatus());

        return updateUserFromRequest(id, dto);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        // 逻辑删除，设置状态为INACTIVE
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public List<User> getUsersByType(UserType userType) {
        // 使用 Pageable.unpaged() 获取所有数据
        Page<User> page = userRepository.findByUserType(userType, Pageable.unpaged());
        return page.getContent(); // 提取 List<User>
    }

    @Override
    public List<User> getUsersByCampusId(Long campusId) {
        Page<User> page = userRepository.findByCampusId(campusId, Pageable.unpaged());
        return page.getContent();
    }

    @Override
    public List<User> getUsersByTypeAndCampusId(UserType userType, Long campusId) {
        return userRepository.findByUserTypeAndCampusId(userType, campusId);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameOrRealNameContaining(keyword);
    }

    @Override
    @Transactional
    public User updateUserStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public Long countUsers() {
        return userRepository.count();
    }

    @Override
    public Long countUsersByCampusId(Long campusId) {
        return userRepository.countByCampusId(campusId);
    }

    @Override
    public Long countUsersByType(UserType userType) {
        return userRepository.countByUserType(userType);
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public User changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        // 验证密码强度（可以根据需要添加更复杂的验证）
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("密码长度至少需要8个字符");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User changePasswordWithOldPassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        // 验证原始密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原始密码错误");
        }

        // 验证密码强度（可以根据需要添加更复杂的验证）
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("密码长度至少需要8个字符");
        }

        // 验证密码复杂度（包含字母、数字和特殊字符）
        String pattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(pattern)) {
            throw new IllegalArgumentException("密码必须包含字母、数字和特殊字符，长度为8-16位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public int batchUpdateUserStatus(List<Long> userIds, UserStatus status) {
        int updatedCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + userId + "' 不存在"));

                user.setStatus(status);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                updatedCount++;
            } catch (ResourceNotFoundException e) {
                // 记录日志或跳过不存在的用户
                // 可以根据需要决定是继续处理还是抛出异常
            }
        }

        return updatedCount;
    }

    @Override
    public List<User> getActiveUsers() {
        return userRepository.findByUserTypeAndStatus(UserType.STUDENT, UserStatus.ACTIVE);
    }

    @Override
    public List<User> getInactiveUsers() {
        return userRepository.findByUserTypeAndStatus(UserType.STUDENT, UserStatus.INACTIVE);
    }

    @Override
    public List<User> getPendingUsers() {
        return userRepository.findByUserTypeAndStatus(UserType.STUDENT, UserStatus.PENDING);
    }

    // 辅助方法：验证密码强度
    private boolean isPasswordStrong(String password) {
        // 至少8个字符，包含字母、数字和特殊字符
        String pattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }

    // 辅助方法：生成随机密码
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    // 辅助方法：重置用户密码
    @Transactional
    public User resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 在实际应用中，这里应该发送包含新密码的邮件或短信给用户
        // emailService.sendPasswordResetEmail(user.getEmail(), newPassword);

        return savedUser;
    }

    // 辅助方法：根据用户类型和状态获取用户数量统计
    public UserStats getUserStats() {
        UserStats stats = new UserStats();

        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByStatus(UserStatus.ACTIVE));
        stats.setInactiveUsers(userRepository.countByStatus(UserStatus.INACTIVE));
        stats.setPendingUsers(userRepository.countByStatus(UserStatus.PENDING));

        for (UserType type : UserType.values()) {
            stats.getUsersByType().put(type, userRepository.countByUserType(type));
        }

        return stats;
    }

    @Override
    public Long countUsersByCampusIdAndType(Long campusId, UserType userType) {
        return userRepository.countByCampusIdAndUserType(campusId, userType);
    }

    @Override
    public Page<User> getUsersByType(UserType userType, Pageable pageable) {
        return userRepository.findByUserType(userType, pageable);
    }

    @Override
    public Page<User> getUsersByTypeAndCampusId(UserType userType, Long campusId, Pageable pageable) {
        return userRepository.findByUserTypeAndCampusId(userType, campusId, pageable);
    }


}