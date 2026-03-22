package com.cricform.repository;

import com.cricform.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByIsActiveTrue();

    List<Player> findByIsActiveTrueAndUpdatedAtAfter(LocalDateTime threshold);

    List<Player> findByNameContainingIgnoreCase(String name);
}
