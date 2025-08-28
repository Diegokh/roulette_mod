package dev.diego.roulette.config;

import java.util.*;

public class RouletteConfig {
    public Map<String, Integer> chipMultipliers = Map.of(
            "STRAIGHT", 35,
            "SPLIT", 17,
            "STREET", 11,
            "CORNER", 8,
            "LINE", 5,
            "DOZEN", 2,
            "COLUMN", 2,
            "COLOR", 1,
            "EVEN_ODD", 1,
            "LOW_HIGH", 1
    );

    public Map<String, Integer> materialToChips = new LinkedHashMap<>(Map.of(
            "minecraft:diamond", 10,
            "minecraft:emerald", 15,
            "minecraft:gold_ingot", 5,
            "minecraft:iron_ingot", 3,
            "minecraft:redstone", 1
    ));

    public Map<String, Integer> chipsToMaterial = new LinkedHashMap<>(Map.of(
            "minecraft:diamond", 100,
            "minecraft:emerald", 150,
            "minecraft:gold_ingot", 50,
            "minecraft:iron_ingot", 30,
            "minecraft:redstone", 10
    ));

    public int bettingWindowSeconds = 20;
    public int minBet = 1;
    public int maxBet = 256;

    public List<Integer> wheelOrderEuropeanNoZero = List.of(
            32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26
    );

    public Set<Integer> redNumbers = Set.of(
            1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
    );
}
