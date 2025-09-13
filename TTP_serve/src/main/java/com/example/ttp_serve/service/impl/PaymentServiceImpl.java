package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.dto.PaymentRequestDTO;
import com.example.ttp_serve.entity.*;
import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentStatus;
import com.example.ttp_serve.enums.PaymentType;
import com.example.ttp_serve.exception.BusinessException;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.*;
import com.example.ttp_serve.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.ttp_serve.enums.EnrollmentStatus.PAID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ContestEnrollmentRepository contestEnrollmentRepository;

    @Override
    @Transactional
    public Payment createPaymentFromRequest(PaymentRequestDTO paymentRequest) {
        // 检查用户是否存在
        User user = userRepository.findById(paymentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + paymentRequest.getUserId() + "' 不存在"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentType(paymentRequest.getPaymentType());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setOrderId(generateOrderId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRelatedId(paymentRequest.getRelatedId());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    // 原有方法保持不变，但需要将createPayment方法标记为过时或删除
    @Override
    @Transactional
    @Deprecated
    public Payment createPayment(Payment payment) {
        // 保持原有实现，但标记为过时
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setUserId(payment.getUser().getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setRelatedId(payment.getRelatedId());

        return createPaymentFromRequest(dto);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("支付记录ID '" + id + "' 不存在"));

        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("支付记录ID '" + id + "' 不存在"));
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));
    }

    @Override
    public List<Payment> getUserPayments(Long userId) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Page<Payment> getUserPayments(Long userId, Pageable pageable) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return paymentRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public Page<Payment> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable);
    }

    @Override
    public List<Payment> getPaymentsByType(PaymentType type) {
        return paymentRepository.findByPaymentType(type);
    }

    @Override
    public Page<Payment> getPaymentsByType(PaymentType type, Pageable pageable) {
        return paymentRepository.findByPaymentType(type, pageable);
    }

    @Override
    public List<Payment> getPaymentsByMethod(PaymentMethod method) {
        return paymentRepository.findByPaymentMethod(method);
    }

    @Override
    public Page<Payment> getPaymentsByMethod(PaymentMethod method, Pageable pageable) {
        return paymentRepository.findByPaymentMethod(method, pageable);
    }

    @Override
    public List<Payment> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public Page<Payment> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return paymentRepository.findByCreatedAtBetween(start, end, pageable);
    }

    @Override
    public List<Payment> getUserPaymentsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return paymentRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    public Page<Payment> getUserPaymentsByDateRange(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return paymentRepository.findByUserIdAndCreatedAtBetween(userId, start, end, pageable);
    }

    @Override
    public Page<Payment> getPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Payment processPaymentSuccess(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());

        // 根据支付类型处理相应的业务逻辑
        switch (payment.getPaymentType()) {
            case RECHARGE:
                processRechargePayment(orderId);
                break;
            case COURSE_FEE:
                processCoursePayment(orderId);
                break;
            case REFUND:
                processRefund(orderId);
                break;
            case CONTEST_FEE:
                processContestPayment(orderId);
                break;
        }

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processPaymentFailure(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    public BigDecimal getUserTotalPayment(Long userId) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户ID '" + userId + "' 不存在");
        }

        return paymentRepository.sumAmountByUserId(userId);
    }

    @Override
    public BigDecimal getTotalPaymentByType(PaymentType type) {
        return paymentRepository.sumAmountByPaymentType(type);
    }

    @Override
    public BigDecimal getCampusTotalPayment(Long campusId) {
        return paymentRepository.sumAmountByCampusId(campusId);
    }

    @Override
    public Long countPayments() {
        return paymentRepository.count();
    }

    @Override
    @Transactional
    public Payment generatePaymentOrder(PaymentType type, PaymentMethod method,
                                        Long userId, BigDecimal amount, Long relatedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户ID '" + userId + "' 不存在"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentType(type);
        payment.setPaymentMethod(method);
        payment.setOrderId(generateOrderId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRelatedId(relatedId);

        return createPayment(payment);
    }

    @Override
    @Transactional
    public Payment processRechargePayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        // 只有充值类型的支付才能处理
        if (payment.getPaymentType() != PaymentType.RECHARGE) {
            throw new BusinessException("非充值类型的支付记录");
        }

        // 更新学员余额
        Student student = studentRepository.findById(payment.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("学员不存在"));

        student.setBalance(student.getBalance().add(payment.getAmount()));
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);

        return payment;
    }

    @Override
    @Transactional
    public Payment processCoursePayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        // 只有课程费用类型的支付才能处理
        if (payment.getPaymentType() != PaymentType.COURSE_FEE) {
            throw new BusinessException("非课程费用类型的支付记录");
        }

        // 这里课程费用的扣款在课程确认时已经完成
        // 此方法主要用于标记支付状态

        return payment;
    }

    @Override
    @Transactional
    public Payment processRefund(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        // 只有退款类型的支付才能处理
        if (payment.getPaymentType() != PaymentType.REFUND) {
            throw new BusinessException("非退款类型的支付记录");
        }

        // 更新学员余额
        Student student = studentRepository.findById(payment.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("学员不存在"));

        student.setBalance(student.getBalance().add(payment.getAmount()));
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);

        return payment;
    }

    @Override
    @Transactional
    public Payment processContestPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单号 '" + orderId + "' 不存在"));

        // 只有比赛报名费类型的支付才能处理
        if (payment.getPaymentType() != PaymentType.CONTEST_FEE) {
            throw new BusinessException("非比赛报名费类型的支付记录");
        }

        // 更新比赛报名状态
        if (payment.getRelatedId() != null) {
            ContestEnrollment enrollment = contestEnrollmentRepository.findById(payment.getRelatedId())
                    .orElseThrow(() -> new ResourceNotFoundException("比赛报名记录不存在"));

            // 标记报名已支付
            // 这里需要根据实际业务逻辑更新报名状态
            enrollment.setStatus(PAID);
            contestEnrollmentRepository.save(enrollment);
        }

        return payment;
    }

    // 生成订单号
    private String generateOrderId() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}