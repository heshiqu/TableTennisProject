package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Coach;
import com.example.ttp_serve.enums.CoachLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long>, JpaSpecificationExecutor<Coach> {

    /**
     * 根据校区ID查找教练
     */
    List<Coach> findByCampusId(Long campusId);

    /**
     * 根据姓名和校区查找教练
     */
    List<Coach> findByRealNameContainingAndCampusId(String name, Long campusId);

    /**
     * 根据姓名、性别、年龄和校区查找教练
     */
    @Query("SELECT c FROM Coach c WHERE " +
            "(:name IS NULL OR c.realName LIKE %:name%) AND " +
            "(:gender IS NULL OR c.gender = :gender) AND " +
            "(:age IS NULL OR c.age = :age) AND " +
            "c.campus.id = :campusId")
    List<Coach> findByCriteria(@Param("name") String name,
                               @Param("gender") String gender,
                               @Param("age") Integer age,
                               @Param("campusId") Long campusId);

    /**
     * 根据教练级别查找
     */
    List<Coach> findByLevel(CoachLevel level);

}