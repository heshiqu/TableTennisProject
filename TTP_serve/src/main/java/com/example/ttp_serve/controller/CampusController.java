package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CampusRequestDTO;
import com.example.ttp_serve.dto.CampusResponseDTO;
import com.example.ttp_serve.dto.CampusStatsDTO;
import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.service.CampusService;
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

import java.util.List;

@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
@Tag(name = "校区管理", description = "提供校区的增删改查及统计功能")
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @Operation(summary = "获取所有校区列表", description = "获取系统中所有校区的列表，不分页")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> getAllCampuses() {
        List<CampusResponseDTO> campuses = campusService.getAllCampuses();
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取校区列表", description = "分页获取系统中所有校区的列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<Page<CampusResponseDTO>>> getAllCampuses(
            @Parameter(description = "分页参数") Pageable pageable) {
        Page<CampusResponseDTO> campuses = campusService.getAllCampuses(pageable);
        return ResponseEntity.ok(MyApiResponse.success("获取成功", campuses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取校区信息", description = "根据校区ID获取校区的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<CampusResponseDTO> getCampusById(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        CampusResponseDTO campus = campusService.getCampusById(id);
        return ResponseEntity.ok(campus);
    }

    @PostMapping
    @Operation(summary = "创建新校区", description = "创建新校区，需要超级管理员权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或校区名称已存在"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<CampusResponseDTO> createCampus(
            @Parameter(description = "校区信息", required = true) @RequestBody CampusRequestDTO campusRequestDTO) {
        CampusResponseDTO createdCampus = campusService.createCampus(campusRequestDTO);
        return ResponseEntity.ok(createdCampus);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新校区信息", description = "更新指定校区的信息，需要管理员权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或校区名称已存在"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<CampusResponseDTO> updateCampus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id,
            @Parameter(description = "校区信息", required = true) @RequestBody CampusRequestDTO campusRequestDTO) {
        CampusResponseDTO updatedCampus = campusService.updateCampus(id, campusRequestDTO);
        return ResponseEntity.ok(updatedCampus);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除校区", description = "删除指定校区，需要超级管理员权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "校区下有子校区或用户，无法删除"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> deleteCampus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        campusService.deleteCampus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/top-level")
    @Operation(summary = "获取顶级校区", description = "获取所有无父校区的顶级校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> getTopLevelCampuses() {
        List<CampusResponseDTO> campuses = campusService.getTopLevelCampuses();
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "获取子校区", description = "获取指定校区的所有子校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> getChildCampuses(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        List<CampusResponseDTO> campuses = campusService.getChildCampuses(id);
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索校区", description = "根据名称搜索校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> searchCampuses(
            @Parameter(description = "校区名称", required = true) @RequestParam String name) {
        List<CampusResponseDTO> campuses = campusService.searchCampusesByName(name);
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/{id}/with-children")
    @Operation(summary = "获取校区及其子校区", description = "获取指定校区及其所有子校区（递归）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> getCampusWithChildren(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        List<CampusResponseDTO> campuses = campusService.getCampusWithChildren(id);
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取校区树", description = "获取完整的校区树形结构")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<CampusResponseDTO>> getCampusTree() {
        List<CampusResponseDTO> campuses = campusService.getCampusTree();
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取校区统计信息", description = "获取指定校区的统计信息，包括用户数量、学员数量等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<CampusStatsDTO> getCampusStats(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        CampusStatsDTO stats = campusService.getCampusStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/count")
    @Operation(summary = "获取校区总数", description = "获取系统中所有校区的总数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Long> getTotalCampusCount() {
        Long count = campusService.countCampuses();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/count/children")
    @Operation(summary = "获取子校区数量", description = "获取指定校区的所有子校区数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Long> getChildCampusCount(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        Long count = campusService.countChildCampuses(id);
        return ResponseEntity.ok(count);
    }
}