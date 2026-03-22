package com.cricform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    @Id
    private Long id;

    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    @Column(name = "series_name", nullable = false, length = 150)
    private String seriesName;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_context", nullable = false)
    private MatchContext matchContext;

    @Column(nullable = false, length = 50)
    private String team1;

    @Column(nullable = false, length = 50)
    private String team2;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(nullable = false, length = 100)
    private String venue;

    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt;

    public enum MatchType {
        T20I,
        ODI,
        TEST
    }

    public enum MatchContext {
        FINAL,
        KNOCKOUT,
        GROUP,
        DEAD_RUBBER
    }
}
