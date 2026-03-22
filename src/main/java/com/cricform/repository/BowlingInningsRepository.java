package com.cricform.repository;

import com.cricform.model.BowlingInnings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BowlingInningsRepository extends JpaRepository<BowlingInnings, Long> {

    Optional<BowlingInnings> findByPlayer_IdAndMatch_Id(Long playerId, Long matchId);

    List<BowlingInnings> findByPlayer_IdOrderByMatch_MatchDateDescMatch_IdDesc(Long playerId);

    List<BowlingInnings> findByMatch_Id(Long matchId);
}
