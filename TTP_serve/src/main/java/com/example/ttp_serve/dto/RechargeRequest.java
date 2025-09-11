package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class RechargeRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;
}