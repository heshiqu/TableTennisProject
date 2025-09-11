package com.example.ttp_serve.controller;

import com.example.ttp_serve.entity.Campus;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
@Tag(name = "校区管理", description = "提供校区的增删改查及统计功能")
public class CampusController {

    private final CampusService campusService;

    /**
     * 获取所有校区列表（不分页）
     * 用于超级管理员查看系统中所有校区信息
     * 参考文档：超级管理员录入每个校区的基本信息
     */
    @GetMapping
    @Operation(summary = "获取所有校区列表", description = "获取系统中所有校区的列表，不分页")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> getAllCampuses() {
        List<Campus> campuses = campusService.getAllCampuses();
        return ResponseEntity.ok(campuses);
    }

    /**
     * 分页获取所有校区列表
     * 用于前端分页展示校区信息
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取校区列表", description = "分页获取系统中所有校区的列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Page<Campus>> getAllCampuses(
            @Parameter(description = "分页参数") Pageable pageable) {
        Page<Campus> campuses = campusService.getAllCampuses(pageable);
        return ResponseEntity.ok(campuses);
    }

    /**
     * 根据ID获取指定校区信息
     * 用于查看某个校区的详细信息，如名称、地址、联系人等
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取校区信息", description = "根据校区ID获取校区的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Campus> getCampusById(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        Optional<Campus> campus = campusService.getCampusById(id);
        return campus.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新校区
     * 超级管理员用于录入新校区的基本信息，如名字、地址、联系方式等
     * 参考文档：超级管理员录入每个校区的基本信息
     */
    @PostMapping
    @Operation(summary = "创建新校区", description = "创建新校区，需要超级管理员权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或校区名称已存在"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Campus> createCampus(
            @Parameter(description = "校区信息", required = true) @RequestBody Campus campus) {
        Campus createdCampus = campusService.createCampus(campus);
        return ResponseEntity.ok(createdCampus);
    }

    /**
     * 更新指定校区信息
     * 超级管理员或校区管理员可修改校区基本信息
     * 参考文档：校区管理员可以对教练员信息进行管理（修改）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新校区信息", description = "更新指定校区的信息，需要管理员权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或校区名称已存在"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Campus> updateCampus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id,
            @Parameter(description = "校区信息", required = true) @RequestBody Campus campus) {
        Campus updatedCampus = campusService.updateCampus(id, campus);
        return ResponseEntity.ok(updatedCampus);
    }

    /**
     * 删除指定校区
     * 超级管理员可删除无子校区、无用户的校区
     */
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

    /**
     * 获取所有顶级校区（无父校区的校区）
     * 用于展示中心校区或独立校区
     */
    @GetMapping("/top-level")
    @Operation(summary = "获取顶级校区", description = "获取所有无父校区的顶级校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> getTopLevelCampuses() {
        List<Campus> campuses = campusService.getTopLevelCampuses();
        return ResponseEntity.ok(campuses);
    }

    /**
     * 获取指定校区的所有子校区
     * 用于查看某个中心校区下的所有分校区
     * 参考文档：超级管理员指定中心校区和若干分校区
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "获取子校区", description = "获取指定校区的所有子校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> getChildCampuses(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        List<Campus> campuses = campusService.getChildCampuses(id);
        return ResponseEntity.ok(campuses);
    }

    /**
     * 根据名称搜索校区
     * 用于快速定位某个校区
     */
    @GetMapping("/search")
    @Operation(summary = "搜索校区", description = "根据名称搜索校区")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> searchCampuses(
            @Parameter(description = "校区名称", required = true) @RequestParam String name) {
        List<Campus> campuses = campusService.searchCampusesByName(name);
        return ResponseEntity.ok(campuses);
    }

    /**
     * 获取指定校区及其所有子校区（递归）
     * 用于展示校区树形结构中的某一条分支
     */
    @GetMapping("/{id}/with-children")
    @Operation(summary = "获取校区及其子校区", description = "获取指定校区及其所有子校区（递归）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> getCampusWithChildren(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        List<Campus> campuses = campusService.getCampusWithChildren(id);
        return ResponseEntity.ok(campuses);
    }

    /**
     * 获取完整的校区树形结构
     * 用于展示系统中所有校区的层级关系
     */
    @GetMapping("/tree")
    @Operation(summary = "获取校区树", description = "获取完整的校区树形结构")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<Campus>> getCampusTree() {
        List<Campus> campuses = campusService.getCampusTree();
        return ResponseEntity.ok(campuses);
    }

    /**
     * 获取指定校区的统计信息
     * 包括用户数量、学员数量、教练数量、管理员数量、子校区数量等
     * 用于校区管理员或超级管理员查看校区运营情况
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "获取校区统计信息", description = "获取指定校区的统计信息，包括用户数量、学员数量等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<CampusService.CampusStats> getCampusStats(
            @Parameter(description = "校区ID", required = true) @PathVariable Long id) {
        CampusService.CampusStats stats = campusService.getCampusStats(id);
        return ResponseEntity.ok(stats);
    }
}