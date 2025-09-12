package com.example.ttp_serve.dto;

import lombok.Data;

@Data
public class CampusStatsDTO {
    private Long campusId;
    private String campusName;
    private Long userCount;
    private Long studentCount;
    private Long coachCount;
    private Long adminCount;
    private Long childCampusCount;
}