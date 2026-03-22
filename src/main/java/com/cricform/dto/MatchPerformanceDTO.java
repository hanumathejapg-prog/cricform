package com.cricform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchPerformanceDTO {
    private Long matchId;
    private String opponent;
    private String format;
    private String matchType;
    private LocalDate date;
    private Integer runs;
    private Integer balls;
    private Boolean notOut;
    private Double weightedScore;
    private Double bowlingAttackScore;
}
