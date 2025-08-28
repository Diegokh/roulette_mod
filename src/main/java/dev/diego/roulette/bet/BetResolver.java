package dev.diego.roulette.bet;

import dev.diego.roulette.RouletteMod;
import dev.diego.roulette.config.RouletteConfig;

import java.util.List;

public final class BetResolver {

    public static long payoutFor(BetPlacement b, int winning) {
        if (!wins(b, winning, RouletteMod.CFG)) return 0L;
        return (long) b.amount() * b.type().multiplier();
    }

    private static boolean wins(BetPlacement b, int n, RouletteConfig cfg) {
        return switch (b.type()) {
            case STRAIGHT, SPLIT, STREET, CORNER, LINE -> b.covered().contains(n);
            case DOZEN -> inDozen(n, b.meta());
            case COLUMN -> inColumn(n, b.meta());
            case COLOR -> cfg.redNumbers.contains(n) == "RED".equalsIgnoreCase(b.meta());
            case EVEN_ODD -> (n % 2 == 0) == "EVEN".equalsIgnoreCase(b.meta());
            case LOW_HIGH -> (n <= 18) == "LOW".equalsIgnoreCase(b.meta());
        };
    }

    private static boolean inDozen(int n, String meta) {
        return switch (meta == null ? "" : meta.toUpperCase()) {
            case "D1" -> n >= 1 && n <= 12;
            case "D2" -> n >= 13 && n <= 24;
            case "D3" -> n >= 25 && n <= 36;
            default -> false;
        };
    }

    private static boolean inColumn(int n, String meta) {
        int col = (n - 1) % 3; // 0,1,2
        return switch (meta == null ? "" : meta.toUpperCase()) {
            case "C1" -> col == 0;
            case "C2" -> col == 1;
            case "C3" -> col == 2;
            default -> false;
        };
    }
}
