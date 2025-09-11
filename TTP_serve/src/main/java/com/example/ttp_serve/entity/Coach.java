package com.example.ttp_serve.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.ttp_serve.enums.*;

@Entity
@Table(name = "coach")
@PrimaryKeyJoinColumn(name = "user_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Coach extends User {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachLevel level;

    private String awards;

    @Column(name = "hourly_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal hourlyRate;

    @Column(name = "max_students")
    private Integer maxStudents = 20;

    @Column(name = "current_students")
    private Integer currentStudents = 0;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoachStudentRelation> studentRelations = new ArrayList<>();

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluationsGiven = new ArrayList<>();

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluationsReceived = new ArrayList<>();
}