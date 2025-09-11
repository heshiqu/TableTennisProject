package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.ContestEnrollment;
import com.example.ttp_serve.entity.ContestSchedule;
import com.example.ttp_serve.entity.MonthlyContest;
import com.example.ttp_serve.enums.ContestGroup;
import com.example.ttp_serve.enums.ContestStatus;
import com.example.ttp_serve.enums.EnrollmentStatus;
import com.example.ttp_serve.service.ContestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 月赛管理控制器
 * 提供月赛的创建、报名、比赛安排和结果管理等功能
 */
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
@Tag(name = "月赛管理", description = "提供月赛的创建、报名、比赛安排和结果管理等功能")
public class ContestController {

    private final ContestService contestService;

    /**
     * 创建月赛
     *
     * @param contest 月赛信息
     * @return 创建的月赛对象
     *
     * @apiNote 创建一个月赛，需要指定校区、比赛日期和组别
     *          同一校区同一日期只能有一个月赛
     */
    @Operation(
            summary = "创建月赛",
            description = "创建一个月赛，需要指定校区、比赛日期和组别。同一校区同一日期只能有一个月赛",
            responses = {
                    @ApiResponse(responseCode = "200", description = "月赛创建成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping
    public ResponseEntity<MyApiResponse<MonthlyContest>> createContest(
            @Parameter(description = "月赛信息", required = true)
            @Valid @RequestBody MonthlyContest contest) {
        try {
            MonthlyContest createdContest = contestService.createContest(contest);
            return ResponseEntity.ok(MyApiResponse.success("月赛创建成功", createdContest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新月赛信息
     *
     * @param id 月赛ID
     * @param contest 月赛信息
     * @return 更新后的月赛对象
     *
     * @apiNote 更新月赛信息，只能更新未开始的月赛
     *          可以修改比赛日期和组别
     */
    @Operation(
            summary = "更新月赛信息",
            description = "更新月赛信息，只能更新未开始的月赛。可以修改比赛日期和组别",
            responses = {
                    @ApiResponse(responseCode = "200", description = "月赛更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<MyApiResponse<MonthlyContest>> updateContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long id,
            @Parameter(description = "月赛信息", required = true) @Valid @RequestBody MonthlyContest contest) {

        try {
            MonthlyContest updatedContest = contestService.updateContest(id, contest);
            return ResponseEntity.ok(MyApiResponse.success("月赛更新成功", updatedContest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除月赛
     *
     * @param id 月赛ID
     * @return 空响应
     *
     * @apiNote 删除指定的月赛
     *          只有没有报名记录的月赛才能删除
     */
    @Operation(
            summary = "删除月赛",
            description = "删除指定的月赛。只有没有报名记录的月赛才能删除",
            responses = {
                    @ApiResponse(responseCode = "200", description = "月赛删除成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<MyApiResponse<Void>> deleteContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long id) {
        try {
            contestService.deleteContest(id);
            return ResponseEntity.ok(MyApiResponse.success("月赛删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取月赛详情
     *
     * @param id 月赛ID
     * @return 月赛详细信息
     *
     * @apiNote 根据月赛ID获取月赛的详细信息
     */
    @Operation(
            summary = "获取月赛详情",
            description = "根据月赛ID获取月赛的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse<MonthlyContest>> getContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long id) {
        try {
            MonthlyContest contest = contestService.getContest(id);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", contest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取校区所有月赛
     *
     * @param campusId 校区ID
     * @return 校区的月赛列表
     *
     * @apiNote 获取指定校区的所有月赛
     */
    @Operation(
            summary = "获取校区所有月赛",
            description = "获取指定校区的所有月赛",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/campus/{campusId}")
    public ResponseEntity<MyApiResponse<List<MonthlyContest>>> getContestsByCampus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId) {
        try {
            List<MonthlyContest> contests = contestService.getContestsByCampus(campusId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", contests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定状态的月赛
     *
     * @param status 月赛状态
     * @return 特定状态的月赛列表
     *
     * @apiNote 获取系统中所有特定状态的月赛
     *          状态包括：即将开始、进行中、已结束
     */
    @Operation(
            summary = "获取特定状态的月赛",
            description = "获取系统中所有特定状态的月赛。状态包括：即将开始、进行中、已结束",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<MyApiResponse<List<MonthlyContest>>> getContestsByStatus(
            @Parameter(description = "月赛状态", required = true) @PathVariable ContestStatus status) {
        try {
            List<MonthlyContest> contests = contestService.getContestsByStatus(status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", contests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取即将开始的月赛
     *
     * @return 即将开始的月赛列表
     *
     * @apiNote 获取所有即将开始的月赛
     *          用于首页展示和报名提醒
     */
    @Operation(
            summary = "获取即将开始的月赛",
            description = "获取所有即将开始的月赛，用于首页展示和报名提醒",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/upcoming")
    public ResponseEntity<MyApiResponse<List<MonthlyContest>>> getUpcomingContests() {
        try {
            List<MonthlyContest> contests = contestService.getUpcomingContests();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", contests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 报名月赛
     *
     * @param enrollment 报名信息
     * @return 报名记录
     *
     * @apiNote 学员报名参加月赛
     *          需要指定月赛、学员和组别
     *          月赛开始前1天停止报名
     */
    @Operation(
            summary = "报名月赛",
            description = "学员报名参加月赛。需要指定月赛、学员和组别。月赛开始前1天停止报名",
            responses = {
                    @ApiResponse(responseCode = "200", description = "报名成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/enroll")
    public ResponseEntity<MyApiResponse<ContestEnrollment>> enrollContest(
            @Parameter(description = "报名信息", required = true)
            @Valid @RequestBody ContestEnrollment enrollment) {
        try {
            ContestEnrollment createdEnrollment = contestService.enrollContest(enrollment);
            return ResponseEntity.ok(MyApiResponse.success("报名成功", createdEnrollment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新报名状态
     *
     * @param id 报名记录ID
     * @param status 报名状态
     * @return 更新后的报名记录
     *
     * @apiNote 更新报名记录的状态
     *          状态包括：已报名、已支付
     */
    @Operation(
            summary = "更新报名状态",
            description = "更新报名记录的状态。状态包括：已报名、已支付",
            responses = {
                    @ApiResponse(responseCode = "200", description = "报名状态更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PutMapping("/enrollments/{id}/status")
    public ResponseEntity<MyApiResponse<ContestEnrollment>> updateEnrollmentStatus(
            @Parameter(description = "报名记录ID", required = true) @PathVariable Long id,
            @Parameter(description = "报名状态", required = true) @RequestParam EnrollmentStatus status) {

        try {
            ContestEnrollment enrollment = contestService.updateEnrollmentStatus(id, status);
            return ResponseEntity.ok(MyApiResponse.success("报名状态更新成功", enrollment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取月赛的报名记录
     *
     * @param contestId 月赛ID
     * @return 月赛的报名记录列表
     *
     * @apiNote 获取指定月赛的所有报名记录
     */
    @Operation(
            summary = "获取月赛的报名记录",
            description = "获取指定月赛的所有报名记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/enrollments")
    public ResponseEntity<MyApiResponse<List<ContestEnrollment>>> getEnrollmentsByContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId) {
        try {
            List<ContestEnrollment> enrollments = contestService.getEnrollmentsByContest(contestId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员的报名记录
     *
     * @param studentId 学员ID
     * @return 学员的报名记录列表
     *
     * @apiNote 获取指定学员的所有月赛报名记录
     */
    @Operation(
            summary = "获取学员的报名记录",
            description = "获取指定学员的所有月赛报名记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/students/{studentId}/enrollments")
    public ResponseEntity<MyApiResponse<List<ContestEnrollment>>> getEnrollmentsByStudent(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            List<ContestEnrollment> enrollments = contestService.getEnrollmentsByStudent(studentId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查学员是否已报名月赛
     *
     * @param contestId 月赛ID
     * @param studentId 学员ID
     * @return 是否已报名
     *
     * @apiNote 检查指定学员是否已报名指定月赛
     */
    @Operation(
            summary = "检查学员是否已报名月赛",
            description = "检查指定学员是否已报名指定月赛",
            responses = {
                    @ApiResponse(responseCode = "200", description = "检查完成"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/students/{studentId}/enrolled")
    public ResponseEntity<MyApiResponse<Boolean>> isStudentEnrolled(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId,
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {

        try {
            boolean isEnrolled = contestService.isStudentEnrolled(contestId, studentId);
            return ResponseEntity.ok(MyApiResponse.success("检查完成", isEnrolled));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 创建比赛安排
     *
     * @param schedule 比赛安排信息
     * @return 创建的比赛安排
     *
     * @apiNote 创建一场比赛的安排
     *          需要指定月赛、轮次、选手和球台
     */
    @Operation(
            summary = "创建比赛安排",
            description = "创建一场比赛的安排。需要指定月赛、轮次、选手和球台",
            responses = {
                    @ApiResponse(responseCode = "200", description = "比赛安排创建成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/schedules")
    public ResponseEntity<MyApiResponse<ContestSchedule>> createSchedule(
            @Parameter(description = "比赛安排信息", required = true)
            @Valid @RequestBody ContestSchedule schedule) {
        try {
            ContestSchedule createdSchedule = contestService.createSchedule(schedule);
            return ResponseEntity.ok(MyApiResponse.success("比赛安排创建成功", createdSchedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新比赛安排
     *
     * @param id 比赛安排ID
     * @param schedule 比赛安排信息
     * @return 更新后的比赛安排
     *
     * @apiNote 更新比赛安排信息
     *          可以修改球台、开始时间和状态
     */
    @Operation(
            summary = "更新比赛安排",
            description = "更新比赛安排信息。可以修改球台、开始时间和状态",
            responses = {
                    @ApiResponse(responseCode = "200", description = "比赛安排更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PutMapping("/schedules/{id}")
    public ResponseEntity<MyApiResponse<ContestSchedule>> updateSchedule(
            @Parameter(description = "比赛安排ID", required = true) @PathVariable Long id,
            @Parameter(description = "比赛安排信息", required = true) @Valid @RequestBody ContestSchedule schedule) {

        try {
            ContestSchedule updatedSchedule = contestService.updateSchedule(id, schedule);
            return ResponseEntity.ok(MyApiResponse.success("比赛安排更新成功", updatedSchedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取月赛的比赛安排
     *
     * @param contestId 月赛ID
     * @return 月赛的比赛安排列表
     *
     * @apiNote 获取指定月赛的所有比赛安排
     */
    @Operation(
            summary = "获取月赛的比赛安排",
            description = "获取指定月赛的所有比赛安排",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/schedules")
    public ResponseEntity<MyApiResponse<List<ContestSchedule>>> getSchedulesByContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId) {
        try {
            List<ContestSchedule> schedules = contestService.getSchedulesByContest(contestId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取轮次的比赛安排
     *
     * @param contestId 月赛ID
     * @param roundNumber 轮次
     * @return 轮次的比赛安排列表
     *
     * @apiNote 获取指定月赛指定轮次的所有比赛安排
     */
    @Operation(
            summary = "获取轮次的比赛安排",
            description = "获取指定月赛指定轮次的所有比赛安排",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/rounds/{roundNumber}/schedules")
    public ResponseEntity<MyApiResponse<List<ContestSchedule>>> getSchedulesByRound(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId,
            @Parameter(description = "轮次", required = true) @PathVariable Integer roundNumber) {

        try {
            List<ContestSchedule> schedules = contestService.getSchedulesByRound(contestId, roundNumber);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员的比赛安排
     *
     * @param playerId 学员ID
     * @return 学员的比赛安排列表
     *
     * @apiNote 获取指定学员的所有比赛安排
     */
    @Operation(
            summary = "获取学员的比赛安排",
            description = "获取指定学员的所有比赛安排",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/players/{playerId}/schedules")
    public ResponseEntity<MyApiResponse<List<ContestSchedule>>> getSchedulesByPlayer(
            @Parameter(description = "学员ID", required = true) @PathVariable Long playerId) {
        try {
            List<ContestSchedule> schedules = contestService.getSchedulesByPlayer(playerId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 生成比赛安排
     *
     * @param contestId 月赛ID
     * @param groupType 组别
     * @return 空响应
     *
     * @apiNote 为指定月赛的指定组别生成比赛安排
     *          系统会根据报名人数自动选择赛制（循环赛或小组赛+淘汰赛）
     */
    @Operation(
            summary = "生成比赛安排",
            description = "为指定月赛的指定组别生成比赛安排。系统会根据报名人数自动选择赛制（循环赛或小组赛+淘汰赛）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "比赛安排生成成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/{contestId}/generate-schedule")
    public ResponseEntity<MyApiResponse<Void>> generateContestSchedule(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId,
            @Parameter(description = "组别", required = true) @RequestParam ContestGroup groupType) {

        try {
            contestService.generateContestSchedule(contestId, groupType);
            return ResponseEntity.ok(MyApiResponse.success("比赛安排生成成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计月赛的报名人数
     *
     * @param contestId 月赛ID
     * @return 报名人数
     *
     * @apiNote 统计指定月赛的总报名人数
     */
    @Operation(
            summary = "统计月赛的报名人数",
            description = "统计指定月赛的总报名人数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "统计成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/enrollments/count")
    public ResponseEntity<MyApiResponse<Long>> countEnrollmentsByContest(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId) {
        try {
            Long count = contestService.countEnrollmentsByContest(contestId);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计月赛组别的报名人数
     *
     * @param contestId 月赛ID
     * @param groupType 组别
     * @return 组别的报名人数
     *
     * @apiNote 统计指定月赛指定组别的报名人数
     */
    @Operation(
            summary = "统计月赛组别的报名人数",
            description = "统计指定月赛指定组别的报名人数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "统计成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{contestId}/groups/{groupType}/enrollments/count")
    public ResponseEntity<MyApiResponse<Long>> countEnrollmentsByGroup(
            @Parameter(description = "月赛ID", required = true) @PathVariable Long contestId,
            @Parameter(description = "组别", required = true) @PathVariable ContestGroup groupType) {

        try {
            Long count = contestService.countEnrollmentsByGroup(contestId, groupType);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新比赛结果
     *
     * @param scheduleId 比赛安排ID
     * @param winnerId 获胜者ID
     * @return 更新后的比赛安排
     *
     * @apiNote 更新比赛结果，指定获胜者
     *          获胜者必须是比赛选手之一
     */
    @Operation(
            summary = "更新比赛结果",
            description = "更新比赛结果，指定获胜者。获胜者必须是比赛选手之一",
            responses = {
                    @ApiResponse(responseCode = "200", description = "比赛结果更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PutMapping("/schedules/{scheduleId}/result")
    public ResponseEntity<MyApiResponse<ContestSchedule>> updateMatchResult(
            @Parameter(description = "比赛安排ID", required = true) @PathVariable Long scheduleId,
            @Parameter(description = "获胜者ID", required = true) @RequestParam Long winnerId) {

        try {
            ContestSchedule schedule = contestService.updateMatchResult(scheduleId, winnerId);
            return ResponseEntity.ok(MyApiResponse.success("比赛结果更新成功", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }
}