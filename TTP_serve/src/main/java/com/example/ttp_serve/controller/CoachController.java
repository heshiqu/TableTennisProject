package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CoachDTO;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误，至少需要一个查询条件"),
            @ApiResponse(responseCode = "404", description = "未找到符合条件的教练")
    })
    public ResponseEntity<List<CoachDTO>> searchCoaches(
            @Parameter(description = "教练姓名(模糊匹配)") @RequestParam(required = false) String name,
            @Parameter(description = "教练性别") @RequestParam(required = false) String gender,
            @Parameter(description = "教练年龄") @RequestParam(required = false) Integer age,
            @Parameter(description = "校区ID") @RequestParam Long campusId) {

        List<CoachDTO> coaches = coachService.findCoachesByCriteria(name, gender, age, campusId);

        if (coaches.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(coaches);
    }

    @GetMapping("/campus/{campusId}")
    @Operation(summary = "获取校区所有教练", description = "获取指定校区的所有教练列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "该校区没有教练")
    })
    public ResponseEntity<List<CoachDTO>> getAllCoachesByCampus(
            @Parameter(description = "校区ID") @PathVariable Long campusId) {

        List<CoachDTO> coaches = coachService.findAllCoachesByCampus(campusId);

        if (coaches.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(coaches);
    }

    @GetMapping("/{coachId}")
    @Operation(summary = "获取教练详情", description = "根据ID获取教练的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "未找到该教练")
    })
    public ResponseEntity<CoachDTO> getCoachDetail(
            @Parameter(description = "教练ID") @PathVariable Long coachId) {

        CoachDTO coach = coachService.getCoachDetail(coachId);
        return ResponseEntity.ok(coach);
    }
}