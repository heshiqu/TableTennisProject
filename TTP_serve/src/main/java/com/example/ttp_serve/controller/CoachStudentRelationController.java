package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CoachStudentRelationDTO;
import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.CoachStudentRelation;
import com.example.ttp_serve.enums.RelationStatus;
import com.example.ttp_serve.service.CoachStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练学员关系管理控制器
 * 提供教练和学员之间关系的申请、审批、查询等功能
 */
@RestController
@RequestMapping("/api/coach-student-relations")
@RequiredArgsConstructor
@Tag(name = "教练学员关系管理", description = "提供教练和学员之间关系的申请、审批、查询等功能")
public class CoachStudentRelationController {

    private final CoachStudentService coachStudentService;

    /**
     * 申请建立教练学员关系
     *
     * @param coachId 教练ID
     * @param studentId 学员ID
     * @return 创建的关系对象DTO
     *
     * @apiNote 学员可以向教练提交关系申请，教练需要审核通过后才能建立正式关系
     *          每位学员最多只能选择两位教练，教练有最大学员数量限制
     */
    @PostMapping("/apply")
    @Operation(summary = "申请建立关系", description = "学员可以向教练提交关系申请，教练需要审核通过后才能建立正式关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "申请提交成功"),
            @ApiResponse(responseCode = "400", description = "申请失败，教练已满员或学员已达最大教练数"),
            @ApiResponse(responseCode = "404", description = "教练或学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CoachStudentRelationDTO>> applyRelation(
            @Parameter(description = "教练ID", required = true) @RequestParam Long coachId,
            @Parameter(description = "学员ID", required = true) @RequestParam Long studentId) {
        try {
            CoachStudentRelation relation = coachStudentService.applyRelation(coachId, studentId);
            return ResponseEntity.ok(MyApiResponse.success("申请提交成功", convertToDTO(relation)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 批准关系申请
     *
     * @param relationId 关系申请ID
     * @param coachId 教练ID（用于权限验证）
     * @return 更新后的关系对象DTO
     *
     * @apiNote 教练可以批准学员的关系申请，批准后会增加教练的当前学员数
     *          只有关系申请对应的教练才能执行此操作
     */
    @PostMapping("/{relationId}/approve")
    @Operation(summary = "批准关系申请", description = "教练可以批准学员的关系申请，批准后会增加教练的当前学员数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "批准成功"),
            @ApiResponse(responseCode = "400", description = "批准失败，教练已满员"),
            @ApiResponse(responseCode = "403", description = "无权操作此申请"),
            @ApiResponse(responseCode = "404", description = "关系申请不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CoachStudentRelationDTO>> approveRelation(
            @Parameter(description = "关系申请ID", required = true) @PathVariable Long relationId,
            @Parameter(description = "教练ID", required = true) @RequestParam Long coachId) {
        try {
            CoachStudentRelation relation = coachStudentService.approveRelation(relationId, coachId);
            return ResponseEntity.ok(MyApiResponse.success("批准成功", convertToDTO(relation)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 拒绝关系申请
     *
     * @param relationId 关系申请ID
     * @param coachId 教练ID（用于权限验证）
     * @param reason 拒绝原因
     * @return 更新后的关系对象DTO
     *
     * @apiNote 教练可以拒绝学员的关系申请，需要提供拒绝原因
     *          只有关系申请对应的教练才能执行此操作
     */
    @PostMapping("/{relationId}/reject")
    @Operation(summary = "拒绝关系申请", description = "教练可以拒绝学员的关系申请，需要提供拒绝原因")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "拒绝成功"),
            @ApiResponse(responseCode = "403", description = "无权操作此申请"),
            @ApiResponse(responseCode = "404", description = "关系申请不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CoachStudentRelationDTO>> rejectRelation(
            @Parameter(description = "关系申请ID", required = true) @PathVariable Long relationId,
            @Parameter(description = "教练ID", required = true) @RequestParam Long coachId,
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String reason) {
        try {
            CoachStudentRelation relation = coachStudentService.rejectRelation(relationId, coachId, reason);
            return ResponseEntity.ok(MyApiResponse.success("拒绝成功", convertToDTO(relation)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取关系详情
     *
     * @param relationId 关系ID
     * @return 关系详情DTO
     *
     * @apiNote 根据关系ID获取关系的详细信息
     */
    @GetMapping("/{relationId}")
    @Operation(summary = "获取关系详情", description = "根据关系ID获取关系的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "关系不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CoachStudentRelationDTO>> getRelation(
            @Parameter(description = "关系ID", required = true) @PathVariable Long relationId) {
        try {
            CoachStudentRelation relation = coachStudentService.getRelation(relationId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTO(relation)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取教练的所有关系
     *
     * @param coachId 教练ID
     * @return 教练的所有关系列表DTO
     *
     * @apiNote 获取指定教练的所有关系（包括待处理、已批准和已拒绝的关系）
     */
    @GetMapping("/coach/{coachId}")
    @Operation(summary = "获取教练的所有关系", description = "获取指定教练的所有关系（包括待处理、已批准和已拒绝的关系）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachStudentRelationDTO>>> getRelationsByCoach(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId) {
        try {
            List<CoachStudentRelation> relations = coachStudentService.getRelationsByCoach(coachId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(relations)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员的所有关系
     *
     * @param studentId 学员ID
     * @return 学员的所有关系列表DTO
     *
     * @apiNote 获取指定学员的所有关系（包括待处理、已批准和已拒绝的关系）
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学员的所有关系", description = "获取指定学员的所有关系（包括待处理、已批准和已拒绝的关系）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachStudentRelationDTO>>> getRelationsByStudent(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            List<CoachStudentRelation> relations = coachStudentService.getRelationsByStudent(studentId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(relations)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定状态的关系
     *
     * @param status 关系状态
     * @return 特定状态的关系列表DTO
     *
     * @apiNote 获取系统中所有特定状态的关系（待处理、已批准或已拒绝）
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "获取特定状态的关系", description = "获取系统中所有特定状态的关系（待处理、已批准或已拒绝）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachStudentRelationDTO>>> getRelationsByStatus(
            @Parameter(description = "关系状态", required = true) @PathVariable RelationStatus status) {
        try {
            List<CoachStudentRelation> relations = coachStudentService.getRelationsByStatus(status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(relations)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取教练的特定状态的关系
     *
     * @param coachId 教练ID
     * @param status 关系状态
     * @return 教练的特定状态的关系列表DTO
     *
     * @apiNote 获取指定教练的特定状态的关系（待处理、已批准或已拒绝）
     */
    @GetMapping("/coach/{coachId}/status/{status}")
    @Operation(summary = "获取教练的特定状态的关系", description = "获取指定教练的特定状态的关系（待处理、已批准或已拒绝）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachStudentRelationDTO>>> getCoachRelationsByStatus(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId,
            @Parameter(description = "关系状态", required = true) @PathVariable RelationStatus status) {
        try {
            List<CoachStudentRelation> relations = coachStudentService.getCoachRelationsByStatus(coachId, status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(relations)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取学员的特定状态的关系
     *
     * @param studentId 学员ID
     * @param status 关系状态
     * @return 学员的特定状态的关系列表DTO
     *
     * @apiNote 获取指定学员的特定状态的关系（待处理、已批准或已拒绝）
     */
    @GetMapping("/student/{studentId}/status/{status}")
    @Operation(summary = "获取学员的特定状态的关系", description = "获取指定学员的特定状态的关系（待处理、已批准或已拒绝）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachStudentRelationDTO>>> getStudentRelationsByStatus(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId,
            @Parameter(description = "关系状态", required = true) @PathVariable RelationStatus status) {
        try {
            List<CoachStudentRelation> relations = coachStudentService.getStudentRelationsByStatus(studentId, status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTOList(relations)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查是否已存在关系
     *
     * @param coachId 教练ID
     * @param studentId 学员ID
     * @return 是否存在关系
     *
     * @apiNote 检查指定的教练和学员之间是否已存在关系（任何状态）
     */
    @GetMapping("/exists")
    @Operation(summary = "检查关系是否存在", description = "检查指定的教练和学员之间是否已存在关系（任何状态）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> existsRelation(
            @Parameter(description = "教练ID", required = true) @RequestParam Long coachId,
            @Parameter(description = "学员ID", required = true) @RequestParam Long studentId) {
        try {
            boolean exists = coachStudentService.existsRelation(coachId, studentId);
            return ResponseEntity.ok(MyApiResponse.success("检查成功", exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除关系
     *
     * @param relationId 关系ID
     * @return 空响应
     *
     * @apiNote 删除指定的关系，如果是已批准的关系，会减少教练的当前学员数
     */
    @DeleteMapping("/{relationId}")
    @Operation(summary = "删除关系", description = "删除指定的关系，如果是已批准的关系，会减少教练的当前学员数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "关系不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Void>> deleteRelation(
            @Parameter(description = "关系ID", required = true) @PathVariable Long relationId) {
        try {
            coachStudentService.deleteRelation(relationId);
            return ResponseEntity.ok(MyApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计教练的学员数量
     *
     * @param coachId 教练ID
     * @return 教练的学员数量
     *
     * @apiNote 统计指定教练的已批准关系的学员数量
     */
    @GetMapping("/coach/{coachId}/students/count")
    @Operation(summary = "统计教练的学员数量", description = "统计指定教练的已批准关系的学员数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countStudentsByCoach(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId) {
        try {
            Long count = coachStudentService.countStudentsByCoach(coachId);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计学员的教练数量
     *
     * @param studentId 学员ID
     * @return 学员的教练数量
     *
     * @apiNote 统计指定学员的已批准关系的教练数量
     */
    @GetMapping("/student/{studentId}/coaches/count")
    @Operation(summary = "统计学员的教练数量", description = "统计指定学员的已批准关系的教练数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Long>> countCoachesByStudent(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            Long count = coachStudentService.countCoachesByStudent(studentId);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查教练是否已满员
     *
     * @param coachId 教练ID
     * @return 教练是否已满员
     *
     * @apiNote 检查指定教练的学员数量是否已达到最大限制
     */
    @GetMapping("/coach/{coachId}/is-full")
    @Operation(summary = "检查教练是否已满员", description = "检查指定教练的学员数量是否已达到最大限制")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "404", description = "教练不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> isCoachFull(
            @Parameter(description = "教练ID", required = true) @PathVariable Long coachId) {
        try {
            boolean isFull = coachStudentService.isCoachFull(coachId);
            return ResponseEntity.ok(MyApiResponse.success("检查成功", isFull));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查学员是否已达到最大教练数
     *
     * @param studentId 学员ID
     * @return 学员是否已达到最大教练数
     *
     * @apiNote 检查指定学员的教练数量是否已达到最大限制（每位学员最多两位教练）
     */
    @GetMapping("/student/{studentId}/has-max-coaches")
    @Operation(summary = "检查学员是否已达到最大教练数", description = "检查指定学员的教练数量是否已达到最大限制（每位学员最多两位教练）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "404", description = "学员不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Boolean>> hasMaxCoaches(
            @Parameter(description = "学员ID", required = true) @PathVariable Long studentId) {
        try {
            boolean hasMax = coachStudentService.hasMaxCoaches(studentId);
            return ResponseEntity.ok(MyApiResponse.success("检查成功", hasMax));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将实体转换为DTO
     */
    private CoachStudentRelationDTO convertToDTO(CoachStudentRelation relation) {
        CoachStudentRelationDTO dto = new CoachStudentRelationDTO();
        dto.setId(relation.getId());
        dto.setCoachId(relation.getCoach().getId());
        dto.setCoachName(relation.getCoach().getRealName());
        dto.setStudentId(relation.getStudent().getId());
        dto.setStudentName(relation.getStudent().getRealName());
        dto.setStatus(relation.getStatus());
        dto.setApplyTime(relation.getApplyTime());
        dto.setApproveTime(relation.getApproveTime());
        return dto;
    }

    /**
     * 将实体列表转换为DTO列表
     */
    private List<CoachStudentRelationDTO> convertToDTOList(List<CoachStudentRelation> relations) {
        return relations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}