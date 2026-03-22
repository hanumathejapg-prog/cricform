package com.cricform.repository;

import com.cricform.model.BattingInnings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattingInningsRepository extends JpaRepository<BattingInnings, Long> {

    Optional<BattingInnings> findByPlayer_IdAndMatch_Id(Long playerId, Long matchId);

    List<BattingInnings> findByPlayer_IdOrderByMatch_MatchDateDescMatch_IdDesc(Long playerId);

    List<BattingInnings> findByMatch_Id(Long matchId);
}
