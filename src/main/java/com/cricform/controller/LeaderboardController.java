package com.cricform.controller;

import com.cricform.dto.LeaderboardEntryDTO;
import com.cricform.model.Player;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.PlayerFormScoreRepository;
import com.cricform.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final PlayerFormScoreRepository playerFormScoreRepository;
    private final CacheService cacheService;

    @GetMapping("/batters")
    public ResponseEntity<List<LeaderboardEntryDTO>> batters(@RequestParam String format) {
        return ResponseEntity.ok(getLeaderboard("batters", format, List.of(Player.PlayerRole.BATSMAN, Player.PlayerRole.BATTING_ALLROUNDER)));
    }

    @GetMapping("/bowlers")
    public ResponseEntity<List<LeaderboardEntryDTO>> bowlers(@RequestParam String format) {
        return ResponseEntity.ok(getLeaderboard("bowlers", format, List.of(Player.PlayerRole.BOWLER, Player.PlayerRole.BOWLING_ALLROUNDER)));
    }

    @GetMapping("/allrounders")
    public ResponseEntity<List<LeaderboardEntryDTO>> allrounders(@RequestParam String format) {
        return ResponseEntity.ok(getLeaderboard("allrounders", format, List.of(Player.PlayerRole.BATTING_ALLROUNDER, Player.PlayerRole.BOWLING_ALLROUNDER)));
    }

    private List<LeaderboardEntryDTO> getLeaderboard(String type, String format, List<Player.PlayerRole> roles) {
        List<LeaderboardEntryDTO> cached = cacheService.getLeaderboard(type, format.toUpperCase());
        if (cached != null) {
            return cached;
        }

        List<PlayerFormScore> scores = playerFormScoreRepository.findTopByFormatAndRoles(
                PlayerFormScore.Format.valueOf(format.toUpperCase()),
                roles,
                PageRequest.of(0, 10)
        );

        List<LeaderboardEntryDTO> data = java.util.stream.IntStream.range(0, scores.size())
                .mapToObj(idx -> {
                    PlayerFormScore p = scores.get(idx);
                    return LeaderboardEntryDTO.builder()
                            .rank(idx + 1)
                            .playerId(p.getPlayer().getId())
                            .playerName(p.getPlayer().getName())
                            .team(p.getPlayer().getTeam())
                            .formScore(p.getFormScore() == null ? null : p.getFormScore().doubleValue())
                            .format(format.toUpperCase())
                            .trend("STABLE")
                            .build();
                }).toList();

        cacheService.putLeaderboard(type, format.toUpperCase(), data);
        return data;
    }
}
