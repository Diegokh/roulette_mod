package dev.diego.roulette.economy;

import dev.diego.roulette.persist.PlayerDataStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public final class EconomyService {
    private static final Map<UUID, Long> CHIP_BALANCES = new HashMap<>();
    private static ConversionTable conversion;

    public static void init(MinecraftServer server) {
        conversion = ConversionTable.fromConfig();
        ServerWorld world = server.getOverworld();
        PlayerDataStorage.load(world, CHIP_BALANCES);
    }

    public static long getChips(ServerPlayerEntity p) {
        return CHIP_BALANCES.getOrDefault(p.getUuid(), 0L);
    }

    public static void addChips(ServerPlayerEntity p, long delta) {
        CHIP_BALANCES.merge(p.getUuid(), delta, Long::sum);
        PlayerDataStorage.markDirtyStatic();
    }

    public static boolean trySpendChips(ServerPlayerEntity p, long amount) {
        long cur = getChips(p);
        if (cur < amount) return false;
        CHIP_BALANCES.put(p.getUuid(), cur - amount);
        PlayerDataStorage.markDirtyStatic();
        return true;
    }

    public static void openBank(ServerPlayerEntity p) {
        p.sendMessage(Text.of("Banco: usa /deposit [item] [cantidad] y /withdraw [chips]"), false);
    }

    public static void depositCli(ServerPlayerEntity p, String itemId, int amount) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            p.sendMessage(Text.of("Item inválido: " + itemId), false);
            return;
        }
        int rate = conversion.materialToChips(itemId);
        if (rate <= 0) {
            p.sendMessage(Text.of("Ese item no se acepta en el banco."), false);
            return;
        }
        int have = countInInventory(p, id);
        if (have < amount) {
            p.sendMessage(Text.of("No tienes suficientes: " + itemId + " (" + have + "/" + amount + ")"), false);
            return;
        }
        removeFromInventory(p, id, amount);
        long chips = (long) rate * amount;
        addChips(p, chips);
        p.sendMessage(Text.of("Depositaste " + amount + " x " + itemId + " → +" + chips + " fichas."), false);
    }

    public static void withdrawCli(ServerPlayerEntity p, long chipsToSpend) {
        if (!trySpendChips(p, chipsToSpend)) {
            p.sendMessage(Text.of("No tienes fichas suficientes."), false);
            return;
        }
        Map<String, Integer> mats = withdrawMaterials(chipsToSpend);
        giveMaterials(p, mats);
        p.sendMessage(Text.of("Retiro completado: " + mats), false);
    }

    public static Map<String, Integer> withdrawMaterials(long chipsToSpend) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (String id : conversion.sortedPayoutOrder()) {
            int cost = conversion.chipsToMaterial(id);
            if (cost <= 0 || cost >= Integer.MAX_VALUE / 2) continue;
            int qty = (int) (chipsToSpend / cost);
            if (qty > 0) {
                out.put(id, qty);
                chipsToSpend -= (long) qty * cost;
            }
        }
        return out;
    }

    private static int countInInventory(ServerPlayerEntity p, Identifier id) {
        int count = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).equals(id)) count += s.getCount();
        }
        return count;
    }

    private static void removeFromInventory(ServerPlayerEntity p, Identifier id, int amount) {
        int remaining = amount;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).equals(id)) {
                int take = Math.min(remaining, s.getCount());
                s.decrement(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        p.playerScreenHandler.sendContentUpdates();
    }

    private static void giveMaterials(ServerPlayerEntity p, Map<String, Integer> mats) {
        mats.forEach((idStr, qty) -> {
            Identifier id = Identifier.tryParse(idStr);
            if (id != null && Registries.ITEM.containsId(id)) {
                var item = Registries.ITEM.get(id);
                int left = qty;
                while (left > 0) {
                    int stack = Math.min(left, item.getMaxCount());
                    left -= stack;
                    p.getInventory().insertStack(new ItemStack(item, stack));
                }
            }
        });
        p.playerScreenHandler.sendContentUpdates();
    }
}
