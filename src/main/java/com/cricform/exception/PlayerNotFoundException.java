package com.cricform.exception;

public class PlayerNotFoundException extends RuntimeException {

    private final Long playerId;

    public PlayerNotFoundException(Long playerId) {
        super("Player not found: " + playerId);
        this.playerId = playerId;
    }

    public Long getPlayerId() {
        return playerId;
    }
}
