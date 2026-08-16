package com.olmod.client;

import com.olmod.capability.PlayerRole;
import com.olmod.network.HorseJumpPacket;
import com.olmod.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "olmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientInputHandler {

    private static boolean lastSent = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean ridingAt = player.getVehicle() instanceof Player vehicle
                && ClientRoleCache.get(vehicle.getUUID()) == PlayerRole.AT;
        boolean jumping = ridingAt && mc.options.keyJump.isDown();

        if (jumping != lastSent) {
            lastSent = jumping;
            ModNetwork.CHANNEL.sendToServer(new HorseJumpPacket(jumping));
        }
    }
}
