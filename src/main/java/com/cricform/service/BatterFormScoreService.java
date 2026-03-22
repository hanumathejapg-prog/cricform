package com.cricform.service;

import com.cricform.model.BattingInnings;
import com.cricform.model.Match;
import com.cricform.repository.BattingInningsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BatterFormScoreService {

    private final BattingInningsRepository battingInningsRepository;
    private final BowlingAttackScoreService bowlingAttackScoreService;

    public double calculateBatterFormScore(Long playerId, String format, int window) {
        return calculateBatterRawScore(playerId, format, window, null);
    }

    double calculateBatterRawScore(Long playerId, String format, int window, Map<Long, Double> bowlerScoresSnapshot) {
        List<BattingInnings> inningsList = battingInningsRepository.findByPlayer_IdOrderByMatch_MatchDateDescMatch_IdDesc(playerId)
                .stream()
                .filter(innings -> innings.getMatch().getMatchType().name().equalsIgnoreCase(format))
                .filter(innings -> !Boolean.TRUE.equals(innings.getDidNotBat()))
                .limit(window)
                .toList();

        if (inningsList.size() < 5) {
            return Double.NaN;
        }

        double total = 0.0;
        List<Integer> runScores = new ArrayList<>();

        inningsList.sort(Comparator.comparing((BattingInnings i) -> i.getMatch().getMatchDate()).reversed());

        for (int i = 0; i < inningsList.size(); i++) {
            BattingInnings innings = inningsList.get(i);
            int rank = i + 1;

            double baseRuns = innings.getRunsScored();
            runScores.add(innings.getRunsScored());

            if (innings.getRunsScored() == 0 && "runout".equalsIgnoreCase(innings.getDismissalType())) {
                baseRuns = baseRuns * 0.8;
            }

            if (Boolean.TRUE.equals(innings.getRetiredHurt())) {
                double avgBalls = getAverageInningsLengthBalls(format);
                double ballsFaced = Math.max(innings.getBallsFaced(), 1);
                baseRuns = baseRuns / (ballsFaced / avgBalls);
                baseRuns += 3;
            }

            if (Boolean.TRUE.equals(innings.getNotOut())) {
                baseRuns += 5;
            }

            double attackScore = bowlerScoresSnapshot == null
                    ? bowlingAttackScoreService.getBowlingAttackScore(innings.getMatch().getId())
                    : getAttackScoreFromSnapshot(innings.getMatch(), bowlerScoresSnapshot);

            double multipliers = calculateSequenceWeight(rank)
                    * getFormatMultiplier(format)
                    * bowlingAttackScoreService.getBowlingAttackMultiplier(attackScore)
                    * getMatchContextMultiplier(innings.getMatch().getMatchContext().name());

            multipliers = applyMultiplierCap(multipliers);
            total += baseRuns * multipliers;
        }

        return total + calculateConsistencyBonus(runScores);
    }

    public double calculateSequenceWeight(int rank) {
        return Math.max(1.0 - ((rank - 1) * 0.1), 0.1);
    }

    public double getFormatMultiplier(String format) {
        if ("T20I".equalsIgnoreCase(format)) {
            return 1.3;
        }
        if ("ODI".equalsIgnoreCase(format)) {
            return 1.1;
        }
        return 1.0;
    }

    public double getMatchContextMultiplier(String matchType) {
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

    public double calculateConsistencyBonus(List<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        double mean = scores.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        if (stdDev < 10) {
            return 10;
        }
        if (stdDev < 20) {
            return 7;
        }
        if (stdDev < 30) {
            return 4;
        }
        if (stdDev < 40) {
            return 2;
        }
        return 0;
    }

    public double normalizeTo100(double rawScore, double minRaw, double maxRaw) {
        if (Double.compare(maxRaw, minRaw) == 0) {
            return 50.0;
        }
        double normalized = ((rawScore - minRaw) / (maxRaw - minRaw)) * 100.0;
        return Math.max(0.0, Math.min(100.0, normalized));
    }

    public double applyMultiplierCap(double stackedMultiplier) {
        return Math.min(stackedMultiplier, 2.0);
    }

    private double getAttackScoreFromSnapshot(Match match, Map<Long, Double> bowlerScoresSnapshot) {
        return bowlerScoresSnapshot.values().stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
    }

    private double getAverageInningsLengthBalls(String format) {
        if ("T20I".equalsIgnoreCase(format)) {
            return 30.0;
        }
        if ("ODI".equalsIgnoreCase(format)) {
            return 60.0;
        }
        return 90.0;
    }
}
