package com.cricform.controller;

import com.cricform.dto.MatchPerformanceDTO;
import com.cricform.dto.PlayerFormScoreDTO;
import com.cricform.dto.PlayerProfileDTO;
import com.cricform.exception.PlayerNotFoundException;
import com.cricform.model.BattingInnings;
import com.cricform.model.Player;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.BattingInningsRepository;
import com.cricform.repository.PlayerFormScoreRepository;
import com.cricform.repository.PlayerRepository;
import com.cricform.service.CacheService;
import com.cricform.service.FormScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final PlayerFormScoreRepository playerFormScoreRepository;
    private final BattingInningsRepository battingInningsRepository;
    private final CacheService cacheService;
    private final FormScoreService formScoreService;

    @GetMapping("/{id}/form")
    public ResponseEntity<?> getPlayerFormScore(
            @PathVariable Long id,
            @RequestParam String format,
            @RequestParam(defaultValue = "10") int window
    ) {
        Player player = playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
        PlayerFormScore cached = cacheService.getPlayerFormScore(id, format.toUpperCase());
        if (cached != null) {
            return ResponseEntity.ok(toPlayerFormScoreDto(player, cached));
        }

        PlayerFormScore score = playerFormScoreRepository
                .findByPlayer_IdAndFormat(id, PlayerFormScore.Format.valueOf(format.toUpperCase()))
                .orElse(null);

        if (score != null) {
            cacheService.putPlayerFormScore(score);
            return ResponseEntity.ok(toPlayerFormScoreDto(player, score));
        }

        formScoreService.triggerAsyncRecalculationForPlayer(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Score being calculated, retry in 30 seconds");
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PlayerProfileDTO> getPlayerProfile(@PathVariable Long id) {
        PlayerProfileDTO cached = cacheService.getPlayerProfile(id);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        Player player = playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));

        PlayerProfileDTO dto = PlayerProfileDTO.builder()
                .playerId(player.getId())
                .playerName(player.getName())
                .team(player.getTeam())
                .role(player.getRole().name())
                .t20iFormScore(getForm(id, "T20I"))
                .odiFormScore(getForm(id, "ODI"))
                .testFormScore(getForm(id, "TEST"))
                .lastUpdated(LocalDateTime.now())
                .isStale(cacheService.isDataStale())
                .insufficientData(isInsufficient(id))
                .build();

        cacheService.putPlayerProfile(dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<MatchPerformanceDTO>> getPlayerHistory(
            @PathVariable Long id,
            @RequestParam String format,
            @RequestParam(defaultValue = "10") int window
    ) {
        playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));

        List<MatchPerformanceDTO> history = battingInningsRepository
                .findByPlayer_IdOrderByMatch_MatchDateDescMatch_IdDesc(id)
                .stream()
                .filter(b -> b.getMatch().getMatchType().name().equalsIgnoreCase(format))
                .limit(window)
                .map(this::toMatchPerformance)
                .toList();

        return ResponseEntity.ok(history);
    }

    private PlayerFormScoreDTO toPlayerFormScoreDto(Player player, PlayerFormScore selectedScore) {
        return PlayerFormScoreDTO.builder()
                .playerId(player.getId())
                .playerName(player.getName())
                .team(player.getTeam())
                .role(player.getRole().name())
                .t20iFormScore(getForm(player.getId(), "T20I"))
                .odiFormScore(getForm(player.getId(), "ODI"))
                .testFormScore(getForm(player.getId(), "TEST"))
                .lastUpdated(selectedScore.getCalculatedAt())
                .isStale(cacheService.isDataStale())
                .insufficientData(Boolean.TRUE.equals(selectedScore.getInsufficientData()))
                .build();
    }

    private MatchPerformanceDTO toMatchPerformance(BattingInnings innings) {
        String opponent = innings.getMatch().getTeam1().equalsIgnoreCase(innings.getPlayer().getTeam())
                ? innings.getMatch().getTeam2()
                : innings.getMatch().getTeam1();

        return MatchPerformanceDTO.builder()
                .matchId(innings.getMatch().getId())
                .opponent(opponent)
                .format(innings.getMatch().getMatchType().name())
                .matchType(innings.getMatch().getMatchContext().name())
                .date(innings.getMatch().getMatchDate())
                .runs(innings.getRunsScored())
                .balls(innings.getBallsFaced())
                .notOut(innings.getNotOut())
                .weightedScore(innings.getWeightedScore().doubleValue())
                .bowlingAttackScore(innings.getBowlingAttackScore().doubleValue())
                .build();
    }

    private Double getForm(Long playerId, String format) {
        return playerFormScoreRepository.findByPlayer_IdAndFormat(playerId, PlayerFormScore.Format.valueOf(format))
                .map(PlayerFormScore::getFormScore)
                .map(v -> v == null ? null : v.doubleValue())
                .orElse(null);
    }

    private boolean isInsufficient(Long playerId) {
        return List.of("T20I", "ODI", "TEST").stream()
                .map(f -> playerFormScoreRepository.findByPlayer_IdAndFormat(playerId, PlayerFormScore.Format.valueOf(f)).orElse(null))
                .filter(java.util.Objects::nonNull)
                .anyMatch(v -> Boolean.TRUE.equals(v.getInsufficientData()));
    }
}
