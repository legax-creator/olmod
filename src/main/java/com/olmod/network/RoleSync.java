package com.olmod.network;

import com.olmod.capability.IRoleData;
import com.olmod.capability.RoleCapabilityHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class RoleSync {

    /** Bir oyuncunun rolünü sunucudaki herkese (kendisi dahil) yayınlar. */
    public static void broadcast(ServerPlayer player) {
        IRoleData data = RoleCapabilityHandler.getRole(player);
        RoleSyncPacket packet = new RoleSyncPacket(player.getUUID(), data.getRole(), data.getOwnerUUID());
        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    /** Belirli bir oyuncunun rolünü, tek bir hedef oyuncuya gönderir (örn. yeni katılan/yeni gören biri). */
    public static void sendTo(ServerPlayer target, ServerPlayer subject) {
        IRoleData data = RoleCapabilityHandler.getRole(subject);
        RoleSyncPacket packet = new RoleSyncPacket(subject.getUUID(), data.getRole(), data.getOwnerUUID());
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), packet);
    }
}
