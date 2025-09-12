package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.Student;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.StudentRepository;
import com.example.ttp_serve.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 学员服务实现类
 * 提供学员相关功能的实现
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public BigDecimal getStudentBalance(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生ID '" + studentId + "' 不存在"));
        return student.getBalance();
    }

    @Override
    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生ID '" + studentId + "' 不存在"));
    }
}