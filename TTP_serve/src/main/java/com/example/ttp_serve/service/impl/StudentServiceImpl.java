package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.Student;
import com.example.ttp_serve.exception.ResourceNotFoundException;
import com.example.ttp_serve.repository.StudentRepository;
import com.example.ttp_serve.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @Override
    public Integer getCurrentMonthCancelCount(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生ID '" + studentId + "' 不存在"));
        
        // 获取当前年月
        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfCurrentMonth = currentDate.withDayOfMonth(1);
        
        // 检查最后取消月份是否为本月
        if (student.getLastCancelMonth() != null && 
            student.getLastCancelMonth().isEqual(firstDayOfCurrentMonth)) {
            return student.getCancelCount();
        }
        
        // 如果不是本月，将数据库中的cancelCount置为0
        if (student.getCancelCount() != null && student.getCancelCount() > 0) {
            student.setCancelCount(0);
            studentRepository.save(student);
        }
        
        return 0;
    }

    @Override
    public List<Student> getStudentsByCampusId(Long campusId) {
        return studentRepository.findByCampusId(campusId);
    }

    @Override
    public Long countStudentsByCampusId(Long campusId) {
        return studentRepository.countByCampusId(campusId);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Long countAllStudents() {
        return studentRepository.count();
    }
}