package com.cricform.service;

import com.cricform.dto.LeaderboardEntryDTO;
import com.cricform.dto.PlayerProfileDTO;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.PlayerFormScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final long TTL_24_HOURS_SECONDS = 86400;
    private static final long TTL_7_DAYS_SECONDS = 604800;

    private final RedisTemplate<String, Object> redisTemplate;
    private final PlayerFormScoreRepository playerFormScoreRepository;

    public String playerFormKey(Long playerId, String format) {
        return "cricform:player:" + playerId + ":form:" + format;
    }

    public String leaderboardKey(String type, String format) {
        return "cricform:leaderboard:" + type + ":" + format;
    }

    public String playerProfileKey(Long playerId) {
        return "cricform:player:" + playerId + ":profile";
    }

    public String searchIndexKey() {
        return "cricform:search:index";
    }

    public String bowlingAttackKey(Long matchId) {
        return "cricform:match:" + matchId + ":bowling_attack";
    }

    public String lastRefreshKey() {
        return "cricform:last_refresh";
    }

    public PlayerFormScore getPlayerFormScore(Long playerId, String format) {
        return (PlayerFormScore) redisTemplate.opsForValue().get(playerFormKey(playerId, format));
    }

    public void putPlayerFormScore(PlayerFormScore score) {
        redisTemplate.opsForValue().set(
                playerFormKey(score.getPlayer().getId(), score.getFormat().name()),
                score,
                TTL_24_HOURS_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @SuppressWarnings("unchecked")
    public List<LeaderboardEntryDTO> getLeaderboard(String type, String format) {
        return (List<LeaderboardEntryDTO>) redisTemplate.opsForValue().get(leaderboardKey(type, format));
    }

    public void putLeaderboard(String type, String format, List<LeaderboardEntryDTO> leaderboard) {
        redisTemplate.opsForValue().set(
                leaderboardKey(type, format),
                leaderboard,
                TTL_24_HOURS_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public PlayerProfileDTO getPlayerProfile(Long playerId) {
        return (PlayerProfileDTO) redisTemplate.opsForValue().get(playerProfileKey(playerId));
    }

    public void putPlayerProfile(PlayerProfileDTO profile) {
        redisTemplate.opsForValue().set(
                playerProfileKey(profile.getPlayerId()),
                profile,
                TTL_24_HOURS_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @SuppressWarnings("unchecked")
    public List<String> getSearchIndex() {
        return (List<String>) redisTemplate.opsForValue().get(searchIndexKey());
    }

    public void putSearchIndex(List<String> playerNames) {
        redisTemplate.opsForValue().set(searchIndexKey(), playerNames, TTL_7_DAYS_SECONDS, TimeUnit.SECONDS);
    }

    public Double getBowlingAttackScore(Long matchId) {
        return (Double) redisTemplate.opsForValue().get(bowlingAttackKey(matchId));
    }

    public void putBowlingAttackScore(Long matchId, Double score) {
        redisTemplate.opsForValue().set(bowlingAttackKey(matchId), score, TTL_7_DAYS_SECONDS, TimeUnit.SECONDS);
    }

    public void invalidateAllFormScores() {
        deleteByPattern("cricform:player:*");
        deleteByPattern("cricform:leaderboard:*");
    }

    public void repopulateAllFormScores() {
        for (String format : List.of("T20I", "ODI", "TEST")) {
            List<PlayerFormScore> scores = playerFormScoreRepository.findByFormat(PlayerFormScore.Format.valueOf(format));
            scores.forEach(this::putPlayerFormScore);
        }
    }

    public void updateLastRefreshTimestamp() {
        redisTemplate.opsForValue().set(lastRefreshKey(), LocalDateTime.now().toString());
    }

    public boolean isDataStale() {
        Object value = redisTemplate.opsForValue().get(lastRefreshKey());
        if (value == null) {
            return true;
        }
        LocalDateTime last = LocalDateTime.parse(value.toString());
        return Duration.between(last, LocalDateTime.now()).toHours() >= 26;
    }

    public long ttlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl == null ? -2 : ttl;
    }

    private void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
