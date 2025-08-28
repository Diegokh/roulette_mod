package dev.diego.roulette;

import dev.diego.roulette.bet.BetPlacement;
import dev.diego.roulette.bet.BetType;
import dev.diego.roulette.config.ConfigManager;
import dev.diego.roulette.config.RouletteConfig;
import dev.diego.roulette.economy.EconomyService;
import dev.diego.roulette.persist.PlayerDataStorage;
import dev.diego.roulette.table.RouletteManager;
import dev.diego.roulette.table.RouletteTable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public class RouletteMod implements ModInitializer {
    public static final String MODID = "roulette";
    public static RouletteConfig CFG;

    @Override
    public void onInitialize() {
        CFG = ConfigManager.init();
        EconomyService.init();
        RouletteManager.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(literal("roulette")
                    .then(literal("bank").executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        EconomyService.openBank(p);
                        return 1;
                    }))
                    .then(literal("create").executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        RouletteManager.createTable(p.getWorld(), p.getBlockPos());
                        p.sendMessage(Text.of("Mesa de ruleta creada."), false);
                        return 1;
                    }))
                    .then(literal("open").executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        RouletteTable t = RouletteManager.nearbyTable(p);
                        if (t == null) { p.sendMessage(Text.of("No hay mesa cerca."), false); return 0; }
                        t.openBets(p.getServer());
                        broadcast(p.getServer(), "Apuestas abiertas por " + CFG.bettingWindowSeconds + "s.");
                        return 1;
                    }))
                    .then(literal("spin").executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                        RouletteTable t = RouletteManager.nearbyTable(p);
                        if (t == null) { p.sendMessage(Text.of("No hay mesa cerca."), false); return 0; }
                        t.forceSpinNow(p.getServer());
                        return 1;
                    }))
                    .then(literal("chips")
                            .then(literal("balance").executes(ctx -> {
                                ServerPlayerEntity p = ctx.getSource().getPlayer();
                                long c = EconomyService.getChips(p);
                                p.sendMessage(Text.of("Tienes " + c + " fichas."), false);
                                return 1;
                            })))
                    .then(literal("bet")
                            .then(literal("straight")
                                    .then(argument("numero", IntegerArgumentType.integer(1, 36))
                                            .then(argument("fichas", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                                        int n = IntegerArgumentType.getInteger(ctx, "numero");
                                                        int amt = IntegerArgumentType.getInteger(ctx, "fichas");
                                                        placeBet(ctx.getSource().getServer(), p, BetType.STRAIGHT, List.of(n), null, amt);
                                                        return 1;
                                                    }))))
                            .then(literal("dozen")
                                    .then(argument("D", StringArgumentType.word()) // D1/D2/D3
                                            .then(argument("fichas", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                                        String d = StringArgumentType.getString(ctx, "D");
                                                        int amt = IntegerArgumentType.getInteger(ctx, "fichas");
                                                        placeBet(ctx.getSource().getServer(), p, BetType.DOZEN, List.of(), d, amt);
                                                        return 1;
                                                    }))))
                            .then(literal("color")
                                    .then(argument("C", StringArgumentType.word()) // RED/BLACK
                                            .then(argument("fichas", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                                        String c = StringArgumentType.getString(ctx, "C");
                                                        int amt = IntegerArgumentType.getInteger(ctx, "fichas");
                                                        placeBet(ctx.getSource().getServer(), p, BetType.COLOR, List.of(), c.toUpperCase(), amt);
                                                        return 1;
                                                    }))))
                            .then(literal("evenodd")
                                    .then(argument("E", StringArgumentType.word()) // EVEN/ODD
                                            .then(argument("fichas", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                                        String e = StringArgumentType.getString(ctx, "E");
                                                        int amt = IntegerArgumentType.getInteger(ctx, "fichas");
                                                        placeBet(ctx.getSource().getServer(), p, BetType.EVEN_ODD, List.of(), e.toUpperCase(), amt);
                                                        return 1;
                                                    }))))
                            .then(literal("lowhigh")
                                    .then(argument("L", StringArgumentType.word()) // LOW/HIGH
                                            .then(argument("fichas", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                                        String l = StringArgumentType.getString(ctx, "L");
                                                        int amt = IntegerArgumentType.getInteger(ctx, "fichas");
                                                        placeBet(ctx.getSource().getServer(), p, BetType.LOW_HIGH, List.of(), l.toUpperCase(), amt);
                                                        return 1;
                                                    })))))
            );
        });
    }

    private static void placeBet(MinecraftServer server, ServerPlayerEntity p, BetType type, List<Integer> covered, String meta, int amt) {
        RouletteTable t = RouletteManager.nearbyTable(p);
        if (t == null) { p.sendMessage(Text.of("No hay mesa cerca."), false); return; }
        if (!t.isOpen()) { p.sendMessage(Text.of("Las apuestas no están abiertas."), false); return; }
        if (!EconomyService.trySpendChips(p, amt)) {
            p.sendMessage(Text.of("No tienes fichas suficientes."), false);
            return;
        }
        BetPlacement bp = BetPlacement.of(p.getUuid(), type, covered, meta, amt);
        t.addBet(bp);
        p.sendMessage(Text.of("Apuesta aceptada: " + type + " x" + amt), false);
    }

    public static void broadcast(MinecraftServer server, String msg) {
        server.getPlayerManager().broadcast(Text.of("[Ruleta] " + msg), false);
    }
}
