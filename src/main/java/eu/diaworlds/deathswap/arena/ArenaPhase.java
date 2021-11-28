package eu.diaworlds.deathswap.arena;

import lombok.Getter;

public enum ArenaPhase {
    WAITING("Čeká"),
    STARTING("Začíná"),
    IN_GAME("Ve hře"),
    ENDING("Končí"),
    STOPPING("Restart");

    @Getter
    private final String displayName;

    ArenaPhase(String displayName) {
        this.displayName = displayName;
    }
}