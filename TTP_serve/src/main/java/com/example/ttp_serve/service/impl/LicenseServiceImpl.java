package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.SystemLicense;
import com.example.ttp_serve.enums.LicenseStatus;
import com.example.ttp_serve.repository.SystemLicenseRepository;
import com.example.ttp_serve.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LicenseServiceImpl implements LicenseService {

    private final SystemLicenseRepository systemLicenseRepository;

    @Autowired
    public LicenseServiceImpl(SystemLicenseRepository systemLicenseRepository) {
        this.systemLicenseRepository = systemLicenseRepository;
    }

    @Override
    public SystemLicense createLicense(SystemLicense license) {
        // 检查许可证密钥是否已存在
        if (systemLicenseRepository.existsByLicenseKey(license.getLicenseKey())) {
            throw new IllegalArgumentException("许可证密钥已存在: " + license.getLicenseKey());
        }

        // 设置默认状态为ACTIVE（如果未设置）
        if (license.getStatus() == null) {
            license.setStatus(LicenseStatus.ACTIVE);
        }

        return systemLicenseRepository.save(license);
    }

    @Override
    public SystemLicense updateLicense(Long id, SystemLicense license) {
        Optional<SystemLicense> existingLicense = systemLicenseRepository.findById(id);

        if (existingLicense.isPresent()) {
            SystemLicense updatedLicense = existingLicense.get();

            // 更新可修改的字段
            if (license.getLicenseKey() != null) {
                // 检查新的许可证密钥是否与其他记录冲突
                if (!updatedLicense.getLicenseKey().equals(license.getLicenseKey()) &&
                        systemLicenseRepository.existsByLicenseKey(license.getLicenseKey())) {
                    throw new IllegalArgumentException("许可证密钥已存在: " + license.getLicenseKey());
                }
                updatedLicense.setLicenseKey(license.getLicenseKey());
            }

            if (license.getCampus() != null) {
                updatedLicense.setCampus(license.getCampus());
            }

            if (license.getActivationDate() != null) {
                updatedLicense.setActivationDate(license.getActivationDate());
            }

            if (license.getExpirationDate() != null) {
                updatedLicense.setExpirationDate(license.getExpirationDate());
            }

            if (license.getStatus() != null) {
                updatedLicense.setStatus(license.getStatus());
            }

            return systemLicenseRepository.save(updatedLicense);
        } else {
            throw new IllegalArgumentException("未找到ID为 " + id + " 的许可证");
        }
    }

    @Override
    public void deleteLicense(Long id) {
        if (systemLicenseRepository.existsById(id)) {
            systemLicenseRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("未找到ID为 " + id + " 的许可证");
        }
    }

    @Override
    public SystemLicense getLicense(Long id) {
        Optional<SystemLicense> license = systemLicenseRepository.findById(id);
        return license.orElse(null);
    }

    @Override
    public SystemLicense getLicenseByKey(String licenseKey) {
        Optional<SystemLicense> license = systemLicenseRepository.findByLicenseKey(licenseKey);
        return license.orElse(null);
    }

    @Override
    public List<SystemLicense> getLicensesByCampus(Long campusId) {
        return systemLicenseRepository.findByCampusId(campusId);
    }

    @Override
    public List<SystemLicense> getLicensesByStatus(LicenseStatus status) {
        return systemLicenseRepository.findByStatus(status);
    }

    @Override
    public List<SystemLicense> getExpiringLicenses(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return systemLicenseRepository.findExpiringLicenses(today, futureDate);
    }

    @Override
    public List<SystemLicense> getExpiredLicenses() {
        return systemLicenseRepository.findExpiredLicenses(LocalDate.now());
    }

    @Override
    public boolean validateLicense(String licenseKey, Long campusId) {
        Optional<SystemLicense> licenseOpt = systemLicenseRepository.findByLicenseKey(licenseKey);

        if (licenseOpt.isPresent()) {
            SystemLicense license = licenseOpt.get();

            // 检查许可证是否属于指定校区
            if (!license.getCampus().getId().equals(campusId)) {
                return false;
            }

            // 检查许可证状态
            if (license.getStatus() != LicenseStatus.ACTIVE) {
                return false;
            }

            // 检查许可证是否在有效期内
            LocalDate today = LocalDate.now();
            return !today.isBefore(license.getActivationDate()) &&
                    !today.isAfter(license.getExpirationDate());
        }

        return false;
    }

    @Override
    public boolean isCampusLicensed(Long campusId) {
        return systemLicenseRepository.existsValidLicenseForCampus(campusId, LocalDate.now());
    }

    @Override
    public SystemLicense renewLicense(Long id, LocalDate newExpirationDate) {
        Optional<SystemLicense> licenseOpt = systemLicenseRepository.findById(id);

        if (licenseOpt.isPresent()) {
            SystemLicense license = licenseOpt.get();

            // 更新过期日期
            license.setExpirationDate(newExpirationDate);

            // 如果许可证已过期，重新激活
            if (license.getStatus() == LicenseStatus.EXPIRED) {
                license.setStatus(LicenseStatus.ACTIVE);
            }

            return systemLicenseRepository.save(license);
        } else {
            throw new IllegalArgumentException("未找到ID为 " + id + " 的许可证");
        }
    }
}