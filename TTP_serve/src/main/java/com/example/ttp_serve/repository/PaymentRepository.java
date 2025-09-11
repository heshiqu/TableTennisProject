package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Payment;
import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentStatus;
import com.example.ttp_serve.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 根据用户ID查找支付记录
    List<Payment> findByUserId(Long userId);

    // 根据状态查找支付记录
    List<Payment> findByStatus(PaymentStatus status);

    // 根据类型查找支付记录
    List<Payment> findByPaymentType(PaymentType paymentType);

    // 根据方式查找支付记录
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    // 根据订单号查找支付记录
    Optional<Payment> findByOrderId(String orderId);

    Payment findByRelatedId(Long relatedId);

    // 根据用户ID和类型查找支付记录
    List<Payment> findByUserIdAndPaymentType(Long userId, PaymentType paymentType);

    // 根据用户ID和状态查找支付记录
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    // 根据日期范围查找支付记录
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 根据用户ID和日期范围查找支付记录
    List<Payment> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 统计用户支付总额
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.status = 'SUCCESS'")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    // 统计特定类型的支付总额
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :paymentType AND p.status = 'SUCCESS'")
    BigDecimal sumAmountByPaymentType(@Param("paymentType") PaymentType paymentType);

    // 统计校区内的支付总额
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.campus.id = :campusId AND p.status = 'SUCCESS'")
    BigDecimal sumAmountByCampusId(@Param("campusId") Long campusId);

    // 分页查询支付记录
    Page<Payment> findAll(Pageable pageable);
}