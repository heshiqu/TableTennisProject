package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Campus;
import com.example.ttp_serve.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

    // 根据名称查找校区
    Optional<Campus> findByName(String name);

    // 查找所有父校区（顶级校区）
    List<Campus> findByParentIsNull();

    // 根据父校区ID查找子校区
    List<Campus> findByParentId(Long parentId);

    // 根据联系人查找校区
    List<Campus> findByContactPerson(String contactPerson);

    // 根据联系人电话查找校区
    List<Campus> findByContactPhone(String contactPhone);

    // 根据地址模糊查询校区
    @Query("SELECT c FROM Campus c WHERE c.address LIKE %:address%")
    List<Campus> findByAddressContaining(@Param("address") String address);

    // 统计校区数量
    @Query("SELECT COUNT(c) FROM Campus c")
    Long countAll();

    // 统计子校区数量
    Long countByParentId(Long parentId);

    // 根据名称模糊查询校区
    @Query("SELECT c FROM Campus c WHERE c.name LIKE %:name%")
    List<Campus> findByNameContaining(@Param("name") String name);
}