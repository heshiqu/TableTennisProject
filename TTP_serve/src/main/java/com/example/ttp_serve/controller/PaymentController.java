package com.example.ttp_serve.controller;

import com.example.ttp_serve.dto.MyApiResponse;
import com.example.ttp_serve.dto.PaymentDTO;
import com.example.ttp_serve.dto.PaymentRequestDTO;
import com.example.ttp_serve.entity.Payment;
import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentStatus;
import com.example.ttp_serve.enums.PaymentType;
import com.example.ttp_serve.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付管理控制器
 * 提供支付记录创建、查询、状态更新和支付处理等功能
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "提供支付记录创建、查询、状态更新和支付处理等功能")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付记录
     *
     * @param paymentRequest 支付请求信息
     * @return 创建的支付记录DTO
     *
     * @apiNote 创建一条支付记录，可以是充值、课程费用、退款或比赛报名费
     *          系统会自动生成订单号并设置初始状态为待支付
     */
    @Operation(
            summary = "创建支付记录",
            description = "创建一条支付记录，可以是充值、课程费用、退款或比赛报名费。系统会自动生成订单号并设置初始状态为待支付",
            responses = {
                    @ApiResponse(responseCode = "200", description = "支付记录创建成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping
    public ResponseEntity<MyApiResponse<PaymentDTO>> createPayment(
            @Parameter(description = "支付信息", required = true)
            @Valid @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            Payment createdPayment = paymentService.createPaymentFromRequest(paymentRequest);
            return ResponseEntity.ok(MyApiResponse.success("支付记录创建成功", convertToDTO(createdPayment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 生成支付订单
     *
     * @param type 支付类型
     * @param method 支付方式
     * @param userId 用户ID
     * @param amount 金额
     * @param relatedId 关联ID
     * @return 生成的支付订单DTO
     *
     * @apiNote 根据参数生成支付订单，适用于各种支付场景
     *          支付类型包括：充值、课程费用、退款、比赛报名费
     *          支付方式包括：微信、支付宝、线下
     */
    @Operation(
            summary = "生成支付订单",
            description = "根据参数生成支付订单，适用于各种支付场景。支付类型包括：充值、课程费用、退款、比赛报名费。支付方式包括：微信、支付宝、线下",
            responses = {
                    @ApiResponse(responseCode = "200", description = "支付订单生成成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/generate")
    public ResponseEntity<MyApiResponse<PaymentDTO>> generatePaymentOrder(
            @Parameter(description = "支付类型", required = true) @RequestParam PaymentType type,
            @Parameter(description = "支付方式", required = true) @RequestParam PaymentMethod method,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "金额", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "关联ID") @RequestParam(required = false) Long relatedId) {

        try {
            Payment payment = paymentService.generatePaymentOrder(type, method, userId, amount, relatedId);
            return ResponseEntity.ok(MyApiResponse.success("支付订单生成成功", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 更新支付状态
     *
     * @param id 支付记录ID
     * @param status 支付状态
     * @return 更新后的支付记录DTO
     *
     * @apiNote 更新支付记录的状态
     *          状态包括：待支付、成功、失败
     *          状态变更时会触发相应的业务逻辑处理
     */
    @Operation(
            summary = "更新支付状态",
            description = "更新支付记录的状态。状态包括：待支付、成功、失败。状态变更时会触发相应的业务逻辑处理",
            responses = {
                    @ApiResponse(responseCode = "200", description = "支付状态更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<MyApiResponse<PaymentDTO>> updatePaymentStatus(
            @Parameter(description = "支付记录ID", required = true) @PathVariable Long id,
            @Parameter(description = "支付状态", required = true) @RequestParam PaymentStatus status) {

        try {
            Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(MyApiResponse.success("支付状态更新成功", convertToDTO(updatedPayment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 处理支付成功
     *
     * @param orderId 订单号
     * @return 处理后的支付记录DTO
     *
     * @apiNote 处理支付成功的回调
     *          根据支付类型执行相应的业务逻辑
     *          如充值则增加余额，退款则返还金额等
     */
    @Operation(
            summary = "处理支付成功",
            description = "处理支付成功的回调。根据支付类型执行相应的业务逻辑，如充值则增加余额，退款则返还金额等",
            responses = {
                    @ApiResponse(responseCode = "200", description = "支付成功处理完成"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/{orderId}/success")
    public ResponseEntity<MyApiResponse<PaymentDTO>> processPaymentSuccess(
            @Parameter(description = "订单号", required = true) @PathVariable String orderId) {
        try {
            Payment payment = paymentService.processPaymentSuccess(orderId);
            return ResponseEntity.ok(MyApiResponse.success("支付成功处理完成", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 处理支付失败
     *
     * @param orderId 订单号
     * @return 处理后的支付记录DTO
     *
     * @apiNote 处理支付失败的回调
     *          将支付状态更新为失败
     */
    @Operation(
            summary = "处理支付失败",
            description = "处理支付失败的回调。将支付状态更新为失败",
            responses = {
                    @ApiResponse(responseCode = "200", description = "支付失败处理完成"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/{orderId}/failure")
    public ResponseEntity<MyApiResponse<PaymentDTO>> processPaymentFailure(
            @Parameter(description = "订单号", required = true) @PathVariable String orderId) {
        try {
            Payment payment = paymentService.processPaymentFailure(orderId);
            return ResponseEntity.ok(MyApiResponse.success("支付失败处理完成", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 处理充值支付
     *
     * @param orderId 订单号
     * @return 处理后的支付记录DTO
     *
     * @apiNote 专门处理充值类型的支付成功
     *          会增加用户的账户余额
     */
    @Operation(
            summary = "处理充值支付",
            description = "专门处理充值类型的支付成功。会增加用户的账户余额",
            responses = {
                    @ApiResponse(responseCode = "200", description = "充值处理完成"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/{orderId}/recharge")
    public ResponseEntity<MyApiResponse<PaymentDTO>> processRechargePayment(
            @Parameter(description = "订单号", required = true) @PathVariable String orderId) {
        try {
            Payment payment = paymentService.processRechargePayment(orderId);
            return ResponseEntity.ok(MyApiResponse.success("充值处理完成", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 处理退款
     *
     * @param orderId 订单号
     * @return 处理后的支付记录DTO
     *
     * @apiNote 处理退款类型的支付
     *          会将金额返还到用户账户
     */
    @Operation(
            summary = "处理退款",
            description = "处理退款类型的支付。会将金额返还到用户账户",
            responses = {
                    @ApiResponse(responseCode = "200", description = "退款处理完成"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<MyApiResponse<PaymentDTO>> processRefund(
            @Parameter(description = "订单号", required = true) @PathVariable String orderId) {
        try {
            Payment payment = paymentService.processRefund(orderId);
            return ResponseEntity.ok(MyApiResponse.success("退款处理完成", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取支付记录
     *
     * @param id 支付记录ID
     * @return 支付记录详情DTO
     *
     * @apiNote 根据ID获取支付记录的详细信息
     */
    @Operation(
            summary = "获取支付记录",
            description = "根据ID获取支付记录的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse<PaymentDTO>> getPayment(
            @Parameter(description = "支付记录ID", required = true) @PathVariable Long id) {
        try {
            Payment payment = paymentService.getPayment(id);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 根据订单号获取支付记录
     *
     * @param orderId 订单号
     * @return 支付记录详情DTO
     *
     * @apiNote 根据订单号获取支付记录的详细信息
     */
    @Operation(
            summary = "根据订单号获取支付记录",
            description = "根据订单号获取支付记录的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/order/{orderId}")
    public ResponseEntity<MyApiResponse<PaymentDTO>> getPaymentByOrderId(
            @Parameter(description = "订单号", required = true) @PathVariable String orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", convertToDTO(payment)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取用户的支付记录（分页）
     *
     * @param userId 用户ID
     * @param page 页码（从0开始，默认为0）
     * @param size 每页数量（默认为10）
     * @param sort 排序字段（可选，如：createdAt,desc）
     * @return 分页的支付记录DTO
     *
     * @apiNote 获取指定用户的分页支付记录，支持排序和分页参数
     */
    @Operation(
            summary = "获取用户的支付记录（分页）",
            description = "获取指定用户的分页支付记录，支持排序和分页参数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getUserPayments(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(required = false) String sort) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            if (sort != null && !sort.isEmpty()) {
                String[] sortParams = sort.split(",");
                Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                        ? Sort.Direction.DESC : Sort.Direction.ASC;
                pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            }
            
            Page<Payment> payments = paymentService.getUserPayments(userId, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定状态的支付记录（分页）
     *
     * @param status 支付状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序方式（如：createdAt,desc）
     * @return 特定状态的支付记录分页DTO
     *
     * @apiNote 获取系统中所有特定状态的支付记录，支持分页和排序
     *          状态包括：待支付、成功、失败
     */
    @Operation(
            summary = "获取特定状态的支付记录（分页）",
            description = "获取系统中所有特定状态的支付记录，支持分页和排序。状态包括：待支付、成功、失败",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getPaymentsByStatus(
            @Parameter(description = "支付状态", required = true) @PathVariable PaymentStatus status,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<Payment> payments = paymentService.getPaymentsByStatus(status, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定类型的支付记录（分页）
     *
     * @param type 支付类型
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序方式（如：createdAt,desc）
     * @return 特定类型的支付记录分页DTO
     *
     * @apiNote 获取系统中所有特定类型的支付记录，支持分页和排序
     *          类型包括：充值、课程费用、退款、比赛报名费
     */
    @Operation(
            summary = "获取特定类型的支付记录（分页）",
            description = "获取系统中所有特定类型的支付记录，支持分页和排序。类型包括：充值、课程费用、退款、比赛报名费",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/type/{type}")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getPaymentsByType(
            @Parameter(description = "支付类型", required = true) @PathVariable PaymentType type,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<Payment> payments = paymentService.getPaymentsByType(type, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定方式的支付记录（分页）
     *
     * @param method 支付方式
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序方式（如：createdAt,desc）
     * @return 特定方式的支付记录分页DTO
     *
     * @apiNote 获取系统中所有特定支付方式的记录，支持分页和排序
     *          方式包括：微信、支付宝、线下
     */
    @Operation(
            summary = "获取特定方式的支付记录（分页）",
            description = "获取系统中所有特定支付方式的记录，支持分页和排序。方式包括：微信、支付宝、线下",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/method/{method}")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getPaymentsByMethod(
            @Parameter(description = "支付方式", required = true) @PathVariable PaymentMethod method,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<Payment> payments = paymentService.getPaymentsByMethod(method, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取日期范围内的支付记录（分页）
     *
     * @param start 开始时间
     * @param end 结束时间
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序方式（如：createdAt,desc）
     * @return 日期范围内的支付记录分页DTO
     *
     * @apiNote 获取指定时间范围内的所有支付记录，支持分页和排序
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @Operation(
            summary = "获取日期范围内的支付记录（分页）",
            description = "获取指定时间范围内的所有支付记录，支持分页和排序。时间格式：yyyy-MM-dd HH:mm:ss",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/date-range")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getPaymentsByDateRange(
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<Payment> payments = paymentService.getPaymentsByDateRange(start, end, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取用户在日期范围内的支付记录（分页）
     *
     * @param userId 用户ID
     * @param start 开始时间
     * @param end 结束时间
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序方式（如：createdAt,desc）
     * @return 用户在日期范围内的支付记录分页DTO
     *
     * @apiNote 获取指定用户在指定时间范围内的支付记录，支持分页和排序
     *          时间格式：yyyy-MM-dd HH:mm:ss
     */
    @Operation(
            summary = "获取用户在日期范围内的支付记录（分页）",
            description = "获取指定用户在指定时间范围内的支付记录，支持分页和排序。时间格式：yyyy-MM-dd HH:mm:ss",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getUserPaymentsByDateRange(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @Parameter(description = "页码（从0开始）", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式（如：createdAt,desc）", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<Payment> payments = paymentService.getUserPaymentsByDateRange(userId, start, end, pageable);
            Page<PaymentDTO> paymentDTOS = payments.map(this::convertToDTO);
            
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 分页获取支付记录
     *
     * @param pageable 分页参数
     * @return 分页的支付记录DTO
     *
     * @apiNote 获取分页的支付记录，支持排序和分页参数
     */
    @Operation(
            summary = "分页获取支付记录",
            description = "获取分页的支付记录，支持排序和分页参数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/page")
    public ResponseEntity<MyApiResponse<Page<PaymentDTO>>> getPayments(
            @Parameter(description = "分页参数") Pageable pageable) {
        try {
            Page<Payment> payments = paymentService.getPayments(pageable);
            Page<PaymentDTO> paymentDTOs = payments.map(this::convertToDTO);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", paymentDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取用户支付总额
     *
     * @param userId 用户ID
     * @return 用户的支付总额
     *
     * @apiNote 获取指定用户的所有成功支付的总额
     */
    @Operation(
            summary = "获取用户支付总额",
            description = "获取指定用户的所有成功支付的总额",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<MyApiResponse<BigDecimal>> getUserTotalPayment(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            BigDecimal total = paymentService.getUserTotalPayment(userId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取特定类型的支付总额
     *
     * @param type 支付类型
     * @return 特定类型的支付总额
     *
     * @apiNote 获取系统中所有特定类型支付的成功总额
     */
    @Operation(
            summary = "获取特定类型的支付总额",
            description = "获取系统中所有特定类型支付的成功总额",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/type/{type}/total")
    public ResponseEntity<MyApiResponse<BigDecimal>> getTotalPaymentByType(
            @Parameter(description = "支付类型", required = true) @PathVariable PaymentType type) {
        try {
            BigDecimal total = paymentService.getTotalPaymentByType(type);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 获取校区支付总额
     *
     * @param campusId 校区ID
     * @return 校区的支付总额
     *
     * @apiNote 获取指定校区所有用户成功支付的总额
     */
    @Operation(
            summary = "获取校区支付总额",
            description = "获取指定校区所有用户成功支付的总额",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/campus/{campusId}/total")
    public ResponseEntity<MyApiResponse<BigDecimal>> getCampusTotalPayment(
            @Parameter(description = "校区ID", required = true) @PathVariable Long campusId) {
        try {
            BigDecimal total = paymentService.getCampusTotalPayment(campusId);
            return ResponseEntity.ok(MyApiResponse.success("获取成功", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 统计支付记录数量
     *
     * @return 支付记录总数
     *
     * @apiNote 获取系统中的支付记录总数
     */
    @Operation(
            summary = "统计支付记录数量",
            description = "获取系统中的支付记录总数",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数无效或业务逻辑错误")
            }
    )
    @GetMapping("/count")
    public ResponseEntity<MyApiResponse<Long>> countPayments() {
        try {
            Long count = paymentService.countPayments();
            return ResponseEntity.ok(MyApiResponse.success("获取成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MyApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 将Payment实体转换为PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUser().getId());
        dto.setUserName(payment.getUser().getRealName());
        dto.setAmount(payment.getAmount());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setOrderId(payment.getOrderId());
        dto.setStatus(payment.getStatus());
        dto.setRelatedId(payment.getRelatedId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        return dto;
    }

    /**
     * 将Payment实体列表转换为PaymentDTO列表
     */
    private List<PaymentDTO> convertToDTOList(List<Payment> payments) {
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}