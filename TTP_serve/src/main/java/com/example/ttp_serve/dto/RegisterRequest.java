package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserType;
import lombok.Data;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "密码必须包含字母、数字和特殊字符，长度为8-16位"
    )
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    private Gender gender;

    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于120")
    private Integer age;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "用户类型不能为空")
    private UserType userType;

    private Long campusId;

    // 教练特有字段
    private CoachLevel level;
    private String awards;

    // 学员特有字段
    private BigDecimal initialBalance;
    
    // 头像URL
    private String avatar;
}