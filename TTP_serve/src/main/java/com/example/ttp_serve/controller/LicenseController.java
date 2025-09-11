package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.entity.SystemLicense;
import com.example.ttp_serve.enums.LicenseStatus;
import com.example.ttp_serve.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 系统许可证管理控制器
 * 提供系统许可证的创建、查询、验证和续订等功能
 */
@RestController
@RequestMapping("/api/licenses")
@Tag(name = "系统许可证管理", description = "提供系统许可证的创建、查询、验证和续订等功能")
public class LicenseController {

    private final LicenseService licenseService;

    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * 创建新的系统许可证
     * @param license 许可证实体
     * @return 创建后的许可证信息
     */
    @Operation(
            summary = "创建新的系统许可证",
            description = "创建一条新的系统许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "许可证创建成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping
    public ResponseEntity<MyApiResponse<SystemLicense>> createLicense(
            @Parameter(description = "许可证实体", required = true) @RequestBody SystemLicense license) {
        try {
            SystemLicense createdLicense = licenseService.createLicense(license);
            return ResponseEntity.ok(MyApiResponse.success("许可证创建成功", createdLicense));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新许可证信息
     * @param id 许可证ID
     * @param license 更新的许可证信息
     * @return 更新后的许可证信息
     */
    @Operation(
            summary = "更新许可证信息",
            description = "更新指定许可证的信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "许可证更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "许可证不存在")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<MyApiResponse<SystemLicense>> updateLicense(
            @Parameter(description = "许可证ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新的许可证信息", required = true) @RequestBody SystemLicense license) {
        try {
            SystemLicense updatedLicense = licenseService.updateLicense(id, license);
            return ResponseEntity.ok(MyApiResponse.success("许可证更新成功", updatedLicense));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 删除许可证
     * @param id 许可证ID
     * @return 操作结果
     */
    @Operation(
            summary = "删除许可证",
            description = "删除指定的许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "许可证删除成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "许可证不存在")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<MyApiResponse<Void>> deleteLicense(
            @Parameter(description = "许可证ID", required = true) @PathVariable Long id) {
        try {
            licenseService.deleteLicense(id);
            return ResponseEntity.ok(MyApiResponse.success("许可证删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据ID获取许可证详情
     * @param id 许可证ID
     * @return 许可证详情
     */
    @Operation(
            summary = "根据ID获取许可证详情",
            description = "根据许可证ID获取许可证的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "许可证不存在")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse<SystemLicense>> getLicense(
            @Parameter(description = "许可证ID", required = true) @PathVariable Long id) {
        try {
            SystemLicense license = licenseService.getLicense(id);
            if (license == null) {
                return ResponseEntity.ok(MyApiResponse.notFound("许可证不存在"));
            }
            return ResponseEntity.ok(MyApiResponse.success("获取成功", license));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据许可证密钥获取许可证详情
     * @param licenseKey 许可证密钥
     * @return 许可证详情
     */
    @Operation(
            summary = "根据许可证密钥获取许可证详情",
            description = "根据许可证密钥获取许可证的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "许可证不存在")
            }
    )
    @GetMapping("/key/{licenseKey}")
    public ResponseEntity<MyApiResponse<SystemLicense>> getLicenseByKey(
            @Parameter(description = "许可证密钥", required = true) @PathVariable String licenseKey) {
        try {
            SystemLicense license = licenseService.getLicenseByKey(licenseKey);
            if (license == null) {
                return ResponseEntity.ok(MyApiResponse.notFound("许可证不存在"));
            }
            return ResponseEntity.ok(MyApiResponse.success("获取成功", license));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据校区ID获取许可证列表
     * @param campusId 校区ID
     * @return 校区相关的许可证列表
     */
    @Operation(
            summary = "根据校区ID获取许可证列表",
            description = "获取指定校区的所有许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/campus/{campusId}")
    public ResponseEntity<MyApiResponse<List<SystemLicense>>> getLicensesByCampus(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId) {
        try {
            List<SystemLicense> licenses = licenseService.getLicensesByCampus(campusId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", licenses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据状态获取许可证列表
     * @param status 许可证状态
     * @return 指定状态的许可证列表
     */
    @Operation(
            summary = "根据状态获取许可证列表",
            description = "获取指定状态的所有许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<MyApiResponse<List<SystemLicense>>> getLicensesByStatus(
            @Parameter(description = "许可证状态", required = true) @PathVariable LicenseStatus status) {
        try {
            List<SystemLicense> licenses = licenseService.getLicensesByStatus(status);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", licenses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取即将过期的许可证列表
     * @param days 过期天数阈值
     * @return 即将过期的许可证列表
     */
    @Operation(
            summary = "获取即将过期的许可证列表",
            description = "获取在指定天数内即将过期的许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/expiring/{days}")
    public ResponseEntity<MyApiResponse<List<SystemLicense>>> getExpiringLicenses(
            @Parameter(description = "过期天数阈值", required = true) @PathVariable int days) {
        try {
            List<SystemLicense> licenses = licenseService.getExpiringLicenses(days);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", licenses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取已过期的许可证列表
     * @return 已过期的许可证列表
     */
    @Operation(
            summary = "获取已过期的许可证列表",
            description = "获取所有已过期的许可证记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/expired")
    public ResponseEntity<MyApiResponse<List<SystemLicense>>> getExpiredLicenses() {
        try {
            List<SystemLicense> licenses = licenseService.getExpiredLicenses();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", licenses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 验证许可证是否有效
     * @param licenseKey 许可证密钥
     * @param campusId 校区ID
     * @return 验证结果
     */
    @Operation(
            summary = "验证许可证是否有效",
            description = "验证指定许可证密钥对指定校区是否有效",
            responses = {
                    @ApiResponse(responseCode = "200", description = "验证成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/validate")
    public ResponseEntity<MyApiResponse<Boolean>> validateLicense(
            @Parameter(description = "许可证密钥", required = true) @RequestParam String licenseKey,
            @Parameter(description = "校区ID", required = true) @RequestParam Long campusId) {
        try {
            boolean isValid = licenseService.validateLicense(licenseKey, campusId);
            return ResponseEntity.ok(MyApiResponse.success("验证成功", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 检查校区是否有有效的许可证
     * @param campusId 校区ID
     * @return 检查结果
     */
    @Operation(
            summary = "检查校区是否有有效的许可证",
            description = "检查指定校区是否有有效的许可证",
            responses = {
                    @ApiResponse(responseCode = "200", description = "检查成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/campus-licensed/{campusId}")
    public ResponseEntity<MyApiResponse<Boolean>> isCampusLicensed(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId) {
        try {
            boolean isLicensed = licenseService.isCampusLicensed(campusId);
            return ResponseEntity.ok(MyApiResponse.success("检查成功", isLicensed));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 续订许可证
     * @param id 许可证ID
     * @param newExpirationDate 新的过期日期
     * @return 续订后的许可证信息
     */
    @Operation(
            summary = "续订许可证",
            description = "续订指定许可证，设置新的过期日期",
            responses = {
                    @ApiResponse(responseCode = "200", description = "许可证续订成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误"),
                    @ApiResponse(responseCode = "404", description = "许可证不存在")
            }
    )
    @PostMapping("/{id}/renew")
    public ResponseEntity<MyApiResponse<SystemLicense>> renewLicense(
            @Parameter(description = "许可证ID", required = true) @PathVariable Long id,
            @Parameter(description = "新的过期日期", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newExpirationDate) {
        try {
            SystemLicense renewedLicense = licenseService.renewLicense(id, newExpirationDate);
            return ResponseEntity.ok(MyApiResponse.success("许可证续订成功", renewedLicense));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }
}