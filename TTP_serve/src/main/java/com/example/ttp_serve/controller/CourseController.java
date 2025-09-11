package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CourseDTO;
import com.example.ttp_serve.dto.CourseRequestDTO;
import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.Course;
import com.example.ttp_serve.enums.CourseStatus;
import com.example.ttp_serve.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程管理控制器
 * 提供课程预约、取消、确认、完成和查询等功能
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "课程管理", description = "提供课程预约、取消、确认、完成和查询等功能")
public class CourseController {

    private final CourseService courseService;

    /**
     * 创建课程预约
     *
     * @param courseRequest 课程请求信息
     * @return 创建的课程对象DTO
     *
     * @apiNote 学员可以预约已建立双选关系的教练的课程
     *          需要指定教练、学员、开始时间和结束时间
     *          系统会自动计算费用并检查时间冲突
     */
    @PostMapping
    @Operation(summary = "创建课程预约", description = "学员可以预约已建立双选关系的教练的课程，系统会自动计算费用并检查时间冲突")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "课程预约成功"),
            @ApiResponse(responseCode = "400", description = "预约失败，参数错误或时间冲突"),
            @ApiResponse(responseCode = "404", description = "教练或学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> createCourse(
            @Parameter(description = "课程信息", required = true)
            @Valid @RequestBody CourseRequestDTO courseRequest) {
        try {
            Course createdCourse = courseService.createCourseFromRequest(courseRequest);
            return ResponseEntity.ok(MyApiResponse.success("课程预约成功", convertToDTO(createdCourse)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新课程信息
     *
     * @param id 课程ID
     * @param courseRequest 课程请求信息
     * @return 更新后的课程对象DTO
     *
     * @apiNote 只能更新未确认的课程信息
     *          可以修改时间、球台等信息
     *          修改时间后会重新计算费用
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新课程信息", description = "只能更新未确认的课程信息，可以修改时间、球台等信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "课程更新成功"),
            @ApiResponse(responseCode = "400", description = "更新失败，课程已确认或时间冲突"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> updateCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long id,
            @Parameter(description = "课程信息", required = true) @Valid @RequestBody CourseRequestDTO courseRequest) {

        try {
            Course updatedCourse = courseService.updateCourseFromRequest(id, courseRequest);
            return ResponseEntity.ok(MyApiResponse.success("课程更新成功", convertToDTO(updatedCourse)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 取消课程
     *
     * @param id 课程ID
     * @param reason 取消原因
     * @param cancelledBy 取消操作者ID
     * @return 取消后的课程对象DTO
     *
     * @apiNote 只能在课程开始前24小时取消
     *          取消后系统会自动处理退款
     *          需要提供取消原因
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消课程", description = "只能在课程开始前24小时取消，取消后系统会自动处理退款")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "课程取消成功"),
            @ApiResponse(responseCode = "400", description = "取消失败，课程已开始或不足24小时"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> cancelCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long id,
            @Parameter(description = "取消原因", required = true) @RequestParam String reason,
            @Parameter(description = "取消操作者ID", required = true) @RequestParam Long cancelledBy) {

        try {
            Course cancelledCourse = courseService.cancelCourse(id, reason, cancelledBy);
            return ResponseEntity.ok(MyApiResponse.success("课程取消成功", convertToDTO(cancelledCourse)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 确认课程
     *
     * @param id 课程ID
     * @return 确认后的课程对象DTO
     *
     * @apiNote 教练可以确认学员的课程预约
     *          确认后课程状态变为已确认
     *          确认后系统会自动从学员账户扣除费用
     */
    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认课程", description = "教练可以确认学员的课程预约，确认后系统会自动从学员账户扣除费用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "课程确认成功"),
            @ApiResponse(responseCode = "400", description = "确认失败，课程状态不正确"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> confirmCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long id) {
        try {
            Course confirmedCourse = courseService.confirmCourse(id);
            return ResponseEntity.ok(MyApiResponse.success("课程确认成功", convertToDTO(confirmedCourse)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 完成课程
     *
     * @param id 课程ID
     * @return 完成后的课程对象DTO
     *
     * @apiNote 教练或学员可以标记课程为已完成
     *          完成后可以发起课程评价
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "完成课程", description = "教练或学员可以标记课程为已完成，完成后可以发起课程评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "课程完成"),
            @ApiResponse(responseCode = "400", description = "完成失败，课程状态不正确"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> completeCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long id) {
        try {
            Course completedCourse = courseService.completeCourse(id);
            return ResponseEntity.ok(MyApiResponse.success("课程完成", convertToDTO(completedCourse)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程详细信息DTO
     *
     * @apiNote 根据课程ID获取课程的详细信息
     *          包括教练、学员、时间、费用等信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程的详细信息，包括教练、学员、时间、费用等信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourseDTO>> getCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long id) {
        try {
            Course course = courseService.getCourse(id);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTO(course)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取教练的课程
     *
     * @param coachId 教练ID
     * @return 教练的所有课程列表DTO
     *
     * @apiNote 获取指定教练的所有课程
     *          包括不同状态的课程
     */
    @GetMapping("/coach/{coachId}")
    @Operation(summary = "获取教练的课程", description = "获取指定教练的所有课程，包括不同状态的课程")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoursesByCoach(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId) {
        try {
            List<Course> courses = courseService.getCoursesByCoach(coachId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员的课程
     *
     * @param studentId 学员ID
     * @return 学员的所有课程列表DTO
     *
     * @apiNote 获取指定学员的所有课程
     *          包括不同状态的课程
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学员的课程", description = "获取指定学员的所有课程，包括不同状态的课程")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoursesByStudent(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            List<Course> courses = courseService.getCoursesByStudent(studentId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定状态的课程
     *
     * @param status 课程状态
     * @return 特定状态的课程列表DTO
     *
     * @apiNote 获取系统中所有特定状态的课程
     *          状态包括：待确认、已确认、已完成、已取消
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "获取特定状态的课程", description = "获取系统中所有特定状态的课程，状态包括：待确认、已确认、已完成、已取消")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoursesByStatus(
            @Parameter(description = "课程状态", required = true) @PathVariable CourseStatus status) {
        try {
            List<Course> courses = courseService.getCoursesByStatus(status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取日期范围内的课程
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 日期范围内的课程列表DTO
     *
     * @apiNote 获取指定时间范围内的所有课程
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/date-range")
    @Operation(summary = "获取日期范围内的课程", description = "获取指定时间范围内的所有课程，时间格式：yyyy-MM-dd HH:mm:ss")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "时间格式错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoursesByDateRange(
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {

        try {
            List<Course> courses = courseService.getCoursesByDateRange(start, end);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取教练在日期范围内的课程
     *
     * @param coachId 教练ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 教练在日期范围内的课程列表DTO
     *
     * @apiNote 获取指定教练在指定时间范围内的课程
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/coach/{coachId}/date-range")
    @Operation(summary = "获取教练在日期范围内的课程", description = "获取指定教练在指定时间范围内的课程，时间格式：yyyy-MM-dd HH:mm:ss")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "400", description = "时间格式错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoachCoursesByDateRange(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {

        try {
            List<Course> courses = courseService.getCoachCoursesByDateRange(coachId, start, end);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员在日期范围内的课程
     *
     * @param studentId 学员ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 学员在日期范围内的课程列表DTO
     *
     * @apiNote 获取指定学员在指定时间范围内的课程
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/student/{studentId}/date-range")
    @Operation(summary = "获取学员在日期范围内的课程", description = "获取指定学员在指定时间范围内的课程，时间格式：yyyy-MM-dd HH:mm:ss")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "400", description = "时间格式错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getStudentCoursesByDateRange(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {

        try {
            List<Course> courses = courseService.getStudentCoursesByDateRange(studentId, start, end);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查时间冲突
     *
     * @param coachId 教练ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否存在时间冲突
     *
     * @apiNote 检查指定教练在指定时间段是否已有课程安排
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/check-time-conflict")
    @Operation(summary = "检查时间冲突", description = "检查指定教练在指定时间段是否已有课程安排，时间格式：yyyy-MM-dd HH:mm:ss")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "400", description = "时间格式错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> checkTimeConflict(
            @Parameter(description = "教练ID", required = true) @RequestParam Long coachId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        try {
            boolean hasConflict = courseService.checkTimeConflict(coachId, startTime, endTime);
            return ResponseEntity.ok(MyApiResponse.success("检查完成", hasConflict));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查球台时间冲突
     *
     * @param courtId 球台ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否存在时间冲突
     *
     * @apiNote 检查指定球台在指定时间段是否已被占用
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @GetMapping("/check-court-time-conflict")
    @Operation(summary = "检查球台时间冲突", description = "检查指定球台在指定时间段是否已被占用，时间格式：yyyy-MM-dd HH:mm:ss")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "400", description = "时间格式错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> checkCourtTimeConflict(
            @Parameter(description = "球台ID", required = true) @RequestParam Long courtId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        try {
            boolean hasConflict = courseService.checkCourtTimeConflict(courtId, startTime, endTime);
            return ResponseEntity.ok(MyApiResponse.success("检查完成", hasConflict));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计课程数量
     *
     * @return 课程总数
     *
     * @apiNote 获取系统中的课程总数
     */
    @GetMapping("/count")
    @Operation(summary = "统计课程数量", description = "获取系统中的课程总数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countCourses() {
        try {
            Long count = courseService.countCourses();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计教练的课程数量
     *
     * @param coachId 教练ID
     * @return 教练的课程数量
     *
     * @apiNote 获取指定教练的课程总数
     */
    @GetMapping("/coach/{coachId}/count")
    @Operation(summary = "统计教练的课程数量", description = "获取指定教练的课程总数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countCoursesByCoach(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId) {
        try {
            Long count = courseService.countCoursesByCoach(coachId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计学员的课程数量
     *
     * @param studentId 学员ID
     * @return 学员的课程数量
     *
     * @apiNote 获取指定学员的课程总数
     */
    @GetMapping("/student/{studentId}/count")
    @Operation(summary = "统计学员的课程数量", description = "获取指定学员的课程总数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countCoursesByStudent(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            Long count = courseService.countCoursesByStudent(studentId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取即将开始的课程
     *
     * @param hours 小时数
     * @return 即将开始的课程列表DTO
     *
     * @apiNote 获取指定小时内即将开始的课程
     *          用于课程提醒功能
     */
    @GetMapping("/upcoming/{hours}")
    @Operation(summary = "获取即将开始的课程", description = "获取指定小时内即将开始的课程，用于课程提醒功能")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getUpcomingCourses(
            @Parameter(description = "小时数", required = true) @PathVariable int hours) {
        try {
            List<Course> courses = courseService.getUpcomingCourses(hours);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取需要评价的课程
     *
     * @param userId 用户ID
     * @return 需要评价的课程列表DTO
     *
     * @apiNote 获取指定用户需要评价的课程
     *          通常是已完成但未评价的课程
     */
    @GetMapping("/need-evaluation/{userId}")
    @Operation(summary = "获取需要评价的课程", description = "获取指定用户需要评价的课程，通常是已完成但未评价的课程")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourseDTO>>> getCoursesNeedEvaluation(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            List<Course> courses = courseService.getCoursesNeedEvaluation(userId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(courses)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将实体转换为DTO
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCoachId(course.getCoach().getId());
        dto.setCoachName(course.getCoach().getRealName());
        dto.setStudentId(course.getStudent().getId());
        dto.setStudentName(course.getStudent().getRealName());

        if (course.getCourt() != null) {
            dto.setCourtId(course.getCourt().getId());
            dto.setCourtNumber(course.getCourt().getCourtNumber());
        }

        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setDuration(course.getDuration());
        dto.setFee(course.getFee());
        dto.setStatus(course.getStatus());
        dto.setCancelReason(course.getCancelReason());

        if (course.getCancelBy() != null) {
            dto.setCancelByUserId(course.getCancelBy().getId());
            dto.setCancelByUserName(course.getCancelBy().getRealName());
        }

        dto.setCancelTime(course.getCancelTime());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());

        return dto;
    }

    /**
     * 将实体列表转换为DTO列表
     */
    private List<CourseDTO> convertToDTOList(List<Course> courses) {
        return courses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}