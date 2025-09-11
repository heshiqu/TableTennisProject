package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.exception.DuplicateResourceException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
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
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("用户名 '" + user.getUsername() + "' 已存在");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new DuplicateResourceException("手机号 '" + user.getPhone() + "' 已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateResourceException("邮箱 '" + user.getEmail() + "' 已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        // 设置创建和更新时间
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + id + "' 不存在"));

        // 检查用户名是否与其他用户冲突
        if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new DuplicateResourceException("用户名 '" + user.getUsername() + "' 已存在");
            }
            existingUser.setUsername(user.getUsername());
        }

        // 检查手机号是否与其他用户冲突
        if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.findByPhone(user.getPhone()).isPresent()) {
                throw new DuplicateResourceException("手机号 '" + user.getPhone() + "' 已存在");
            }
            existingUser.setPhone(user.getPhone());
        }

        // 检查邮箱是否与其他用户冲突
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new DuplicateResourceException("邮箱 '" + user.getEmail() + "' 已存在");
            }
            existingUser.setEmail(user.getEmail());
        }

        // 更新允许修改的字段
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }

        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }

        if (user.getAge() != null) {
            existingUser.setAge(user.getAge());
        }

        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }

        if (user.getUserType() != null) {
            existingUser.setUserType(user.getUserType());
        }

        if (user.getCampus() != null) {
            existingUser.setCampus(user.getCampus());
        }

        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
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


}