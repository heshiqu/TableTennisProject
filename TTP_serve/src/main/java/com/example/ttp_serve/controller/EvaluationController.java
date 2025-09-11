package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.Evaluation;
import com.example.ttp_serve.enums.EvaluationType;
import com.example.ttp_serve.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * 评价管理控制器
 * 提供评价的创建、查询、更新和删除等功能
 */
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "提供评价的创建、查询、更新和删除等功能")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * 创建评价
     *
     * @param evaluation 评价信息
     * @return 创建的评价对象
     *
     * @apiNote 创建一条评价记录，可以是学员对教练的评价或教练对学员的评价
     *          只能对已完成的课程进行评价
     *          每个用户对同一课程只能评价一次
     */
    @PostMapping
    @Operation(summary = "创建评价", description = "创建一条评价记录，可以是学员对教练的评价或教练对学员的评价，只能对已完成的课程进行评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "评价创建成功"),
            @ApiResponse(responseCode = "400", description = "评价创建失败，课程未完成或已评价过"),
            @ApiResponse(responseCode = "404", description = "课程或用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Evaluation>> createEvaluation(
            @Parameter(description = "评价信息", required = true)
            @Valid @RequestBody Evaluation evaluation) {
        try {
            Evaluation createdEvaluation = evaluationService.createEvaluation(evaluation);
            return ResponseEntity.ok(MyApiResponse.success("评价创建成功", createdEvaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新评价
     *
     * @param id 评价ID
     * @param evaluation 评价信息
     * @return 更新后的评价对象
     *
     * @apiNote 更新评价内容和评分
     *          只能更新自己创建的评价
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新评价", description = "更新评价内容和评分，只能更新自己创建的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "评价更新成功"),
            @ApiResponse(responseCode = "400", description = "评价更新失败"),
            @ApiResponse(responseCode = "404", description = "评价不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Evaluation>> updateEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id,
            @Parameter(description = "评价信息", required = true) @Valid @RequestBody Evaluation evaluation) {

        try {
            Evaluation updatedEvaluation = evaluationService.updateEvaluation(id, evaluation);
            return ResponseEntity.ok(MyApiResponse.success("评价更新成功", updatedEvaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除评价
     *
     * @param id 评价ID
     * @return 空响应
     *
     * @apiNote 删除指定的评价
     *          只能删除自己创建的评价
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "删除指定的评价，只能删除自己创建的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "评价删除成功"),
            @ApiResponse(responseCode = "400", description = "评价删除失败"),
            @ApiResponse(responseCode = "404", description = "评价不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Void>> deleteEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id) {
        try {
            evaluationService.deleteEvaluation(id);
            return ResponseEntity.ok(MyApiResponse.success("评价删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详细信息
     *
     * @apiNote 根据评价ID获取评价的详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情", description = "根据评价ID获取评价的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "评价不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Evaluation>> getEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long id) {
        try {
            Evaluation evaluation = evaluationService.getEvaluation(id);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据课程ID获取评价
     *
     * @param courseId 课程ID
     * @return 课程的评价列表
     *
     * @apiNote 获取指定课程的所有评价
     *          包括学员对教练的评价和教练对学员的评价
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "根据课程ID获取评价", description = "获取指定课程的所有评价，包括学员对教练的评价和教练对学员的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByCourse(courseId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据评价者ID获取评价
     *
     * @param fromUserId 评价者ID
     * @return 评价者创建的评价列表
     *
     * @apiNote 获取指定用户创建的所有评价
     */
    @GetMapping("/from-user/{fromUserId}")
    @Operation(summary = "根据评价者ID获取评价", description = "获取指定用户创建的所有评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByFromUser(
            @Parameter(description = "评价者ID", required = true) @PathVariable Long fromUserId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByFromUser(fromUserId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据被评价者ID获取评价
     *
     * @param toUserId 被评价者ID
     * @return 被评价者收到的评价列表
     *
     * @apiNote 获取指定用户收到的所有评价
     */
    @GetMapping("/to-user/{toUserId}")
    @Operation(summary = "根据被评价者ID获取评价", description = "获取指定用户收到的所有评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByToUser(
            @Parameter(description = "被评价者ID", required = true) @PathVariable Long toUserId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByToUser(toUserId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据类型获取评价
     *
     * @param type 评价类型
     * @return 特定类型的评价列表
     *
     * @apiNote 获取系统中所有特定类型的评价
     *          类型包括：学员对教练、教练对学员
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取评价", description = "获取系统中所有特定类型的评价，类型包括：学员对教练、教练对学员")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByType(
            @Parameter(description = "评价类型", required = true) @PathVariable EvaluationType type) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByType(type);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据评价者和类型获取评价
     *
     * @param fromUserId 评价者ID
     * @param type 评价类型
     * @return 评价者创建的特定类型的评价列表
     *
     * @apiNote 获取指定用户创建的特定类型的评价
     */
    @GetMapping("/from-user/{fromUserId}/type/{type}")
    @Operation(summary = "根据评价者和类型获取评价", description = "获取指定用户创建的特定类型的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByFromUserAndType(
            @Parameter(description = "评价者ID", required = true) @PathVariable Long fromUserId,
            @Parameter(description = "评价类型", required = true) @PathVariable EvaluationType type) {

        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByFromUserAndType(fromUserId, type);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据被评价者和类型获取评价
     *
     * @param toUserId 被评价者ID
     * @param type 评价类型
     * @return 被评价者收到的特定类型的评价列表
     *
     * @apiNote 获取指定用户收到的特定类型的评价
     */
    @GetMapping("/to-user/{toUserId}/type/{type}")
    @Operation(summary = "根据被评价者和类型获取评价", description = "获取指定用户收到的特定类型的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByToUserAndType(
            @Parameter(description = "被评价者ID", required = true) @PathVariable Long toUserId,
            @Parameter(description = "评价类型", required = true) @PathVariable EvaluationType type) {

        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByToUserAndType(toUserId, type);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据课程ID和类型获取评价
     *
     * @param courseId 课程ID
     * @param type 评价类型
     * @return 课程的特定类型的评价列表
     *
     * @apiNote 获取指定课程的特定类型的评价
     */
    @GetMapping("/course/{courseId}/type/{type}")
    @Operation(summary = "根据课程ID和类型获取评价", description = "获取指定课程的特定类型的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "课程不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Evaluation>>> getEvaluationsByCourseAndType(
            @Parameter(description = "课程ID", required = true) @PathVariable Long courseId,
            @Parameter(description = "评价类型", required = true) @PathVariable EvaluationType type) {

        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByCourseAndType(courseId, type);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 分页获取评价
     *
     * @param pageable 分页参数
     * @return 分页的评价列表
     *
     * @apiNote 获取分页的评价列表，支持排序和分页参数
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取评价", description = "获取分页的评价列表，支持排序和分页参数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Page<Evaluation>>> getEvaluations(
            @Parameter(description = "分页参数") Pageable pageable) {
        try {
            Page<Evaluation> evaluations = evaluationService.getEvaluations(pageable);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", evaluations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 计算被评价者的平均评分
     *
     * @param userId 用户ID
     * @return 平均评分
     *
     * @apiNote 计算指定用户收到的评价的平均评分
     *          只计算有评分的评价
     */
    @GetMapping("/user/{userId}/average-rating")
    @Operation(summary = "计算平均评分", description = "计算指定用户收到的评价的平均评分，只计算有评分的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "计算成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Double>> calculateAverageRating(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            Double averageRating = evaluationService.calculateAverageRating(userId);
            return ResponseEntity.ok(MyApiResponse.success("计算成功", averageRating));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计被评价者的评价数量
     *
     * @param userId 用户ID
     * @return 评价数量
     *
     * @apiNote 统计指定用户收到的评价数量
     */
    @GetMapping("/user/{userId}/count")
    @Operation(summary = "统计评价数量", description = "统计指定用户收到的评价数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countEvaluationsByToUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            Long count = evaluationService.countEvaluationsByToUser(userId);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查是否已存在评价
     *
     * @param courseId 课程ID
     * @param fromUserId 评价者ID
     * @param type 评价类型
     * @return 是否已存在评价
     *
     * @apiNote 检查指定用户是否已对指定课程进行过特定类型的评价
     */
    @GetMapping("/exists")
    @Operation(summary = "检查评价是否存在", description = "检查指定用户是否已对指定课程进行过特定类型的评价")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> existsEvaluationByCourseAndFromUser(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "评价者ID", required = true) @RequestParam Long fromUserId,
            @Parameter(description = "评价类型", required = true) @RequestParam EvaluationType type) {

        try {
            boolean exists = evaluationService.existsEvaluationByCourseAndFromUser(courseId, fromUserId, type);
            return ResponseEntity.ok(MyApiResponse.success("检查完成", exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取需要评价的课程
     *
     * @param userId 用户ID
     * @return 需要评价的课程ID列表
     *
     * @apiNote 获取指定用户需要评价的课程ID列表
     *          通常是已完成但未评价的课程
     */
    @GetMapping("/user/{userId}/need-evaluation")
    @Operation(summary = "获取需要评价的课程", description = "获取指定用户需要评价的课程ID列表，通常是已完成但未评价的课程")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<Long>>> getCoursesNeedEvaluation(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            List<Long> courseIds = evaluationService.getCoursesNeedEvaluation(userId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", courseIds));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }
}