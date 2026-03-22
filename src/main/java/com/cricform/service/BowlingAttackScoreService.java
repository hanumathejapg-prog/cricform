package com.cricform.service;

import com.cricform.model.BowlingInnings;
import com.cricform.model.Match;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.BowlingInningsRepository;
import com.cricform.repository.MatchRepository;
import com.cricform.repository.PlayerFormScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BowlingAttackScoreService {

    private final BowlingInningsRepository bowlingInningsRepository;
    private final MatchRepository matchRepository;
    private final PlayerFormScoreRepository playerFormScoreRepository;

    public double getBowlingAttackScore(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            return 50.0;
        }

        PlayerFormScore.Format format = PlayerFormScore.Format.valueOf(match.getMatchType().name());
        List<BowlingInnings> bowlers = bowlingInningsRepository.findByMatch_Id(matchId);

        return bowlers.stream()
                .map(BowlingInnings::getPlayer)
                .map(player -> playerFormScoreRepository.findByPlayer_IdAndFormat(player.getId(), format)
                        .map(pfs -> pfs.getFormScore() == null ? 50.0 : pfs.getFormScore().doubleValue())
                        .orElse(50.0))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(50.0);
    }

    public double getBowlingAttackMultiplier(double score) {
        if (score < 30) {
            return 0.8;
        }
        if (score < 45) {
            return 0.9;
        }
        if (score < 60) {
            return 1.0;
        }
        if (score < 75) {
            return 1.1;
        }
        if (score <= 85) {
            return 1.2;
        }
        return 1.3;
    }
}
