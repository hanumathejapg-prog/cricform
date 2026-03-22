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
@Table(name = "bowling_innings")
public class BowlingInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "overs_bowled", nullable = false, precision = 4, scale = 1)
    private BigDecimal oversBowled;

    @Column(nullable = false)
    private Integer maidens;

    @Column(name = "runs_conceded", nullable = false)
    private Integer runsConceded;

    @Column(nullable = false)
    private Integer wickets;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal economy;

    @Column(name = "did_not_bowl", nullable = false)
    private Boolean didNotBowl;

    @Column(name = "injury_withdrawal", nullable = false)
    private Boolean injuryWithdrawal;

    @Column(name = "match_sequence_rank", nullable = false)
    private Integer matchSequenceRank;

    @Column(name = "weighted_score", nullable = false, precision = 8, scale = 2)
    private BigDecimal weightedScore;

    @Column(name = "batting_lineup_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal battingLineupScore;
}
