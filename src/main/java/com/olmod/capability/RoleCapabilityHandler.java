package com.olmod.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "olmod")
public class RoleCapabilityHandler {

    public static final ResourceLocation ROLE_DATA_ID = new ResourceLocation("olmod", "role_data");

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IRoleData.class);
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ROLE_DATA_ID, new RoleDataProvider());
        }
    }

    // Ölünce/boyut değiştirince (respawn, dimension değiştirme) rolü koru
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(RoleDataProvider.ROLE_DATA).ifPresent(oldData -> {
            event.getEntity().getCapability(RoleDataProvider.ROLE_DATA).ifPresent(newData -> {
                newData.setRole(oldData.getRole());
                newData.setOwnerUUID(oldData.getOwnerUUID());
                newData.setSitting(oldData.isSitting());
                newData.setLoveTicks(oldData.getLoveTicks());
            });
        });
    }

    // Bir oyuncu dünyaya girdiğinde (login/respawn/dünya değişimi): kendi rolünü herkese,
    // o an sunucuda olan herkesin rolünü de kendisine gönder ki render doğru başlasın.
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer joining)) {
            return;
        }
        com.olmod.network.RoleSync.broadcast(joining);
        for (ServerPlayer other : joining.getServer().getPlayerList().getPlayers()) {
            if (other != joining) {
                com.olmod.network.RoleSync.sendTo(joining, other);
            }
        }
    }

    public static IRoleData getRole(Player player) {
        return player.getCapability(RoleDataProvider.ROLE_DATA).orElseThrow(
                () -> new IllegalStateException("Oyuncuda RoleData capability yok: " + player.getName().getString())
        );
    }
}
