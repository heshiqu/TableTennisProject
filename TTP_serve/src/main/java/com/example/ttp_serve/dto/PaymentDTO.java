package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentStatus;
import com.example.ttp_serve.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private String orderId;
    private PaymentStatus status;
    private Long relatedId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}