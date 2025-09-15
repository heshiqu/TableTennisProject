package com.example.ttp_serve.service;

import com.example.ttp_serve.entity.Student;

import java.math.BigDecimal;

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
}