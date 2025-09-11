package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.LicenseStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SystemLicenseDto {

    private Long id;
    private String licenseKey;
    private Long campusId;
    private String campusName;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private LicenseStatus status;
    private LocalDateTime createdAt;
}