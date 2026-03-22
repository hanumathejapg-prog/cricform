package com.cricform.service;

import com.cricform.model.Player;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.PlayerFormScoreRepository;
import com.cricform.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormScoreService {

    private final PlayerRepository playerRepository;
    private final PlayerFormScoreRepository playerFormScoreRepository;
    private final BatterFormScoreService batterFormScoreService;
    private final BowlerFormScoreService bowlerFormScoreService;
    private final CacheService cacheService;


    @Async
    public void triggerAsyncRecalculationForPlayer(Long playerId) {
        recalculateAllScores();
    }

    @Transactional
    public void recalculateAllScores() {
        Map<Long, Map<String, Double>> iteration0Bowler = calculateIteration0BowlerScores();
        Map<Long, Map<String, Double>> iteration1Batter = calculateIteration1BatterScores(iteration0Bowler);
        Map<Long, Map<String, Double>> iteration2Bowler = calculateIteration2BowlerScores(iteration1Batter);

        persistScores(iteration1Batter, iteration2Bowler);
        cacheService.invalidateAllFormScores();
        cacheService.repopulateAllFormScores();
        cacheService.updateLastRefreshTimestamp();
        log.info("Form score recalculation completed. Stored scores in player_form_scores table and refreshed Redis cache.");
    }

    private Map<Long, Map<String, Double>> calculateIteration0BowlerScores() {
        Map<Long, Map<String, Double>> scores = new HashMap<>();

        for (Player player : playerRepository.findByIsActiveTrue()) {
            if (!isBowler(player.getRole())) {
                continue;
            }
            Map<String, Double> perFormat = new HashMap<>();
            for (String format : List.of("T20I", "ODI", "TEST")) {
                double raw = bowlerFormScoreService.calculateBowlerRawScore(player.getId(), format, 10, null, false);
                perFormat.put(format, raw);
            }
            scores.put(player.getId(), perFormat);
        }
        return scores;
    }

    private Map<Long, Map<String, Double>> calculateIteration1BatterScores(Map<Long, Map<String, Double>> iteration0Bowler) {
        Map<Long, Double> flatBowler = flattenByAveraging(iteration0Bowler);
        Map<Long, Map<String, Double>> scores = new HashMap<>();

        for (Player player : playerRepository.findByIsActiveTrue()) {
            if (!isBatter(player.getRole())) {
                continue;
            }
            Map<String, Double> perFormat = new HashMap<>();
            for (String format : List.of("T20I", "ODI", "TEST")) {
                double raw = batterFormScoreService.calculateBatterRawScore(player.getId(), format, 10, flatBowler);
                perFormat.put(format, raw);
            }
            scores.put(player.getId(), perFormat);
        }
        return scores;
    }

    private Map<Long, Map<String, Double>> calculateIteration2BowlerScores(Map<Long, Map<String, Double>> iteration1Batter) {
        Map<Long, Double> flatBatters = flattenByAveraging(iteration1Batter);
        Map<Long, Map<String, Double>> scores = new HashMap<>();

        for (Player player : playerRepository.findByIsActiveTrue()) {
            if (!isBowler(player.getRole())) {
                continue;
            }
            Map<String, Double> perFormat = new HashMap<>();
            for (String format : List.of("T20I", "ODI", "TEST")) {
                double raw = bowlerFormScoreService.calculateBowlerRawScore(player.getId(), format, 10, flatBatters, true);
                perFormat.put(format, raw);
            }
            scores.put(player.getId(), perFormat);
        }
        return scores;
    }

    private void persistScores(Map<Long, Map<String, Double>> batterRaw, Map<Long, Map<String, Double>> bowlerRaw) {
        for (String format : List.of("T20I", "ODI", "TEST")) {
            List<Double> allRawForFormat = new ArrayList<>();
            batterRaw.values().forEach(map -> addIfFinite(allRawForFormat, map.get(format)));
            bowlerRaw.values().forEach(map -> addIfFinite(allRawForFormat, map.get(format)));

            double min = allRawForFormat.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = allRawForFormat.stream().mapToDouble(Double::doubleValue).max().orElse(100.0);

            persistRoleScores(batterRaw, format, min, max);
            persistRoleScores(bowlerRaw, format, min, max);
        }
    }

    private void persistRoleScores(Map<Long, Map<String, Double>> rawScores, String format, double min, double max) {
        PlayerFormScore.Format enumFormat = PlayerFormScore.Format.valueOf(format);

        for (Map.Entry<Long, Map<String, Double>> entry : rawScores.entrySet()) {
            Long playerId = entry.getKey();
            Double raw = entry.getValue().get(format);
            boolean insufficient = raw == null || raw.isNaN() || raw.isInfinite();

            PlayerFormScore score = playerFormScoreRepository.findByPlayer_IdAndFormat(playerId, enumFormat)
                    .orElseGet(PlayerFormScore::new);

            score.setPlayer(playerRepository.findById(playerId).orElseThrow());
            score.setFormat(enumFormat);
            score.setRawScore(toDecimal(insufficient ? 0.0 : raw));
            score.setConsistencyBonus(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            score.setMatchesConsidered(10);
            score.setInsufficientData(insufficient);
            score.setCalculatedAt(LocalDateTime.now());
            score.setWindowSize(10);

            if (insufficient) {
                score.setFormScore(null);
            } else {
                double normalized = ((raw - min) / ((max - min) == 0 ? 1.0 : (max - min))) * 100.0;
                normalized = Math.max(0.0, Math.min(100.0, normalized));
                score.setFormScore(toDecimal(normalized));
            }

            playerFormScoreRepository.save(score);
        }
    }

    private void addIfFinite(List<Double> target, Double value) {
        if (value != null && !value.isNaN() && !value.isInfinite()) {
            target.add(value);
        }
    }

    private boolean isBowler(Player.PlayerRole role) {
        return role == Player.PlayerRole.BOWLER || role == Player.PlayerRole.BOWLING_ALLROUNDER || role == Player.PlayerRole.BATTING_ALLROUNDER;
    }

    private boolean isBatter(Player.PlayerRole role) {
        return role == Player.PlayerRole.BATSMAN || role == Player.PlayerRole.BATTING_ALLROUNDER || role == Player.PlayerRole.BOWLING_ALLROUNDER;
    }

    private Map<Long, Double> flattenByAveraging(Map<Long, Map<String, Double>> input) {
        Map<Long, Double> flat = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : input.entrySet()) {
            double avg = entry.getValue().values().stream()
                    .filter(v -> v != null && !v.isNaN() && !v.isInfinite())
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(50.0);
            flat.put(entry.getKey(), avg);
        }
        return flat;
    }

    private BigDecimal toDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
