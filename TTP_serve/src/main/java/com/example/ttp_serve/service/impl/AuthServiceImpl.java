package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.LoginRequest;
import com.example.ttp_serve.dto.RegisterRequest;
import com.example.ttp_serve.entity.Campus;
import com.example.ttp_serve.entity.Coach;
import com.example.ttp_serve.entity.Student;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.exception.DuplicateResourceException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.CoachRepository;
import com.example.ttp_serve.repository.StudentRepository;
import com.example.ttp_serve.repository.UserRepository;
import com.example.ttp_serve.repository.CampusRepository;
import com.example.ttp_serve.service.AuthService;
import com.example.ttp_serve.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampusRepository campusRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 存储已注销的令牌（在实际应用中，应使用Redis等分布式缓存）
    private final Map<String, Long> revokedTokens = new HashMap<>();


    @Override
    @Transactional
    public String login(LoginRequest loginRequest) {
        try {
            // 验证用户状态
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户名或密码错误");
            }
            User user = userOpt.get();
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new RuntimeException("用户账号未激活或已被禁用");
            }
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 生成JWT令牌
            return jwtUtil.generateToken(authentication);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("用户名或密码错误", e);
        }
    }

    @Override
    @Transactional
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("用户名 '" + registerRequest.getUsername() + "' 已存在");
        }

        // 检查手机号是否已存在
        if (registerRequest.getPhone() != null &&
                userRepository.findByPhone(registerRequest.getPhone()).isPresent()) {
            throw new DuplicateResourceException("手机号 '" + registerRequest.getPhone() + "' 已存在");
        }

        // 检查邮箱是否已存在
        if (registerRequest.getEmail() != null &&
                userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("邮箱 '" + registerRequest.getEmail() + "' 已存在");
        }

        User user;
        // 根据用户类型创建相应的实体对象
        if (registerRequest.getUserType() == UserType.STUDENT) {
            user = new Student();
            // 设置Student特有属性
            if (registerRequest.getInitialBalance() != null) {
                ((Student) user).setBalance(registerRequest.getInitialBalance());
            } else {
                ((Student) user).setBalance(BigDecimal.ZERO);
            }
            ((Student) user).setCancelCount(0);
            ((Student) user).setLastCancelMonth(null);
        } else if (registerRequest.getUserType() == UserType.COACH) {
            user = new Coach();
            // 设置Coach特有属性
            if (registerRequest.getLevel() != null) {
                ((Coach) user).setLevel(registerRequest.getLevel());
            } else {
                ((Coach) user).setLevel(CoachLevel.JUNIOR);
            }
            ((Coach) user).setAwards(registerRequest.getAwards());
            // 设置收费标准
            switch (((Coach) user).getLevel()) {
                case SENIOR:
                    ((Coach) user).setHourlyRate(new BigDecimal("200.00"));
                    break;
                case INTERMEDIATE:
                    ((Coach) user).setHourlyRate(new BigDecimal("150.00"));
                    break;
                case JUNIOR:
                default:
                    ((Coach) user).setHourlyRate(new BigDecimal("80.00"));
                    break;
            }
            ((Coach) user).setMaxStudents(20);
            ((Coach) user).setCurrentStudents(0);
        } else {
            user = new User();
        }

        // 设置公共属性
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRealName(registerRequest.getRealName());
        user.setGender(registerRequest.getGender());
        user.setAge(registerRequest.getAge());
        user.setPhone(registerRequest.getPhone());
        user.setEmail(registerRequest.getEmail());
        user.setAvatar(registerRequest.getAvatar());
        user.setUserType(registerRequest.getUserType());

        // 设置校区信息
        if (registerRequest.getCampusId() != null) {
            Campus campus = campusRepository.findById(registerRequest.getCampusId())
                    .orElseThrow(() -> new ResourceNotFoundException("校区不存在"));
            user.setCampus(campus);
        }

        // 设置用户状态
        if (registerRequest.getUserType() == UserType.COACH) {
            user.setStatus(UserStatus.PENDING);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存用户
        User savedUser = userRepository.save(user);

        return savedUser;
    }


    @Override
    public String refreshToken(String token) {
        // 验证令牌是否有效
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的令牌");
        }

        // 检查令牌是否已被注销
        if (revokedTokens.containsKey(token)) {
            throw new RuntimeException("令牌已失效");
        }

        // 从令牌中提取用户名
        String username = jwtUtil.getUsernameFromToken(token);

        // 加载用户信息 - 使用 userDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 创建新的认证信息
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // 生成新令牌
        return jwtUtil.generateToken(authentication);
    }

    @Override
    public void logout(String token) {
        // 从令牌中提取用户名
        String username = jwtUtil.getUsernameFromToken(token);

        // 查找用户ID
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            // 将令牌加入注销列表（设置有效期为令牌剩余时间）
            long expiration = jwtUtil.getExpirationFromToken(token);
            long currentTime = System.currentTimeMillis() / 1000;
            long ttl = expiration - currentTime;

            if (ttl > 0) {
                revokedTokens.put(token, expiration);
            }
        }

        // 清除安全上下文
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean validateToken(String token) {
        // 检查令牌是否已被注销
        if (revokedTokens.containsKey(token)) {
            return false;
        }

        return jwtUtil.validateToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("无效的令牌");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return user.getId();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }

        return null;
    }

    @Override
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    @Override
    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    @Override
    public boolean isSuperAdmin() {
        return hasAuthority("ROLE_SUPER_ADMIN");
    }

    @Override
    public boolean isCampusAdmin() {
        return hasAuthority("ROLE_CAMPUS_ADMIN");
    }

    @Override
    public boolean isCoach() {
        return hasAuthority("ROLE_COACH");
    }

    @Override
    public boolean isStudent() {
        return hasAuthority("ROLE_STUDENT");
    }

    /**
     * 获取用户权限
     */
    private String[] getAuthorities(User user) {
        switch (user.getUserType()) {
            case SUPER_ADMIN:
                return new String[]{"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_USER"};
            case CAMPUS_ADMIN:
                return new String[]{"ROLE_CAMPUS_ADMIN", "ROLE_ADMIN", "ROLE_USER"};
            case COACH:
                return new String[]{"ROLE_COACH", "ROLE_USER"};
            case STUDENT:
                return new String[]{"ROLE_STUDENT", "ROLE_USER"};
            default:
                return new String[]{"ROLE_USER"};
        }
    }

    /**
     * 清理过期的注销令牌
     */
    public void cleanupRevokedTokens() {
        long currentTime = System.currentTimeMillis() / 1000;
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
}