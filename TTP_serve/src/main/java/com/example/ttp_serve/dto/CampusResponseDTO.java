package com.example.ttp_serve.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampusResponseDTO {
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
    private List<CampusResponseDTO> children;

    // 统计信息
    private Long userCount;
    private Long studentCount;
    private Long coachCount;
    private Long adminCount;
    private Long childCampusCount;
}