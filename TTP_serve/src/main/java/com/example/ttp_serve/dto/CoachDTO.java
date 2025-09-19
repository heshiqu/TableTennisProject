package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoachDTO {
    private Long id;
    private String username;
    private String realName;
    private Gender gender;
    private Integer age;
    private String phone;
    private String email;
    private String avatar; // 教练照片
    private UserType userType; // 用户类型
    private Long campusId; // 校区ID
    private String campusName; // 校区名称
    private UserStatus status; // 用户状态
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    // 教练特有字段
    private CoachLevel level;
    private String awards; // 获奖信息
    private BigDecimal hourlyRate;
    private Integer currentStudents;
    private Integer maxStudents;
}