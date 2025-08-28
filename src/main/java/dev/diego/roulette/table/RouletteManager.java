package dev.diego.roulette.table;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class RouletteManager {
    private static final List<RouletteTable> TABLES = new ArrayList<>();

    public static void init() {}

    public static RouletteTable createTable(World w, BlockPos p) {
        RouletteTable t = new RouletteTable(w, p);
        TABLES.add(t);
        return t;
    }

    public static RouletteTable nearbyTable(ServerPlayerEntity p) {
        double best = Double.MAX_VALUE;
        RouletteTable found = null;
        for (RouletteTable t : TABLES) {
            if (t.getWorld() != p.getWorld()) continue;
            double d = p.getPos().squaredDistanceTo(t.getPos().toCenterPos());
            if (d < best && d <= 64 * 64) {
                best = d;
                found = t;
            }
        }
        return found;
    }
}
