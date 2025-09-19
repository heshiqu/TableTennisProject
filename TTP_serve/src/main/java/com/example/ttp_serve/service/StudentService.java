package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.Student;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学员服务接口
 * 提供学员相关功能
 */
public interface StudentService {

    /**
     * 根据学生ID获取个人余额
     * @param studentId 学生ID
     * @return 学生个人余额
     */
    BigDecimal getStudentBalance(Long studentId);

    /**
     * 根据学生ID获取学生信息
     * @param studentId 学生ID
     * @return 学生实体
     */
    Student getStudentById(Long studentId);

    /**
     * 根据学生ID获取本月取消次数
     * @param studentId 学生ID
     * @return 本月取消次数
     */
    Integer getCurrentMonthCancelCount(Long studentId);

    /**
     * 根据校区ID获取所有学生信息
     * @param campusId 校区ID
     * @return 该校区下的所有学生列表
     */
    List<Student> getStudentsByCampusId(Long campusId);

    /**
     * 统计指定校区的学生数量
     * @param campusId 校区ID
     * @return 该校区下的学生数量
     */
    Long countStudentsByCampusId(Long campusId);

    /**
     * 获取所有学生信息
     * @return 所有学生列表
     */
    List<Student> getAllStudents();

    /**
     * 统计所有学生数量
     * @return 学生总数
     */
    Long countAllStudents();
}