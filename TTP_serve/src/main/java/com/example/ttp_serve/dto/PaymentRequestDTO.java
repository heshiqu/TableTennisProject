package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.PaymentMethod;
import com.example.ttp_serve.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private Long userId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private Long relatedId; // 用于关联课程ID或比赛ID
}