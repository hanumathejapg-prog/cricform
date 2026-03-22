package com.cricform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_form_scores")
public class PlayerFormScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Format format;

    @Column(name = "form_score", precision = 5, scale = 2)
    private BigDecimal formScore;

    @Column(name = "raw_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal rawScore;

    @Column(name = "consistency_bonus", nullable = false, precision = 5, scale = 2)
    private BigDecimal consistencyBonus;

    @Column(name = "matches_considered", nullable = false)
    private Integer matchesConsidered;

    @Column(name = "insufficient_data", nullable = false)
    private Boolean insufficientData;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "window_size", nullable = false)
    private Integer windowSize;

    public enum Format {
        T20I,
        ODI,
        TEST
    }
}
