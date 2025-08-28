package dev.diego.roulette;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RouletteTestCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("setsaldo")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(argument("cantidad", LongArgumentType.longArg(0))
                            .executes(ctx -> setSaldo(ctx))));

            dispatcher.register(literal("saldo")
                    .executes(ctx -> mostrarSaldo(ctx)));
        });
    }

    private static int setSaldo(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        long cantidad = LongArgumentType.getLong(ctx, "cantidad");

        dev.diego.roulette.persist.PlayerDataStorage.get((ServerWorld) player.getWorld())
                .balances().put(player.getUuid(), cantidad);

        dev.diego.roulette.persist.PlayerDataStorage.markDirtyStatic();
        ctx.getSource().sendFeedback(() -> Text.literal("Saldo actualizado a " + cantidad + " fichas"), false);
        return 1;
    }

    private static int mostrarSaldo(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        long saldo = dev.diego.roulette.persist.PlayerDataStorage.get((ServerWorld) player.getWorld())
                .balances().getOrDefault(player.getUuid(), 0L);

        ctx.getSource().sendFeedback(() -> Text.literal("Tu saldo actual es " + saldo + " fichas"), false);
        return 1;
    }
}
