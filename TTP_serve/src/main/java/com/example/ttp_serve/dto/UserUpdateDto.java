package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserStatus;
import lombok.Data;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
public class UserUpdateDto {

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    private Gender gender;

    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于120")
    private Integer age;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private Long campusId;

    private UserStatus status;

    // 教练特有字段
    private CoachLevel level;
    private String awards;
    private BigDecimal hourlyRate;
    private Integer maxStudents;

    // 学员特有字段
    private BigDecimal balance;
}