package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CoachDTO;
import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@Tag(name = "教练管理", description = "教练信息的查询和管理接口")
public class CoachController {

    @Autowired
    private CoachService coachService;

    @GetMapping("/search")
    @Operation(summary = "条件查询教练", description = "根据姓名、性别、年龄条件查询教练，至少需要一个查询条件")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<MyApiResponse<List<CoachDTO>>> searchCoaches(
            @Parameter(description = "教练姓名(模糊匹配)") @RequestParam(required = false) String name,
            @Parameter(description = "教练性别") @RequestParam(required = false) String gender,
            @Parameter(description = "教练年龄") @RequestParam(required = false) Integer age,
            @Parameter(description = "校区ID") @RequestParam Long campusId) {
        try {
            List<CoachDTO> coaches = coachService.findCoachesByCriteria(name, gender, age, campusId);
            return ResponseEntity.ok(MyApiResponse.success("查询成功", coaches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/campus/{campusId}")
    @Operation(summary = "获取校区所有教练", description = "获取指定校区的所有教练列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<MyApiResponse<List<CoachDTO>>> getAllCoachesByCampus(
            @Parameter(description = "校区ID") @PathVariable Long campusId) {
        try {
            List<CoachDTO> coaches = coachService.findAllCoachesByCampus(campusId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", coaches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{coachId}")
    @Operation(summary = "获取教练详情", description = "根据ID获取教练的详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<MyApiResponse<CoachDTO>> getCoachDetail(
            @Parameter(description = "教练ID") @PathVariable Long coachId) {
        try {
            CoachDTO coach = coachService.getCoachDetail(coachId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", coach));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/count")
    @Operation(summary = "获取教练总数", description = "获取系统中所有教练的总数量")
    @ApiResponse(responseCode = "200", description = "成功获取教练数量")
    public ResponseEntity<MyApiResponse<Long>> getTotalCoachCount() {
        try {
            long count = coachService.getTotalCoachCount();
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/count/campus/{campusId}")
    @Operation(summary = "获取校区教练数量", description = "获取指定校区的教练数量")
    @ApiResponse(responseCode = "200", description = "成功获取校区教练数量")
    public ResponseEntity<MyApiResponse<Long>> getCoachCountByCampus(
            @Parameter(description = "校区ID", required = true, example = "1") 
            @PathVariable Long campusId) {
        try {
            long count = coachService.getCoachCountByCampus(campusId);
            return ResponseEntity.ok(MyApiResponse.success("统计成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取所有教练信息
     *
     * @return 所有教练信息列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有教练", description = "获取系统中所有教练的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CoachDTO>>> getAllCoaches() {
        try {
            List<CoachDTO> coaches = coachService.getAllCoaches();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", coaches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }
}