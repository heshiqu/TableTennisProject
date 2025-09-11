package com.example.ttp_serve.controller;

import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、信息维护、状态管理等接口")
public class UserController {

    private final UserService userService;

    /**
     * 根据ID获取用户信息
     * 用于查看用户详细信息，包括学员和教练员
     * 参考文档：学员和教练员均可对其基本信息进行维护
     */
    @Operation(summary = "根据ID获取用户信息", description = "通过用户ID查询用户详细信息，包括学员和教练员")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户信息",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有用户列表（不分页）
     * 用于校区管理员或超级管理员查看系统中所有用户
     * 参考文档：校区管理员可以对本校区的学员信息、教练员信息进行管理
     */
    @Operation(summary = "获取所有用户列表", description = "获取系统中所有用户的列表，不分页")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 分页获取所有用户列表
     * 用于前端分页展示用户信息
     */
    @Operation(summary = "分页获取用户列表", description = "分页获取系统中所有用户的列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/page")
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "分页参数", example = "{\"page\": 0, \"size\": 10, \"sort\": [\"username,asc\"]}")
            Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 创建新用户
     * 用于学员注册或教练员入职申请
     * 参考文档：
     * - 学员注册需提供用户名、密码、真实姓名、校区、电话等信息
     * - 教练员注册需额外提供照片和获奖信息，并需校区管理员审核
     * - 密码设置要求8-16位，必须包含字母、数字和特殊字符
     */
    @Operation(summary = "创建新用户", description = "创建新用户，可用于学员注册或教练员入职申请")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功创建用户",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效或用户名/手机号/邮箱已存在")
    })
    @PostMapping
    public ResponseEntity<User> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * 更新用户信息
     * 用于用户修改个人信息或管理员修改用户信息
     * 参考文档：学员和教练员均可对其基本信息进行维护，例如更换照片、修改密码等
     */
    @Operation(summary = "更新用户信息", description = "更新用户信息，可用于用户修改个人信息或管理员修改用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功更新用户信息",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更新的用户信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户（逻辑删除，设置为非活跃状态）
     * 用于校区管理员或超级管理员删除用户账号
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户，将用户状态设置为非活跃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "成功删除用户"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据关键词搜索用户
     * 用于按用户名或真实姓名搜索用户
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，可按用户名或真实姓名搜索")
    @ApiResponse(responseCode = "200", description = "成功搜索用户",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @Parameter(description = "搜索关键词", required = true, example = "张三")
            @RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    /**
     * 更新用户状态
     * 用于审核教练员注册申请或激活/停用用户账号
     * 参考文档：教练员需要校区管理员审核通过，方可注册成功
     */
    @Operation(summary = "更新用户状态", description = "更新用户状态，可用于审核教练员注册申请或激活/停用用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功更新用户状态",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "用户状态", required = true, example = "ACTIVE")
            @RequestParam UserStatus status) {
        User updatedUser = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 修改用户密码
     * 用于用户自行修改密码或管理员重置密码
     * 参考文档：密码设置要求8-16位，必须包含字母、数字和特殊字符
     */
    @Operation(summary = "修改用户密码", description = "修改用户密码，密码设置要求8-16位，必须包含字母、数字和特殊字符")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功修改密码",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "密码不符合要求")
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<User> changePassword(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "新密码", required = true, example = "NewPassword123!")
            @RequestParam String newPassword) {
        User updatedUser = userService.changePassword(id, newPassword);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 获取用户统计信息
     * 用于展示系统中各类用户的数量统计
     * 包括总用户数、活跃用户数、非活跃用户数、待审核用户数等
     */
    @Operation(summary = "获取用户统计信息", description = "获取系统中各类用户的数量统计")
    @ApiResponse(responseCode = "200", description = "成功获取用户统计信息",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserService.UserStats.class)))
    @GetMapping("/stats")
    public ResponseEntity<UserService.UserStats> getUserStats() {
        UserService.UserStats stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }
}