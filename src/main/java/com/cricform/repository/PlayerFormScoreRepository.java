package com.cricform.repository;

import com.cricform.model.PlayerFormScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerFormScoreRepository extends JpaRepository<PlayerFormScore, Long> {

    Optional<PlayerFormScore> findByPlayer_IdAndFormat(Long playerId, PlayerFormScore.Format format);

    List<PlayerFormScore> findByFormat(PlayerFormScore.Format format);

    @Query("""
            select pfs from PlayerFormScore pfs
            where pfs.format = :format
              and pfs.player.role in :roles
              and pfs.formScore is not null
            order by pfs.formScore desc
            """)
    List<PlayerFormScore> findTopByFormatAndRoles(@Param("format") PlayerFormScore.Format format,
                                                  @Param("roles") List<com.cricform.model.Player.PlayerRole> roles,
                                                  org.springframework.data.domain.Pageable pageable);
}
