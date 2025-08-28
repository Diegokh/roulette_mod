package dev.diego.roulette.persist;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDataStorage extends PersistentState {
    private static final String KEY = "roulette_players";

    // Codec que convierte entre NbtCompound y nuestra clase
    private static final Codec<PlayerDataStorage> CODEC = NbtCompound.CODEC.xmap(
            PlayerDataStorage::fromNbt,
            s -> s.writeNbt(new NbtCompound(), null)
    );

    // TYPE: usa DataFixTypes.LEVEL en este mapeo
    private static final PersistentStateType<PlayerDataStorage> TYPE =
            new PersistentStateType<>(KEY, (Supplier<PlayerDataStorage>) PlayerDataStorage::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, Long> balances = new HashMap<>();
    private static PlayerDataStorage INSTANCE;

    public PlayerDataStorage() {}

    public static void load(ServerWorld world, Map<UUID, Long> out) {
        PlayerDataStorage data = get(world);
        out.clear();
        out.putAll(data.balances);
    }

    public static PlayerDataStorage get(ServerWorld world) {
        if (INSTANCE != null) return INSTANCE;
        INSTANCE = world.getPersistentStateManager().getOrCreate(TYPE);
        return INSTANCE;
    }

    public static void markDirtyStatic() {
        if (INSTANCE != null) INSTANCE.markDirty();
    }

    public Map<UUID, Long> balances() {
        return balances;
    }

    public static PlayerDataStorage fromNbt(NbtCompound nbt) {
        PlayerDataStorage s = new PlayerDataStorage();
        Optional<NbtCompound> maybeList = nbt.getCompound("balances");
        maybeList.ifPresent(list -> {
            for (String k : list.getKeys()) {
                long v = list.getLong(k).orElse(0L);
                try {
                    UUID u = UUID.fromString(k);
                    s.balances.put(u, v);
                } catch (Exception ignored) {}
            }
        });
        return s;
    }

    // Firma 1.21.8
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound b = new NbtCompound();
        for (var e : balances.entrySet()) {
            b.putLong(e.getKey().toString(), e.getValue());
        }
        nbt.put("balances", b);
        return nbt;
    }
}
