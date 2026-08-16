package com.olmod.event;

import com.olmod.capability.IRoleData;
import com.olmod.capability.PlayerRole;
import com.olmod.capability.RoleCapabilityHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * Sahip, shift+sağ tık ile doğru yemle beslediğinde can+açlık dolar ve
 * "aşk modu" (çiftleştirmeye hazır) süresi başlar — vanilla hayvanlardaki gibi.
 * - Köpek: biftek (çiğ/pişmiş)
 * - Kedi: çiğ balık (morina/somon)
 * - At: buğday, elma, altın havuç, altın elma, şeker, saman balyası
 */
@Mod.EventBusSubscriber(modid = "olmod")
public class RoleFeedingHandler {

    private static final Set<Item> DOG_FOOD = Set.of(Items.BEEF, Items.COOKED_BEEF);
    private static final Set<Item> CAT_FOOD = Set.of(Items.COD, Items.SALMON);
    private static final Set<Item> HORSE_FOOD = Set.of(
            Items.WHEAT, Items.SUGAR, Items.APPLE, Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.HAY_BLOCK);

    private static final int LOVE_TICKS = 600; // vanilla hayvanlardaki aşk modu süresi (~30 sn)
    private static final float HEAL_AMOUNT = 6.0F;

    @SubscribeEvent
    public static void onFeed(PlayerInteractEvent.EntityInteract event) {
        if (!event.getEntity().isShiftKeyDown()) return;
        if (!(event.getTarget() instanceof Player target)) return;

        IRoleData data = RoleCapabilityHandler.getRole(target);
        PlayerRole role = data.getRole();
        if (!role.isPet()) return;

        Player owner = event.getEntity();
        if (data.getOwnerUUID() == null || !data.getOwnerUUID().equals(owner.getUUID())) {
            return; // sadece sahip besleyebilir
        }

        ItemStack held = event.getItemStack();
        Set<Item> validFood = switch (role) {
            case KOPEK -> DOG_FOOD;
            case KEDI -> CAT_FOOD;
            case AT -> HORSE_FOOD;
            default -> Set.of();
        };

        if (held.isEmpty() && (role == PlayerRole.KOPEK || role == PlayerRole.KEDI)) {
            data.setSitting(!data.isSitting());
            event.setCanceled(true);
            return;
        }

        if (!validFood.contains(held.getItem())) return;

        target.heal(HEAL_AMOUNT);
        target.getFoodData().setFoodLevel(20);
        data.setLoveTicks(LOVE_TICKS);

        if (!owner.getAbilities().instabuild) {
            held.shrink(1);
        }
        event.setCanceled(true);
    }
}
