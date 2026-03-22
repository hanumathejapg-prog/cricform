package com.cricform.service;

import com.cricform.dto.external.ExternalPlayerProfileDTO;
import com.cricform.dto.ScorecardDTO;
import com.cricform.dto.SeriesDTO;
import com.cricform.model.BattingInnings;
import com.cricform.model.BowlingInnings;
import com.cricform.model.Match;
import com.cricform.model.Player;
import com.cricform.repository.BattingInningsRepository;
import com.cricform.repository.BowlingInningsRepository;
import com.cricform.repository.MatchRepository;
import com.cricform.repository.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionService {

    private final CricketApiService cricketApiService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final BattingInningsRepository battingInningsRepository;
    private final BowlingInningsRepository bowlingInningsRepository;

    @Transactional
    public void runFullIngestionCycle() {
        List<SeriesDTO> recentSeries = cricketApiService.fetchRecentInternationalSeries();
        List<SeriesMatchRef> allMatches = extractMatchReferences(recentSeries);

        for (SeriesMatchRef ref : allMatches) {
            try {
                Match existingMatch = matchRepository.findById(ref.matchId()).orElse(null);
                if (existingMatch != null && existingMatch.getIngestedAt() != null
                        && existingMatch.getIngestedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                    continue;
                }

                ScorecardDTO scorecardDTO = cricketApiService.fetchMatchScorecard(ref.matchId());
                if (scorecardDTO.getPayload() == null) {
                    continue;
                }

                Match match = upsertMatch(ref, scorecardDTO.getPayload(), existingMatch);
                Map<Long, Player> playersInMatch = new HashMap<>();

                parseAndUpsertBattingInnings(scorecardDTO.getPayload(), match, playersInMatch);
                parseAndUpsertBowlingInnings(scorecardDTO.getPayload(), match, playersInMatch);
            } catch (Exception ex) {
                log.error("Failed to ingest matchId {}. Continuing with next match.", ref.matchId(), ex);
            }
        }
    }

    private List<SeriesMatchRef> extractMatchReferences(List<SeriesDTO> recentSeries) {
        List<SeriesMatchRef> refs = new ArrayList<>();
        for (SeriesDTO seriesDTO : recentSeries) {
            for (Long matchId : seriesDTO.getMatchIds()) {
                refs.add(new SeriesMatchRef(
                        matchId,
                        seriesDTO.getSeriesId(),
                        seriesDTO.getSeriesName(),
                        seriesDTO.getSeriesType()
                ));
            }
        }
        return refs;
    }

    private Match upsertMatch(SeriesMatchRef ref, JsonNode root, Match existingMatch) {
        Match match = existingMatch != null ? existingMatch : new Match();

        match.setId(ref.matchId());
        match.setSeriesId(ref.seriesId());
        match.setSeriesName(defaultString(ref.seriesName(), "Unknown Series"));
        match.setMatchType(resolveMatchType(ref.seriesType()));
        match.setMatchContext(Match.MatchContext.GROUP);

        JsonNode matchInfo = root.path("matchHeader");
        String team1 = matchInfo.path("team1").path("name").asText("Team 1");
        String team2 = matchInfo.path("team2").path("name").asText("Team 2");
        String venue = matchInfo.path("venueInfo").path("ground").asText("Unknown Venue");

        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setVenue(venue);
        match.setMatchDate(LocalDate.now());
        match.setIngestedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        return matchRepository.save(match);
    }

    private void parseAndUpsertBattingInnings(JsonNode root, Match match, Map<Long, Player> playersInMatch) {
        Map<Long, String> dismissalMap = extractDismissalTypeByPlayerId(root);
        JsonNode inningsArray = root.path("scoreCard");

        if (!inningsArray.isArray()) {
            return;
        }

        for (JsonNode innings : inningsArray) {
            JsonNode batsmenData = innings.path("batTeamDetails").path("batsmenData");
            if (!batsmenData.isObject()) {
                continue;
            }

            batsmenData.fields().forEachRemaining(entry -> {
                JsonNode batNode = entry.getValue();
                Long playerId = batNode.path("batId").asLong(0L);
                if (playerId == 0L) {
                    return;
                }

                Player player = resolveAndUpsertPlayer(playerId, batNode.path("batName").asText("Unknown"), playersInMatch);
                BattingInnings batting = battingInningsRepository
                        .findByPlayer_IdAndMatch_Id(playerId, match.getId())
                        .orElse(new BattingInnings());

                batting.setPlayer(player);
                batting.setMatch(match);
                batting.setRunsScored(batNode.path("runs").asInt(0));
                batting.setBallsFaced(batNode.path("balls").asInt(0));
                batting.setStrikeRate(asBigDecimal(batNode.path("strikeRate"), 6, 2));

                boolean notOut = batNode.path("notOut").asBoolean(false);
                String dismissal = dismissalMap.getOrDefault(playerId, "UNKNOWN");
                boolean retiredHurt = "retired_hurt".equalsIgnoreCase(dismissal);
                boolean didNotBat = batNode.path("balls").asInt(0) == 0 && batNode.path("runs").asInt(0) == 0;

                batting.setNotOut(notOut);
                batting.setRetiredHurt(retiredHurt);
                batting.setDidNotBat(didNotBat);
                batting.setDismissalType(dismissal);
                batting.setMatchSequenceRank(1);
                batting.setWeightedScore(BigDecimal.ZERO.setScale(2));
                batting.setBowlingAttackScore(BigDecimal.ZERO.setScale(2));

                battingInningsRepository.save(batting);
            });
        }
    }

    private void parseAndUpsertBowlingInnings(JsonNode root, Match match, Map<Long, Player> playersInMatch) {
        JsonNode inningsArray = root.path("scoreCard");
        if (!inningsArray.isArray()) {
            return;
        }

        for (JsonNode innings : inningsArray) {
            JsonNode bowlersData = innings.path("bowlTeamDetails").path("bowlersData");
            if (!bowlersData.isObject()) {
                continue;
            }

            bowlersData.fields().forEachRemaining(entry -> {
                JsonNode bowlNode = entry.getValue();
                Long playerId = bowlNode.path("bowlId").asLong(0L);
                if (playerId == 0L) {
                    return;
                }

                Player player = resolveAndUpsertPlayer(playerId, bowlNode.path("bowlName").asText("Unknown"), playersInMatch);
                BowlingInnings bowling = bowlingInningsRepository
                        .findByPlayer_IdAndMatch_Id(playerId, match.getId())
                        .orElse(new BowlingInnings());

                bowling.setPlayer(player);
                bowling.setMatch(match);
                bowling.setOversBowled(asOversDecimal(bowlNode.path("overs").asText("0.0")));
                bowling.setMaidens(bowlNode.path("maidens").asInt(0));
                bowling.setRunsConceded(bowlNode.path("runs").asInt(0));
                bowling.setWickets(bowlNode.path("wickets").asInt(0));
                bowling.setEconomy(asBigDecimal(bowlNode.path("economy"), 5, 2));

                boolean didNotBowl = bowling.getOversBowled().compareTo(BigDecimal.ZERO) == 0;
                bowling.setDidNotBowl(didNotBowl);
                bowling.setInjuryWithdrawal(false);
                bowling.setMatchSequenceRank(1);
                bowling.setWeightedScore(BigDecimal.ZERO.setScale(2));
                bowling.setBattingLineupScore(BigDecimal.ZERO.setScale(2));

                bowlingInningsRepository.save(bowling);
            });
        }
    }

    private Player resolveAndUpsertPlayer(Long playerId, String fallbackName, Map<Long, Player> playersInMatch) {
        if (playersInMatch.containsKey(playerId)) {
            return playersInMatch.get(playerId);
        }

        Player player = playerRepository.findById(playerId).orElseGet(Player::new);

        try {
            ExternalPlayerProfileDTO profile = cricketApiService.fetchPlayerProfile(playerId);
            player.setId(playerId);
            player.setName(defaultString(profile.getName(), fallbackName));
            player.setTeam(defaultString(profile.getTeamName(), "Unknown"));
            player.setRole(resolvePlayerRole(profile.getRole()));
        } catch (Exception ex) {
            log.warn("Unable to fetch profile for player {}. Using fallback data.", playerId, ex);
            player.setId(playerId);
            player.setName(defaultString(fallbackName, "Unknown"));
            player.setTeam(defaultString(player.getTeam(), "Unknown"));
            player.setRole(Objects.requireNonNullElse(player.getRole(), Player.PlayerRole.BATSMAN));
        }

        if (player.getCreatedAt() == null) {
            player.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        }
        player.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        player.setIsActive(true);

        Player saved = playerRepository.save(player);
        playersInMatch.put(playerId, saved);
        return saved;
    }

    private Match.MatchType resolveMatchType(String seriesType) {
        if (seriesType == null) {
            return Match.MatchType.ODI;
        }
        String normalized = seriesType.trim().toUpperCase();
        if (normalized.contains("T20")) {
            return Match.MatchType.T20I;
        }
        if (normalized.contains("TEST")) {
            return Match.MatchType.TEST;
        }
        return Match.MatchType.ODI;
    }

    private Player.PlayerRole resolvePlayerRole(String role) {
        if (role == null) {
            return Player.PlayerRole.BATSMAN;
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.contains("BOWLING ALLROUNDER") || normalized.contains("BOWLING_ALLROUNDER")) {
            return Player.PlayerRole.BOWLING_ALLROUNDER;
        }
        if (normalized.contains("ALLROUNDER") || normalized.contains("BATTING ALLROUNDER") || normalized.contains("BATTING_ALLROUNDER")) {
            return Player.PlayerRole.BATTING_ALLROUNDER;
        }
        if (normalized.contains("BOWL")) {
            return Player.PlayerRole.BOWLER;
        }
        return Player.PlayerRole.BATSMAN;
    }

    private Map<Long, String> extractDismissalTypeByPlayerId(JsonNode root) {
        Map<Long, String> dismissalMap = new HashMap<>();
        JsonNode inningsArray = root.path("scoreCard");
        if (!inningsArray.isArray()) {
            return dismissalMap;
        }

        for (JsonNode innings : inningsArray) {
            JsonNode wickets = innings.path("wicketsData");
            if (!wickets.isArray()) {
                continue;
            }

            wickets.forEach(wicket -> {
                Long playerId = wicket.path("batId").asLong(0L);
                if (playerId == 0L) {
                    return;
                }
                String wicketType = wicket.path("wicketType").asText("UNKNOWN");
                dismissalMap.put(playerId, wicketType);
            });
        }

        return dismissalMap;
    }

    private BigDecimal asBigDecimal(JsonNode node, int precision, int scale) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        return node.decimalValue().setScale(scale, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal asOversDecimal(String overs) {
        try {
            return new BigDecimal(overs).setScale(1, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(1);
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record SeriesMatchRef(Long matchId, Long seriesId, String seriesName, String seriesType) {
    }
}
