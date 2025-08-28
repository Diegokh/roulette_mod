package dev.diego.roulette.table;

import dev.diego.roulette.RouletteMod;
import dev.diego.roulette.bet.BetPlacement;
import dev.diego.roulette.bet.BetResolver;
import dev.diego.roulette.economy.EconomyService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class RouletteTable {
    public enum State { OPEN, LOCKED, SPINNING, PAYING, IDLE }

    private final UUID id = UUID.randomUUID();
    private final World world;
    private final BlockPos pos;

    private State state = State.IDLE;
    private final Map<UUID, List<BetPlacement>> bets = new HashMap<>();
    private int winningNumber = -1;
    private long openUntil = 0L;
    private final Random rng = new Random();

    public RouletteTable(World w, BlockPos p) {
        this.world = w;
        this.pos = p.toImmutable();
    }

    public boolean isOpen() {
        return state == State.OPEN && System.currentTimeMillis() < openUntil;
    }

    public void openBets(MinecraftServer server) {
        bets.clear();
        state = State.OPEN;
        openUntil = System.currentTimeMillis() + (RouletteMod.CFG.bettingWindowSeconds * 1000L);
        server.getPlayerManager().getPlayerList().forEach(pl ->
                pl.sendMessage(Text.of("Apuestas abiertas en la mesa cercana. Usa /roulette bet ..."), false)
        );
        server.submit(() -> {
            // Cierre automático
            try {
                Thread.sleep(RouletteMod.CFG.bettingWindowSeconds * 1000L);
            } catch (InterruptedException ignored) {}
            if (state == State.OPEN) lockAndSpin(server);
        });
    }

    public void forceSpinNow(MinecraftServer server) {
        if (state == State.OPEN) lockAndSpin(server);
    }

    private void lockAndSpin(MinecraftServer server) {
        state = State.LOCKED;
        // Elegir ganador
        winningNumber = RouletteMod.CFG.wheelOrderEuropeanNoZero.get(rng.nextInt(36));
        state = State.SPINNING;
        announce(server, "Girando... número ganador en breve.");
        // Simulación breve de giro (placeholder para animación con minecart)
        server.submit(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            pay(server);
        });
    }

    private void pay(MinecraftServer server) {
        state = State.PAYING;
        announce(server, "Número ganador: " + winningNumber);

        Map<UUID, Long> earnings = new HashMap<>();
        bets.forEach((uuid, list) -> {
            long total = 0L;
            for (BetPlacement b : list) total += BetResolver.payoutFor(b, winningNumber);
            if (total > 0) earnings.put(uuid, total);
        });

        for (var e : earnings.entrySet()) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            if (p != null) {
                EconomyService.addChips(p, e.getValue());
                p.sendMessage(Text.of("Ganaste " + e.getValue() + " fichas."), false);
            }
        }
        state = State.IDLE;
        bets.clear();
    }

    public void addBet(BetPlacement b) {
        bets.computeIfAbsent(b.player(), k -> new ArrayList<>()).add(b);
    }

    public World getWorld() { return world; }
    public BlockPos getPos() { return pos; }
    public int getWinningNumber() { return winningNumber; }

    private void announce(MinecraftServer server, String msg) {
        server.getPlayerManager().broadcast(Text.of("[Ruleta] " + msg), false);
    }
}
