package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.CoachLevel;
import com.example.ttp_serve.enums.Gender;
import lombok.Data;
import java.math.BigDecimal;

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
    private CoachLevel level;
    private String awards; // 获奖信息
    private BigDecimal hourlyRate;
    private Integer currentStudents;
    private Integer maxStudents;
}