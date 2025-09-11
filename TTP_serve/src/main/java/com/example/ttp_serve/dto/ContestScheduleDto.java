package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.ContestScheduleStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContestScheduleDto {

    private Long id;
    private Long contestId;
    private LocalDate contestDate;
    private Integer roundNumber;
    private Long player1Id;
    private String player1Name;
    private Long player2Id;
    private String player2Name;
    private Long courtId;
    private String courtNumber;
    private LocalDateTime startTime;
    private Long winnerId;
    private String winnerName;
    private ContestScheduleStatus status;
    private LocalDateTime createdAt;
}