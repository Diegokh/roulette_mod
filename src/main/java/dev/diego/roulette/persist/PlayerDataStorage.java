package dev.diego.roulette.persist;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStorage extends PersistentState {
    private static final String KEY = "roulette_players";
    private final Map<UUID, Long> balances = new HashMap<>();
    private static PlayerDataStorage INSTANCE;

    public static void load(Map<UUID, Long> out) {
        // Se obtiene al primer mundo del servidor
        // Este metodo sera completado cuando haya un server disponible
        // En Fabric suele usarse un callback de server start; aquí simplificamos:
    }

    public static PlayerDataStorage get(ServerWorld world) {
        if (INSTANCE != null) return INSTANCE;
        INSTANCE = world.getPersistentStateManager().getOrCreate(PlayerDataStorage::fromNbt, PlayerDataStorage::new, KEY);
        return INSTANCE;
    }

    public static void markDirty() {
        if (INSTANCE != null) INSTANCE.markDirty();
    }

    public Map<UUID, Long> balances() { return balances; }

    public static PlayerDataStorage fromNbt(NbtCompound nbt) {
        PlayerDataStorage s = new PlayerDataStorage();
        var list = nbt.getCompound("balances");
        for (String k : list.getKeys()) {
            try {
                UUID u = UUID.fromString(k);
                long v = list.getLong(k);
                s.balances.put(u, v);
            } catch (Exception ignored) {}
        }
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound b = new NbtCompound();
        for (var e : balances.entrySet()) {
            b.putLong(e.getKey().toString(), e.getValue());
        }
        nbt.put("balances", b);
        return nbt;
    }
}
