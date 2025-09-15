package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.dto.JwtResponse;
import com.example.ttp_serve.dto.LoginRequest;
import com.example.ttp_serve.dto.RegisterRequest;
import com.example.ttp_serve.dto.UserDTO;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserType;
import com.example.ttp_serve.service.AuthService;
import com.example.ttp_serve.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证管理控制器
 * 提供用户登录、注册、令牌刷新和验证等功能
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "提供用户登录、注册、令牌刷新和验证等功能")
public class AuthController {

    private final AuthService authService;
    private final CourseService courseService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求体，包含用户名和密码
     * @return JWT令牌和用户信息
     *
     * @apiNote 用户使用用户名和密码进行登录，成功返回JWT令牌
     *          令牌需要在后续请求的Authorization头中携带：Bearer {token}
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码进行登录，成功返回JWT令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<JwtResponse>> login(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {

        try {
            String token = authService.login(loginRequest);
            // 获取用户信息
            User user = authService.getCurrentUser();
            JwtResponse response = new JwtResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getUserType(),
                    user.getCampus() != null ? user.getCampus().getId() : null
            );

            // 如果是教练或学生，检查并更新已结束的课程状态
            if (user.getUserType() == UserType.COACH || user.getUserType() == UserType.STUDENT) {
                try {
                    courseService.completeExpiredCourses(user.getId());
                } catch (Exception e) {
                    // 记录日志但不影响登录流程
                    System.err.println("更新课程状态时发生错误: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(MyApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求体，包含用户基本信息
     * @return 注册成功的用户信息
     *
     * @apiNote 用户可以注册为学员或教练
     *          学员注册后直接激活，教练注册后需要管理员审核
     *          密码必须包含字母、数字和特殊字符，长度为8-16位
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户可以注册为学员或教练，学员注册后直接激活，教练注册后需要管理员审核")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "注册失败，参数错误或用户已存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<UserDTO>> register(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(registerRequest);
            UserDTO userDTO = convertToUserDTO(user);

            String message = "注册成功";
            if (user.getUserType().equals(UserType.COACH)) {
                message = "教练注册申请已提交，等待管理员审核";
            }

            return ResponseEntity.ok(MyApiResponse.success(message, userDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 学员注册
     *
     * @param registerRequest 注册请求体，包含学员基本信息
     * @return 注册成功的学员信息
     *
     * @apiNote 专门用于学员注册的接口，设置用户类型为STUDENT
     */
    @PostMapping("/register/student")
    @Operation(summary = "学员注册", description = "专门用于学员注册的接口，设置用户类型为STUDENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "注册失败，参数错误或用户已存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<UserDTO>> registerStudent(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        registerRequest.setUserType(UserType.STUDENT);
        return register(registerRequest);
    }

    /**
     * 教练注册
     *
     * @param registerRequest 注册请求体，包含教练基本信息
     * @return 注册成功的教练信息
     *
     * @apiNote 专门用于教练注册的接口，设置用户类型为COACH
     *          教练注册后需要管理员审核才能激活
     */
    @PostMapping("/register/coach")
    @Operation(summary = "教练注册", description = "专门用于教练注册的接口，设置用户类型为COACH，需要管理员审核")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册申请已提交，等待审核"),
            @ApiResponse(responseCode = "400", description = "注册失败，参数错误或用户已存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<UserDTO>> registerCoach(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        registerRequest.setUserType(UserType.COACH);
        return register(registerRequest);
    }

    /**
     * 刷新令牌
     *
     * @param request HTTP请求，用于获取Authorization头
     * @return 新的JWT令牌
     *
     * @apiNote 使用有效的JWT令牌获取新的令牌，延长会话时间
     *          需要在Authorization头中携带当前有效的令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用有效的JWT令牌获取新的令牌，延长会话时间")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "令牌刷新成功"),
            @ApiResponse(responseCode = "400", description = "令牌刷新失败"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<String>> refreshToken(
            @Parameter(description = "HTTP请求头，包含Authorization令牌") HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "未提供令牌"));
            }

            String newToken = authService.refreshToken(token);
            return ResponseEntity.ok(MyApiResponse.success("令牌刷新成功", newToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 用户注销
     *
     * @param request HTTP请求，用于获取Authorization头
     * @return 操作结果
     *
     * @apiNote 用户注销登录，使当前令牌失效
     *          需要在Authorization头中携带当前有效的令牌
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "用户注销登录，使当前令牌失效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注销成功"),
            @ApiResponse(responseCode = "400", description = "注销失败"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Void>> logout(
            @Parameter(description = "HTTP请求头，包含Authorization令牌") HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "未提供令牌"));
            }

            authService.logout(token);
            return ResponseEntity.ok(MyApiResponse.success("注销成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 验证令牌
     *
     * @param request HTTP请求，用于获取Authorization头
     * @return 令牌是否有效
     *
     * @apiNote 验证JWT令牌是否有效且未过期
     *          需要在Authorization头中携带要验证的令牌
     */
    @GetMapping("/validate-token")
    @Operation(summary = "验证令牌", description = "验证JWT令牌是否有效且未过期")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "令牌验证成功"),
            @ApiResponse(responseCode = "400", description = "令牌验证失败"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> validateToken(
            @Parameter(description = "HTTP请求头，包含Authorization令牌") HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(MyApiResponse.error(400, "未提供令牌"));
            }

            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(MyApiResponse.success("令牌验证成功", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前认证用户的信息
     *
     * @apiNote 获取当前登录用户的详细信息
     *          需要在Authorization头中携带有效的JWT令牌
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<UserDTO>> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(MyApiResponse.error(401, "未认证"));
            }

            UserDTO userDTO = convertToUserDTO(user);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", userDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查当前用户权限
     *
     * @param authority 要检查的权限
     * @return 是否拥有该权限
     *
     * @apiNote 检查当前用户是否拥有指定的权限
     *          需要在Authorization头中携带有效的JWT令牌
     */
    @GetMapping("/check-authority")
    @Operation(summary = "检查用户权限", description = "检查当前用户是否拥有指定的权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "400", description = "检查失败"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> checkAuthority(
            @Parameter(description = "权限名称", required = true) @RequestParam String authority) {
        try {
            boolean hasAuthority = authService.hasAuthority(authority);
            return ResponseEntity.ok(MyApiResponse.success("检查成功", hasAuthority));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查当前用户角色
     *
     * @return 用户角色信息
     *
     * @apiNote 检查当前用户的角色类型
     *          需要在Authorization头中携带有效的JWT令牌
     */
    @GetMapping("/check-role")
    @Operation(summary = "检查用户角色", description = "检查当前用户的角色类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "400", description = "检查失败"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<String>> checkRole() {
        try {
            if (authService.isSuperAdmin()) {
                return ResponseEntity.ok(MyApiResponse.success("角色检查成功", "SUPER_ADMIN"));
            } else if (authService.isCampusAdmin()) {
                return ResponseEntity.ok(MyApiResponse.success("角色检查成功", "CAMPUS_ADMIN"));
            } else if (authService.isCoach()) {
                return ResponseEntity.ok(MyApiResponse.success("角色检查成功", "COACH"));
            } else if (authService.isStudent()) {
                return ResponseEntity.ok(MyApiResponse.success("角色检查成功", "STUDENT"));
            } else {
                return ResponseEntity.ok(MyApiResponse.success("角色检查成功", "USER"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 从HTTP请求中提取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setUserType(user.getUserType());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // 设置校区信息
        if (user.getCampus() != null) {
            dto.setCampusId(user.getCampus().getId());
            dto.setCampusName(user.getCampus().getName());
        }

        // 根据用户类型设置特有字段
        if (user.getUserType() == UserType.COACH) {
            // 如果是教练，设置教练特有字段
            if (user instanceof com.example.ttp_serve.entity.Coach) {
                com.example.ttp_serve.entity.Coach coach = (com.example.ttp_serve.entity.Coach) user;
                dto.setLevel(coach.getLevel() != null ? coach.getLevel().name() : null);
                dto.setAwards(coach.getAwards());
                dto.setHourlyRate(coach.getHourlyRate() != null ? coach.getHourlyRate().toString() : null);
            }
        } else if (user.getUserType() == UserType.STUDENT) {
            // 如果是学员，设置学员特有字段
            if (user instanceof com.example.ttp_serve.entity.Student) {
                com.example.ttp_serve.entity.Student student = (com.example.ttp_serve.entity.Student) user;
                dto.setBalance(student.getBalance() != null ? student.getBalance().toString() : null);
                dto.setCancelCount(student.getCancelCount());
            }
        }

        return dto;
    }
}