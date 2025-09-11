package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.RelationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CoachStudentRelationDTO {
    private Long id;
    private Long coachId;
    private String coachName;
    private Long studentId;
    private String studentName;
    private RelationStatus status;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
}