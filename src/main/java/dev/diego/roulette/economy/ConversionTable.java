package dev.diego.roulette.economy;

import dev.diego.roulette.RouletteMod;
import dev.diego.roulette.config.RouletteConfig;

import java.util.*;

public final class ConversionTable {
    private final Map<String, Integer> matToChips;
    private final Map<String, Integer> chipsToMat;
    private final List<String> payoutOrder; // estable

    private ConversionTable(Map<String, Integer> a, Map<String, Integer> b) {
        this.matToChips = Map.copyOf(a);
        this.chipsToMat = Map.copyOf(b);
        this.payoutOrder = new ArrayList<>(b.keySet());
    }

    public static ConversionTable fromConfig() {
        RouletteConfig c = RouletteMod.CFG;
        return new ConversionTable(c.materialToChips, c.chipsToMaterial);
    }

    public int materialToChips(String id) { return matToChips.getOrDefault(id, 0); }
    public int chipsToMaterial(String id) { return chipsToMat.getOrDefault(id, Integer.MAX_VALUE / 2); }
    public List<String> sortedPayoutOrder() { return payoutOrder; }
}
