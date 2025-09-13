package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.CourtDTO;
import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.Court;
import com.example.ttp_serve.service.CourtService;
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
 * 球台管理控制器
 * 提供球台查询、统计和管理等功能
 */
@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
@Tag(name = "球台管理", description = "提供球台查询、统计和管理等功能")
public class CourtController {

    private final CourtService courtService;

    /**
     * 获取指定校区的所有球台
     *
     * @param campusId 校区ID
     * @return 该校区下的所有球台列表
     *
     * @apiNote 根据校区ID获取该校区下的所有球台信息
     */
    @GetMapping("/campus/{campusId}")
    @Operation(summary = "获取校区球台", description = "根据校区ID获取该校区下的所有球台")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourtDTO>>> getCourtsByCampusId(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId) {
        try {
            List<Court> courts = courtService.getCourtsByCampusId(campusId);
            List<CourtDTO> courtDTOs = courts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取所有球台
     *
     * @return 所有球台列表
     *
     * @apiNote 获取系统中的所有球台信息
     */
    @GetMapping
    @Operation(summary = "获取所有球台", description = "获取系统中的所有球台")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourtDTO>>> getAllCourts() {
        try {
            List<Court> courts = courtService.getAllCourts();
            List<CourtDTO> courtDTOs = courts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据状态获取球台
     *
     * @param status 球台状态(AVAILABLE/OCCUPIED/MAINTENANCE)
     * @return 指定状态的球台列表
     *
     * @apiNote 根据球台状态获取对应的球台列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取球台", description = "根据球台状态获取对应的球台列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "状态参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourtDTO>>> getCourtsByStatus(
            @Parameter(description = "球台状态", required = true) @PathVariable String status) {
        try {
            List<Court> courts = courtService.getCourtsByStatus(status);
            List<CourtDTO> courtDTOs = courts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTOs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据校区和状态获取球台
     *
     * @param campusId 校区ID
     * @param status 球台状态
     * @return 指定校区和状态的球台列表
     *
     * @apiNote 根据校区ID和球台状态获取对应的球台列表
     */
    @GetMapping("/campus/{campusId}/status/{status}")
    @Operation(summary = "获取校区状态球台", description = "根据校区ID和球台状态获取对应的球台列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "状态参数错误"),
            @ApiResponse(responseCode = "404", description = "校区不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<List<CourtDTO>>> getCourtsByCampusIdAndStatus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId,
            @Parameter(description = "球台状态", required = true) @PathVariable String status) {
        try {
            List<Court> courts = courtService.getCourtsByCampusIdAndStatus(campusId, status);
            List<CourtDTO> courtDTOs = courts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTOs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取球台详情
     *
     * @param id 球台ID
     * @return 球台详细信息
     *
     * @apiNote 根据球台ID获取球台的详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取球台详情", description = "根据球台ID获取球台的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "球台不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourtDTO>> getCourtById(
            @Parameter(description = "球台ID", required = true) @PathVariable Long id) {
        try {
            Court court = courtService.getCourtById(id);
            CourtDTO courtDTO = convertToDTO(court);

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据球台编号获取球台
     *
     * @param courtNumber 球台编号
     * @return 球台详细信息
     *
     * @apiNote 根据球台编号获取球台的详细信息
     */
    @GetMapping("/number/{courtNumber}")
    @Operation(summary = "根据编号获取球台", description = "根据球台编号获取球台的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "球台不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<MyApiResponse<CourtDTO>> getCourtByNumber(
            @Parameter(description = "球台编号", required = true) @PathVariable String courtNumber) {
        try {
            Court court = courtService.getCourtByNumber(courtNumber);
            CourtDTO courtDTO = convertToDTO(court);

            return ResponseEntity.ok(MyApiResponse.success("获取成功", courtDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将Court实体转换为CourtDTO
     *
     * @param court 球台实体
     * @return 球台DTO
     */
    private CourtDTO convertToDTO(Court court) {
        CourtDTO dto = new CourtDTO();
        dto.setId(court.getId());
        dto.setCampusId(court.getCampus().getId());
        dto.setCampusName(court.getCampus().getName());
        dto.setCourtNumber(court.getCourtNumber());
        dto.setStatus(court.getStatus());
        dto.setCreatedAt(court.getCreatedAt());
        dto.setUpdatedAt(court.getUpdatedAt());
        return dto;
    }
}