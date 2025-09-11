package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.SystemLicense;
import com.example.ttp_serve.enums.LicenseStatus;

import java.time.LocalDate;
import java.util.List;

public interface LicenseService {

    SystemLicense createLicense(SystemLicense license);
    SystemLicense updateLicense(Long id, SystemLicense license);
    void deleteLicense(Long id);
    SystemLicense getLicense(Long id);
    SystemLicense getLicenseByKey(String licenseKey);
    List<SystemLicense> getLicensesByCampus(Long campusId);
    List<SystemLicense> getLicensesByStatus(LicenseStatus status);
    List<SystemLicense> getExpiringLicenses(int days);
    List<SystemLicense> getExpiredLicenses();
    boolean validateLicense(String licenseKey, Long campusId);
    boolean isCampusLicensed(Long campusId);
    SystemLicense renewLicense(Long id, LocalDate newExpirationDate);
}