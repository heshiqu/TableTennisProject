package com.example.ttp_serve.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampusRequestDTO {
    @NotBlank(message = "校区名称不能为空")
    private String name;

    private String address;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private Long parentId;
}