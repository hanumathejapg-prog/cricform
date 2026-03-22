package com.cricform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerFormScoreDTO {
    private Long playerId;
    private String playerName;
    private String team;
    private String role;
    private Double t20iFormScore;
    private Double odiFormScore;
    private Double testFormScore;
    private LocalDateTime lastUpdated;
    private boolean isStale;
    private boolean insufficientData;
}
