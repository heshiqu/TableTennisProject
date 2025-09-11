package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.LoginRequest;
import com.example.ttp_serve.dto.RegisterRequest;
import com.example.ttp_serve.entity.User;

public interface AuthService{

    // 用户登录
    String login(LoginRequest loginRequest);

    // 用户注册
    User register(RegisterRequest registerRequest);

    // 刷新令牌
    String refreshToken(String token);

    // 注销
    void logout(String token);

    // 验证令牌
    boolean validateToken(String token);

    // 获取用户ID从令牌
    Long getUserIdFromToken(String token);

    // 获取当前认证用户
    User getCurrentUser();

    // 获取当前用户ID
    Long getCurrentUserId();

    // 检查当前用户是否有指定权限
    boolean hasAuthority(String authority);

    // 检查当前用户是否是超级管理员
    boolean isSuperAdmin();

    // 检查当前用户是否是校区管理员
    boolean isCampusAdmin();

    // 检查当前用户是否是教练
    boolean isCoach();

    // 检查当前用户是否是学员
    boolean isStudent();
}