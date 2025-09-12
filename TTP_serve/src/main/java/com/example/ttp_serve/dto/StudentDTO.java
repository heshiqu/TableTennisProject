package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生数据传输对象
 * 用于返回学生个人信息
 */
@Data
public class StudentDTO {
    private Long id;
    private String username;
    private String realName;
    private Gender gender;
    private Integer age;
    private String phone;
    private String email;
    private String avatar;
    private UserType userType;
    private Long campusId;
    private String campusName;
    private UserStatus status;
    private BigDecimal balance;
    private Integer cancelCount;
    private LocalDate lastCancelMonth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}