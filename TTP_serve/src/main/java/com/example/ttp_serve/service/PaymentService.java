package com.example.ttp_serve.service;

import com.example.ttp_serve.dto.PaymentRequestDTO;
import com.example.ttp_serve.entity.Payment;
import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentStatus;
import com.example.ttp_serve.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    // 创建支付记录
    Payment createPayment(Payment payment);

    // 更新支付状态
    Payment updatePaymentStatus(Long id, PaymentStatus status);

    // 获取支付记录
    Payment getPayment(Long id);

    // 根据订单号获取支付记录
    Payment getPaymentByOrderId(String orderId);

    // 获取用户的支付记录
    List<Payment> getUserPayments(Long userId);

    // 分页获取用户的支付记录
    Page<Payment> getUserPayments(Long userId, Pageable pageable);

    // 获取特定状态的支付记录
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    
    // 分页获取特定状态的支付记录
    Page<Payment> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    // 获取特定类型的支付记录
    List<Payment> getPaymentsByType(PaymentType type);
    
    // 分页获取特定类型的支付记录
    Page<Payment> getPaymentsByType(PaymentType type, Pageable pageable);

    // 获取特定方式的支付记录
    List<Payment> getPaymentsByMethod(PaymentMethod method);
    
    // 分页获取特定方式的支付记录
    Page<Payment> getPaymentsByMethod(PaymentMethod method, Pageable pageable);

    // 获取日期范围内的支付记录
    List<Payment> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end);
    
    // 分页获取日期范围内的支付记录
    Page<Payment> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 获取用户在日期范围内的支付记录
    List<Payment> getUserPaymentsByDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    
    // 分页获取用户在日期范围内的支付记录
    Page<Payment> getUserPaymentsByDateRange(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 分页获取支付记录
    Page<Payment> getPayments(Pageable pageable);

    // 处理支付成功
    Payment processPaymentSuccess(String orderId);

    // 处理支付失败
    Payment processPaymentFailure(String orderId);

    // 获取用户支付总额
    BigDecimal getUserTotalPayment(Long userId);

    // 获取特定类型的支付总额
    BigDecimal getTotalPaymentByType(PaymentType type);

    // 获取校区支付总额
    BigDecimal getCampusTotalPayment(Long campusId);

    // 统计支付记录数量
    Long countPayments();

    // 生成支付订单
    Payment generatePaymentOrder(PaymentType type, PaymentMethod method,
                                 Long userId, BigDecimal amount, Long relatedId);

    // 处理充值支付
    Payment processRechargePayment(String orderId);

    // 处理课程费用支付
    Payment processCoursePayment(String orderId);

    // 处理退款
    Payment processRefund(String orderId);

    // 处理比赛报名费支付
    Payment processContestPayment(String orderId);

    // 使用DTO创建支付记录
    Payment createPaymentFromRequest(PaymentRequestDTO paymentRequest);
}