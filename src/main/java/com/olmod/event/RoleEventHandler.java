package com.olmod.event;

import com.olmod.capability.IRoleData;
import com.olmod.capability.PlayerRole;
import com.olmod.capability.RoleCapabilityHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Köpek, kedi ve at rolündeki oyuncuların yapabileceklerini kısıtlar:
 * - üçü de blok kıramaz, blokla etkileşime giremez (kapı vb. açamaz;
 *   basınç plakaları etkileşim olmadığı için zaten etkilenmez)
 * - üçünün de envanteri yok
 * - kedi ve at kimseye saldıramaz / hasar veremez
 * - at: herkes binebilir, ama sadece sahibi ("/ol oyuncu" olan kişi) sürebilir (bkz. RoleMovementHandler)
 *
 * Besleme/çiftleştirme RoleFeedingHandler'da, hareket/zıplama/oturma/ışınlanma RoleMovementHandler'da.
 */
@Mod.EventBusSubscriber(modid = "olmod")
public class RoleEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        IRoleData data = RoleCapabilityHandler.getRole(player);
        if (data.getRole().isPet()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        IRoleData data = RoleCapabilityHandler.getRole(player);
        if (data.getRole().isHarmless()) {
            event.setCanceled(true);
        }
    }

    // Kapı, sandık, kaldıraç vb. bloklarla etkileşimi engelle.
    // Basınç plakaları oyuncunun üzerine basmasıyla tetiklenir (bu event'e girmez), o yüzden etkilenmez.
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        IRoleData data = RoleCapabilityHandler.getRole(player);
        if (data.getRole().isPet()) {
            event.setCanceled(true);
            event.setUseBlock(Event.Result.DENY);
        }
    }

    // Envanter açmayı engelle (sunucu tarafı yedek; client tarafında da E tuşu ayrıca engellenmeli).
    @SubscribeEvent
    public static void onOpenInventory(PlayerContainerEvent.Open event) {
        IRoleData data = RoleCapabilityHandler.getRole(event.getEntity());
        if (data.getRole().isPet()) {
            event.getEntity().closeContainer();
        }
    }

    // At rolündeki oyuncuya binme: sneak yapmadan sağ tıklayan (kim olursa olsun) ata biner.
    // Sürme yetkisi (input kontrolü) sadece sahipte — bkz. RoleMovementHandler.
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().isShiftKeyDown()) {
            return; // shift+sağ tık besleme/oturtma için ayrılmış (RoleFeedingHandler / RoleMovementHandler)
        }
        if (!(event.getTarget() instanceof Player targetPlayer)) {
            return;
        }
        IRoleData targetData = RoleCapabilityHandler.getRole(targetPlayer);
        if (targetData.getRole() != PlayerRole.AT) {
            return;
        }

        Player clicker = event.getEntity();
        if (clicker.getVehicle() == null && targetPlayer.getPassengers().isEmpty()) {
            clicker.startRiding(targetPlayer);
            event.setCanceled(true);
        }
    }
}
