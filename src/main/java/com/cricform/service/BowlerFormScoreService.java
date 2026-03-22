package com.cricform.service;

import com.cricform.model.BowlingInnings;
import com.cricform.repository.BowlingInningsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BowlerFormScoreService {

    private final BowlingInningsRepository bowlingInningsRepository;
    private final BattingLineupScoreService battingLineupScoreService;

    public double calculateBowlerFormScore(Long playerId, String format, int window) {
        return calculateBowlerRawScore(playerId, format, window, null, true);
    }

    double calculateBowlerRawScore(Long playerId, String format, int window, Map<Long, Double> batterSnapshot, boolean includeLineupMultiplier) {
        List<BowlingInnings> inningsList = bowlingInningsRepository.findByPlayer_IdOrderByMatch_MatchDateDescMatch_IdDesc(playerId)
                .stream()
                .filter(innings -> innings.getMatch().getMatchType().name().equalsIgnoreCase(format))
                .filter(innings -> !Boolean.TRUE.equals(innings.getDidNotBowl()))
                .filter(innings -> innings.getOversBowled().doubleValue() >= 1.0)
                .limit(window)
                .toList();

        if (inningsList.size() < 5) {
            return Double.NaN;
        }

        double total = 0.0;
        List<Integer> perMatchRounded = new ArrayList<>();

        inningsList.sort(Comparator.comparing((BowlingInnings i) -> i.getMatch().getMatchDate()).reversed());

        for (int i = 0; i < inningsList.size(); i++) {
            BowlingInnings innings = inningsList.get(i);
            int rank = i + 1;

            double matchScore = calculateMatchBowlingScore(innings);
            perMatchRounded.add((int) Math.round(matchScore));

            double lineupScore = includeLineupMultiplier
                    ? (batterSnapshot == null
                    ? battingLineupScoreService.getBattingLineupScore(innings.getMatch().getId())
                    : batterSnapshot.values().stream().mapToDouble(Double::doubleValue).average().orElse(50.0))
                    : 50.0;

            double multipliers = calculateSequenceWeight(rank)
                    * getFormatMultiplier(format)
                    * getLineupMultiplier(lineupScore)
                    * getMatchContextMultiplier(innings.getMatch().getMatchContext().name());

            multipliers = Math.min(multipliers, 2.0);
            total += matchScore * multipliers;
        }

        return total + calculateConsistencyBonus(perMatchRounded);
    }

    public double calculateMatchBowlingScore(BowlingInnings innings) {
        BigDecimal overs = innings.getOversBowled();
        double completedOvers = Boolean.TRUE.equals(innings.getInjuryWithdrawal())
                ? Math.floor(overs.doubleValue())
                : overs.doubleValue();

        double fullQuota = switch (innings.getMatch().getMatchType()) {
            case T20I -> 4.0;
            case ODI -> 10.0;
            case TEST -> 20.0;
        };

        String phase = inferMatchPhase(innings, completedOvers);
        double wicketsComponent = innings.getWickets() * getWicketWeight("caught");
        double economyComponent = getEconomyScore(innings.getEconomy().doubleValue(), phase, innings.getMatch().getMatchType().name());
        double oversComponent = Boolean.TRUE.equals(innings.getInjuryWithdrawal())
                ? 0.0
                : getOversComponent(completedOvers, fullQuota);

        return wicketsComponent + economyComponent + oversComponent + calculateMaidenBonus(innings.getMaidens());
    }

    public double getWicketWeight(String dismissalType) {
        if (dismissalType == null) {
            return 12;
        }
        return switch (dismissalType.toLowerCase()) {
            case "bowled" -> 15;
            case "lbw" -> 14;
            case "caught" -> 12;
            case "stumped" -> 10;
            case "runout", "run out" -> 0;
            default -> 12;
        };
    }

    public double getEconomyScore(double economy, String matchPhase, String format) {
        if ("TEST".equalsIgnoreCase(format)) {
            if (economy < 3.0) return 30;
            if (economy < 4.0) return 20;
            if (economy < 5.0) return 10;
            return 2;
        }

        if ("POWERPLAY".equalsIgnoreCase(matchPhase)) {
            if (economy < 5.0) return 30;
            if (economy < 7.0) return 20;
            if (economy < 9.0) return 10;
            return 2;
        }

        if ("DEATH".equalsIgnoreCase(matchPhase)) {
            if (economy < 8.0) return 30;
            if (economy < 10.0) return 18;
            if (economy < 12.0) return 10;
            return 3;
        }

        if (economy < 6.0) return 25;
        if (economy < 8.0) return 15;
        if (economy < 10.0) return 8;
        return 2;
    }

    public double getOversComponent(double oversBowled, double fullQuota) {
        if (fullQuota <= 0) {
            return 0;
        }

        if (oversBowled >= fullQuota) {
            return 10;
        }
        if (oversBowled >= (fullQuota * 0.75)) {
            return 5;
        }
        if (oversBowled < (fullQuota * 0.5)) {
            return 0;
        }
        return 2;
    }

    public double calculateMaidenBonus(int maidens) {
        return maidens * 5.0;
    }

    private String inferMatchPhase(BowlingInnings innings, double oversBowled) {
        if (innings.getMatch().getMatchType().name().equals("TEST")) {
            return "TEST";
        }
        if (oversBowled <= 6.0) {
            return "POWERPLAY";
        }
        if (oversBowled >= 16.0) {
            return "DEATH";
        }
        return "MIDDLE";
    }

    private double calculateSequenceWeight(int rank) {
        return Math.max(1.0 - ((rank - 1) * 0.1), 0.1);
    }

    private double getFormatMultiplier(String format) {
        if ("T20I".equalsIgnoreCase(format)) {
            return 1.3;
        }
        if ("ODI".equalsIgnoreCase(format)) {
            return 1.1;
        }
        return 1.0;
    }

    private double getLineupMultiplier(double score) {
        if (score < 30) return 0.8;
        if (score < 45) return 0.9;
        if (score < 60) return 1.0;
        if (score < 75) return 1.1;
        if (score <= 85) return 1.2;
        return 1.3;
    }

    private double getMatchContextMultiplier(String matchType) {
        if (matchType == null) {
            return 1.0;
        }
        return switch (matchType.toUpperCase()) {
            case "FINAL" -> 1.3;
            case "KNOCKOUT" -> 1.2;
            case "DEAD_RUBBER" -> 0.8;
            default -> 1.0;
        };
    }

    double normalizeTo100(double rawScore, double minRaw, double maxRaw) {
        if (Double.compare(maxRaw, minRaw) == 0) {
            return 50.0;
        }
        double normalized = ((rawScore - minRaw) / (maxRaw - minRaw)) * 100.0;
        return BigDecimal.valueOf(Math.max(0.0, Math.min(100.0, normalized)))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    double calculateConsistencyBonus(List<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        double mean = scores.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = scores.stream().mapToDouble(score -> Math.pow(score - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        if (stdDev < 10) return 10;
        if (stdDev < 20) return 7;
        if (stdDev < 30) return 4;
        if (stdDev < 40) return 2;
        return 0;
    }
}
