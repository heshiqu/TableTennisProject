package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 教练特有字段
    private String level;
    private String awards;
    private String hourlyRate;

    // 学员特有字段
    private String balance;
    private Integer cancelCount;
}