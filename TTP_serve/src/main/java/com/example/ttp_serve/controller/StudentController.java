package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.dto.StudentDTO;
import com.example.ttp_serve.entity.Student;
import com.example.ttp_serve.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 学员管理控制器
 * 提供学员个人信息管理、余额查询等功能
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "学员管理", description = "提供学员个人信息管理、余额查询等功能")
public class StudentController {

    private final StudentService studentService;

    /**
     * 根据学生ID获取个人余额
     *
     * @param studentId 学生ID
     * @return 学生个人余额信息
     */
    @GetMapping("/{studentId}/balance")
    @Operation(summary = "获取学生个人余额", description = "根据学生ID获取学生的账户余额信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学生不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<BigDecimal>> getStudentBalance(
            @Parameter(description = "学生ID", required = true, example = "1")
            @PathVariable Long studentId) {
        try {
            BigDecimal balance = studentService.getStudentBalance(studentId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", balance));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据学生ID获取个人信息
     *
     * @param studentId 学生ID
     * @return 学生个人信息
     */
    @GetMapping("/{studentId}")
    @Operation(summary = "获取学生个人信息", description = "根据学生ID获取学生的详细个人信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学生不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<StudentDTO>> getStudentInfo(
            @Parameter(description = "学生ID", required = true, example = "1")
            @PathVariable Long studentId) {
        try {
            Student student = studentService.getStudentById(studentId);
            StudentDTO studentDTO = convertToDTO(student);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", studentDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将Student实体转换为StudentDTO
     *
     * @param student 学生实体
     * @return 学生DTO
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUsername(student.getUsername());
        dto.setRealName(student.getRealName());
        dto.setGender(student.getGender());
        dto.setAge(student.getAge());
        dto.setPhone(student.getPhone());
        dto.setEmail(student.getEmail());
        dto.setAvatar(student.getAvatar());
        dto.setUserType(student.getUserType());
        dto.setCampusId(student.getCampus() != null ? student.getCampus().getId() : null);
        dto.setCampusName(student.getCampus() != null ? student.getCampus().getName() : null);
        dto.setStatus(student.getStatus());
        dto.setBalance(student.getBalance());
        dto.setCancelCount(student.getCancelCount());
        dto.setLastCancelMonth(student.getLastCancelMonth());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());
        return dto;
    }
}