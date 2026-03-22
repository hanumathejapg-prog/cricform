package com.cricform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "batting_innings")
public class BattingInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "runs_scored", nullable = false)
    private Integer runsScored;

    @Column(name = "balls_faced", nullable = false)
    private Integer ballsFaced;

    @Column(name = "strike_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal strikeRate;

    @Column(name = "not_out", nullable = false)
    private Boolean notOut;

    @Column(name = "retired_hurt", nullable = false)
    private Boolean retiredHurt;

    @Column(name = "did_not_bat", nullable = false)
    private Boolean didNotBat;

    @Column(name = "dismissal_type", nullable = false, length = 30)
    private String dismissalType;

    @Column(name = "match_sequence_rank", nullable = false)
    private Integer matchSequenceRank;

    @Column(name = "weighted_score", nullable = false, precision = 8, scale = 2)
    private BigDecimal weightedScore;

    @Column(name = "bowling_attack_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal bowlingAttackScore;
}
