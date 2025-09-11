package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.dto.SystemLogDto;
import com.example.ttp_serve.entity.SystemLog;
import com.example.ttp_serve.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统日志管理控制器
 * 提供系统日志的创建、查询和统计等功能
 */
@RestController
@RequestMapping("/api/system-logs")
@Tag(name = "系统日志管理", description = "提供系统日志的创建、查询和统计等功能")
public class SystemLogController {

    private final SystemLogService systemLogService;

    @Autowired
    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * 创建系统日志
     * @param systemLog 日志实体
     * @return 创建后的日志信息
     */
    @Operation(
            summary = "创建系统日志",
            description = "创建一条系统日志记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "日志创建成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping
    public ResponseEntity<MyApiResponse<SystemLogDto>> createLog(
            @Parameter(description = "日志实体", required = true) @RequestBody SystemLog systemLog) {
        try {
            SystemLog createdLog = systemLogService.createLog(systemLog);
            return ResponseEntity.ok(MyApiResponse.success("日志创建成功", convertToDto(createdLog)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据ID获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    @Operation(
            summary = "根据ID获取日志详情",
            description = "根据日志ID获取日志的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "日志不存在")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse<SystemLogDto>> getLogById(
            @Parameter(description = "日志ID", required = true) @PathVariable Long id) {
        try {
            SystemLog log = systemLogService.getLog(id);
            if (log == null) {
                return ResponseEntity.ok(MyApiResponse.notFound("日志不存在"));
            }
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDto(log)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据用户ID获取日志列表
     * @param userId 用户ID
     * @return 用户相关的日志列表
     */
    @Operation(
            summary = "根据用户ID获取日志列表",
            description = "获取指定用户的所有系统日志记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<MyApiResponse<List<SystemLogDto>>> getLogsByUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            List<SystemLog> logs = systemLogService.getLogsByUser(userId);
            List<SystemLogDto> logDtos = logs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(MyApiResponse.success("获取成功", logDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据模块名称获取日志列表
     * @param module 模块名称
     * @return 模块相关的日志列表
     */
    @Operation(
            summary = "根据模块名称获取日志列表",
            description = "获取指定模块的所有系统日志记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/module/{module}")
    public ResponseEntity<MyApiResponse<List<SystemLogDto>>> getLogsByModule(
            @Parameter(description = "模块名称", required = true) @PathVariable String module) {
        try {
            List<SystemLog> logs = systemLogService.getLogsByModule(module);
            List<SystemLogDto> logDtos = logs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(MyApiResponse.success("获取成功", logDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据日期范围获取日志列表
     * @param start 开始时间
     * @param end 结束时间
     * @return 日期范围内的日志列表
     */
    @Operation(
            summary = "根据日期范围获取日志列表",
            description = "获取指定时间范围内的所有系统日志记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/date-range")
    public ResponseEntity<MyApiResponse<List<SystemLogDto>>> getLogsByDateRange(
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<SystemLog> logs = systemLogService.getLogsByDateRange(start, end);
            List<SystemLogDto> logDtos = logs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(MyApiResponse.success("获取成功", logDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 分页获取日志列表
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页的日志列表
     */
    @Operation(
            summary = "分页获取日志列表",
            description = "获取分页的系统日志记录，支持排序和分页参数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping
    public ResponseEntity<MyApiResponse<Page<SystemLogDto>>> getLogs(
            @Parameter(description = "页码，从0开始", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SystemLog> logPage = systemLogService.getLogs(pageable);
            Page<SystemLogDto> dtoPage = logPage.map(this::convertToDto);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", dtoPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 搜索包含关键字的日志
     * @param keyword 搜索关键字
     * @return 匹配的日志列表
     */
    @Operation(
            summary = "搜索包含关键字的日志",
            description = "根据关键字搜索系统日志记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<MyApiResponse<List<SystemLogDto>>> searchLogs(
            @Parameter(description = "搜索关键字", required = true) @RequestParam String keyword) {
        try {
            List<SystemLog> logs = systemLogService.searchLogs(keyword);
            List<SystemLogDto> logDtos = logs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(MyApiResponse.success("获取成功", logDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取日志总数
     * @return 日志总数
     */
    @Operation(
            summary = "获取日志总数",
            description = "获取系统中的日志记录总数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/count")
    public ResponseEntity<MyApiResponse<Long>> countLogs() {
        try {
            Long count = systemLogService.countLogs();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将SystemLog实体转换为SystemLogDto
     * @param log 系统日志实体
     * @return 系统日志DTO
     */
    private SystemLogDto convertToDto(SystemLog log) {
        SystemLogDto dto = new SystemLogDto();
        dto.setId(log.getId());
        dto.setUserId(log.getUser().getId());
        dto.setUserName(log.getUser().getUsername()); // 假设User实体中有getUsername方法
        dto.setOperation(log.getOperation());
        dto.setModule(log.getModule());
        dto.setIpAddress(log.getIpAddress());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}