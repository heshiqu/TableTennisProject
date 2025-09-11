package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // 根据校区ID查找学员
    List<Student> findByCampusId(Long campusId);

    // 查找余额大于指定值的学员
    List<Student> findByBalanceGreaterThan(BigDecimal amount);

    // 查找余额小于指定值的学员
    List<Student> findByBalanceLessThan(BigDecimal amount);

    // 更新学员余额
    @Query("UPDATE Student s SET s.balance = s.balance + :amount WHERE s.id = :id")
    void updateBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    // 统计校区内的学员数量
    Long countByCampusId(Long campusId);
}