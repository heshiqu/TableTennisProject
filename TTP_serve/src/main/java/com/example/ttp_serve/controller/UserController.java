package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.dto.UserDTO;
import com.example.ttp_serve.dto.UserRequestDTO;
import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
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

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse<UserDTO>> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("用户ID '" + id + "' 不存在"));
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTO(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取所有用户列表（不分页）
     * 用于校区管理员或超级管理员查看系统中所有用户
     * 参考文档：校区管理员可以对本校区的学员信息、教练员信息进行管理
     */
    @Operation(summary = "获取所有用户列表", description = "获取系统中所有用户的列表，不分页")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @GetMapping
    public ResponseEntity<MyApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(users)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 分页获取所有用户列表
     * 用于前端分页展示用户信息
     */
    @Operation(summary = "分页获取用户列表", description = "分页获取系统中所有用户的列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/page")
    public ResponseEntity<MyApiResponse<Page<UserDTO>>> getAllUsers(
            @Parameter(description = "分页参数", example = "{\"page\": 0, \"size\": 10, \"sort\": [\"username,asc\"]}")
            Pageable pageable) {
        try {
            Page<User> users = userService.getAllUsers(pageable);
            Page<UserDTO> userDTOs = users.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", userDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效或用户名/手机号/邮箱已存在")
    })
    @PostMapping
    public ResponseEntity<MyApiResponse<UserDTO>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequestDTO.class))
            )
            @Valid @RequestBody UserRequestDTO userRequest) {
        try {
            User createdUser = userService.createUserFromRequest(userRequest);
            return ResponseEntity.ok(MyApiResponse.success("用户创建成功", convertToDTO(createdUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     * 用于用户修改个人信息或管理员修改用户信息
     * 参考文档：学员和教练员均可对其基本信息进行维护，例如更换照片、修改密码等
     */
    @Operation(summary = "更新用户信息", description = "更新用户信息，可用于用户修改个人信息或管理员修改用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功更新用户信息",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MyApiResponse<UserDTO>> updateUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更新的用户信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequestDTO.class))
            )
            @Valid @RequestBody UserRequestDTO userRequest) {
        try {
            User updatedUser = userService.updateUserFromRequest(id, userRequest);
            return ResponseEntity.ok(MyApiResponse.success("用户更新成功", convertToDTO(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
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
    public ResponseEntity<MyApiResponse<Void>> deleteUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(MyApiResponse.success("用户删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据关键词搜索用户
     * 用于按用户名或真实姓名搜索用户
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，可按用户名或真实姓名搜索")
    @ApiResponse(responseCode = "200", description = "成功搜索用户",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/search")
    public ResponseEntity<MyApiResponse<List<UserDTO>>> searchUsers(
            @Parameter(description = "搜索关键词", required = true, example = "张三")
            @RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            return ResponseEntity.ok(MyApiResponse.success("搜索成功", convertToDTOList(users)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     * 用于审核教练员注册申请或激活/停用用户账号
     * 参考文档：教练员需要校区管理员审核通过，方可注册成功
     */
    @Operation(summary = "更新用户状态", description = "更新用户状态，可用于审核教练员注册申请或激活/停用用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功更新用户状态",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<MyApiResponse<UserDTO>> updateUserStatus(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "用户状态", required = true, example = "ACTIVE")
            @RequestParam UserStatus status) {
        try {
            User updatedUser = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(MyApiResponse.success("状态更新成功", convertToDTO(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 修改用户密码
     * 用于用户自行修改密码，需要先验证原始密码
     * 参考文档：密码设置要求8-16位，必须包含字母、数字和特殊字符
     */
    @Operation(summary = "修改用户密码", description = "修改用户密码，需要先验证原始密码，密码设置要求8-16位，必须包含字母、数字和特殊字符")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功修改密码",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "原始密码错误或新密码不符合要求")
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<MyApiResponse<UserDTO>> changePassword(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "原始密码", required = true, example = "OldPassword123!")
            @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true, example = "NewPassword123!")
            @RequestParam String newPassword) {
        try {
            User updatedUser = userService.changePasswordWithOldPassword(id, oldPassword, newPassword);
            return ResponseEntity.ok(MyApiResponse.success("密码修改成功", convertToDTO(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
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
    public ResponseEntity<MyApiResponse<UserService.UserStats>> getUserStats() {
        try {
            UserService.UserStats stats = userService.getUserStats();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据用户类型分页获取用户列表
     */
    @Operation(summary = "根据用户类型分页获取用户列表", description = "根据用户类型分页获取用户列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/type/{userType}/page")
    public ResponseEntity<MyApiResponse<Page<UserDTO>>> getUsersByType(
            @Parameter(description = "用户类型", required = true, example = "STUDENT")
            @PathVariable UserType userType,
            @Parameter(description = "分页参数", example = "{\"page\": 0, \"size\": 10, \"sort\": [\"username,asc\"]}")
            Pageable pageable) {
        try {
            Page<User> users = userService.getUsersByType(userType, pageable);
            Page<UserDTO> userDTOs = users.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", userDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据校区ID获取用户列表
     */
    @Operation(summary = "根据校区ID获取用户列表", description = "根据校区ID获取用户列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/campus/{campusId}")
    public ResponseEntity<MyApiResponse<List<UserDTO>>> getUsersByCampusId(
            @Parameter(description = "校区ID", required = true, example = "1")
            @PathVariable Long campusId) {
        try {
            List<User> users = userService.getUsersByCampusId(campusId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(users)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学生总数
     * 用于获取系统中所有学生用户的总数
     */
    @Operation(summary = "获取学生总数", description = "获取系统中所有学生用户的总数")
    @ApiResponse(responseCode = "200", description = "成功获取学生总数",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "150")))
    @GetMapping("/count/students")
    public ResponseEntity<MyApiResponse<Long>> getTotalStudentCount() {
        try {
            Long count = userService.countUsersByType(UserType.STUDENT);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据校区ID获取学生数量
     * 用于获取指定校区内的学生用户数量
     */
    @Operation(summary = "根据校区ID获取学生数量", description = "获取指定校区内的学生用户数量")
    @ApiResponse(responseCode = "200", description = "成功获取校区学生数量",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", example = "45")))
    @GetMapping("/count/students/campus/{campusId}")
    public ResponseEntity<MyApiResponse<Long>> getStudentCountByCampus(
            @Parameter(description = "校区ID", required = true, example = "1")
            @PathVariable Long campusId) {
        try {
            Long count = userService.countUsersByCampusIdAndType(campusId, UserType.STUDENT);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 分页获取校区管理员列表
     * 用于获取系统中所有校区管理员用户的分页列表
     */
    @Operation(summary = "分页获取校区管理员列表", description = "分页获取系统中所有校区管理员用户的列表")
    @ApiResponse(responseCode = "200", description = "成功获取校区管理员列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/admins/page")
    public ResponseEntity<MyApiResponse<Page<UserDTO>>> getCampusAdmins(
            @Parameter(description = "分页参数", example = "{\"page\": 0, \"size\": 10, \"sort\": [\"username,asc\"]}")
            Pageable pageable) {
        try {
            Page<User> admins = userService.getUsersByType(UserType.CAMPUS_ADMIN, pageable);
            Page<UserDTO> adminDTOs = admins.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", adminDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据校区ID分页获取校区管理员列表
     * 用于获取指定校区内的校区管理员用户的分页列表
     */
    @Operation(summary = "根据校区ID分页获取校区管理员列表", description = "获取指定校区内的校区管理员用户的分页列表")
    @ApiResponse(responseCode = "200", description = "成功获取校区管理员列表",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/admins/campus/{campusId}/page")
    public ResponseEntity<MyApiResponse<Page<UserDTO>>> getCampusAdminsByCampus(
            @Parameter(description = "校区ID", required = true, example = "1")
            @PathVariable Long campusId,
            @Parameter(description = "分页参数", example = "{\"page\": 0, \"size\": 10, \"sort\": [\"username,asc\"]}")
            Pageable pageable) {
        try {
            Page<User> admins = userService.getUsersByTypeAndCampusId(UserType.CAMPUS_ADMIN, campusId, pageable);
            Page<UserDTO> adminDTOs = admins.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", adminDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
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

        if (user.getCampus() != null) {
            dto.setCampusId(user.getCampus().getId());
            dto.setCampusName(user.getCampus().getName());
        }

        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    /**
     * 将User实体列表转换为UserDTO列表
     */
    private List<UserDTO> convertToDTOList(List<User> users) {
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}