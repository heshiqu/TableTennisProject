package com.example.ttp_serve.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemLogDto {

    private Long id;
    private Long userId;
    private String userName;
    private String operation;
    private String module;
    private String ipAddress;
    private LocalDateTime createdAt;
}