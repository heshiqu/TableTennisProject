package com.example.ttp_serve.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.ttp_serve.enums.*;

@Entity
@Table(name = "coach_student_relation")
@Data
@NoArgsConstructor
public class CoachStudentRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_user_id", nullable = false)  // 修改为 coach_user_id
    private Coach coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)  // 修改为 student_user_id
    private Student student;

    @Enumerated(EnumType.STRING)
    private RelationStatus status = RelationStatus.PENDING;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @PrePersist
    protected void onCreate() {
        applyTime = LocalDateTime.now();
    }
}