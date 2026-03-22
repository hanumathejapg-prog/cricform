package com.cricform.controller;

import com.cricform.model.Player;
import com.cricform.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final PlayerRepository playerRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Player>> searchPlayers(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Player> players = playerRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .toList();
        return ResponseEntity.ok(players);
    }
}
