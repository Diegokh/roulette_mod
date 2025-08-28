package dev.diego.roulette.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("config", "roulette.json");

    public static RouletteConfig init() {
        try {
            if (Files.notExists(FILE)) {
                Files.createDirectories(FILE.getParent());
                RouletteConfig def = new RouletteConfig();
                try (Writer w = Files.newBufferedWriter(FILE)) {
                    GSON.toJson(def, w);
                }
                return def;
            }
            try (Reader r = Files.newBufferedReader(FILE)) {
                RouletteConfig cfg = GSON.fromJson(r, RouletteConfig.class);
                return (cfg != null) ? cfg : new RouletteConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new RouletteConfig();
        }
    }
}
