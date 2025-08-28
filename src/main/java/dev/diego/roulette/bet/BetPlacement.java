package dev.diego.roulette.bet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BetPlacement {
    private final UUID player;
    private final BetType type;
    private final List<Integer> covered;
    private final String meta; // D1/D2/D3, C1/C2/C3, RED/BLACK, EVEN/ODD, LOW/HIGH
    private final int amount;

    private BetPlacement(UUID player, BetType type, List<Integer> covered, String meta, int amount) {
        this.player = player;
        this.type = type;
        this.covered = covered;
        this.meta = meta;
        this.amount = amount;
    }

    public static BetPlacement of(UUID player, BetType type, List<Integer> covered, String meta, int amount) {
        return new BetPlacement(player, type, new ArrayList<>(covered), meta, amount);
    }

    public UUID player() { return player; }
    public BetType type() { return type; }
    public List<Integer> covered() { return covered; }
    public String meta() { return meta; }
    public int amount() { return amount; }
}
