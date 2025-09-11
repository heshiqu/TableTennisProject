package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    // 根据ID获取用户
    Optional<User> getUserById(Long id);

    // 根据用户名获取用户
    Optional<User> getUserByUsername(String username);

    // 获取所有用户
    List<User> getAllUsers();

    // 分页获取所有用户
    Page<User> getAllUsers(Pageable pageable);

    // 创建用户
    User createUser(User user);

    // 更新用户
    User updateUser(Long id, User user);

    // 删除用户
    void deleteUser(Long id);

    // 根据用户类型获取用户
    List<User> getUsersByType(UserType userType);

    // 根据校区ID获取用户
    List<User> getUsersByCampusId(Long campusId);

    // 根据用户类型和校区ID获取用户
    List<User> getUsersByTypeAndCampusId(UserType userType, Long campusId);

    // 根据关键字搜索用户
    List<User> searchUsers(String keyword);

    // 更新用户状态
    User updateUserStatus(Long id, UserStatus status);

    // 统计用户数量
    Long countUsers();

    // 统计校区用户数量
    Long countUsersByCampusId(Long campusId);

    // 统计特定类型用户数量
    Long countUsersByType(UserType userType);

    // 验证用户凭证
    boolean validateUserCredentials(String username, String password);

    // 更改密码
    User changePassword(Long id, String newPassword);

    // 检查用户名是否已存在
    boolean isUsernameExists(String username);

    // 检查手机号是否已存在
    boolean isPhoneExists(String phone);

    // 检查邮箱是否已存在
    boolean isEmailExists(String email);

    // 根据手机号查找用户
    Optional<User> getUserByPhone(String phone);

    // 根据邮箱查找用户
    Optional<User> getUserByEmail(String email);

    // 批量更新用户状态
    int batchUpdateUserStatus(List<Long> userIds, UserStatus status);

    // 获取活跃用户
    List<User> getActiveUsers();

    // 获取非活跃用户
    List<User> getInactiveUsers();

    // 获取待审核用户
    List<User> getPendingUsers();

    // 内部类：用户统计信息
    public static class UserStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long pendingUsers;
        private Map<UserType, Long> usersByType = new EnumMap<>(UserType.class);

        // 构造函数、getter和setter方法
        public UserStats() {}

        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }

        public Long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(Long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

        public Long getPendingUsers() { return pendingUsers; }
        public void setPendingUsers(Long pendingUsers) { this.pendingUsers = pendingUsers; }

        public Map<UserType, Long> getUsersByType() { return usersByType; }
        public void setUsersByType(Map<UserType, Long> usersByType) { this.usersByType = usersByType; }
    }

    public UserStats getUserStats();
}