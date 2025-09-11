package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.SystemLicense;
import com.example.ttp_serve.enums.LicenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemLicenseRepository extends JpaRepository<SystemLicense, Long> {

    // 根据许可证密钥查找
    Optional<SystemLicense> findByLicenseKey(String licenseKey);

    // 根据校区ID查找许可证
    List<SystemLicense> findByCampusId(Long campusId);

    // 根据状态查找许可证
    List<SystemLicense> findByStatus(LicenseStatus status);

    // 根据过期日期查找许可证
    List<SystemLicense> findByExpirationDate(LocalDate expirationDate);

    // 查找即将过期的许可证（30天内）
    @Query("SELECT sl FROM SystemLicense sl WHERE sl.expirationDate BETWEEN :today AND :futureDate")
    List<SystemLicense> findExpiringLicenses(@Param("today") LocalDate today,
                                             @Param("futureDate") LocalDate futureDate);

    // 查找已过期的许可证
    @Query("SELECT sl FROM SystemLicense sl WHERE sl.expirationDate < :today")
    List<SystemLicense> findExpiredLicenses(@Param("today") LocalDate today);

    // 检查许可证密钥是否存在
    boolean existsByLicenseKey(String licenseKey);

    // 检查校区是否有有效的许可证
    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM SystemLicense sl WHERE " +
            "sl.campus.id = :campusId AND sl.status = 'ACTIVE' AND sl.expirationDate >= :today")
    boolean existsValidLicenseForCampus(@Param("campusId") Long campusId,
                                        @Param("today") LocalDate today);
}