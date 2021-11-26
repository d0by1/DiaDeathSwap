package eu.diaworlds.deathswap.arena;

import lombok.Getter;

public enum Phase {
    WAITING("Čeká"),
    STARTING("Začíná"),
    IN_GAME("Ve hře"),
    ENDING("Končí"),
    RESETTING("Reset");

    @Getter
    private final String displayName;

    Phase(String displayName) {
        this.displayName = displayName;
    }
}