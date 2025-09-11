package com.example.ttp_serve.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampusDto {

    private Long id;
    private String name;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计信息
    private Long userCount;
    private Long studentCount;
    private Long coachCount;
    private Long courseCount;
}