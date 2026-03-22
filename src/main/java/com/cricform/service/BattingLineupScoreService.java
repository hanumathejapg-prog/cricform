package com.cricform.service;

import com.cricform.model.BattingInnings;
import com.cricform.model.Match;
import com.cricform.model.PlayerFormScore;
import com.cricform.repository.BattingInningsRepository;
import com.cricform.repository.MatchRepository;
import com.cricform.repository.PlayerFormScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BattingLineupScoreService {

    private final BattingInningsRepository battingInningsRepository;
    private final MatchRepository matchRepository;
    private final PlayerFormScoreRepository playerFormScoreRepository;

    public double getBattingLineupScore(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            return 50.0;
        }

        PlayerFormScore.Format format = PlayerFormScore.Format.valueOf(match.getMatchType().name());
        List<BattingInnings> batters = battingInningsRepository.findByMatch_Id(matchId);

        return batters.stream()
                .map(BattingInnings::getPlayer)
                .map(player -> playerFormScoreRepository.findByPlayer_IdAndFormat(player.getId(), format)
                        .map(pfs -> pfs.getFormScore() == null ? 50.0 : pfs.getFormScore().doubleValue())
                        .orElse(50.0))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(50.0);
    }
}
