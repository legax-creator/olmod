package com.olmod.network;

import com.olmod.capability.PlayerRole;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Sunucudan client'lara: "şu UUID'li oyuncunun rolü şu" bilgisini gönderir.
 * Client bunu görsel render (kedi/köpek/at modeli) için kullanır.
 */
public class RoleSyncPacket {

    private final UUID playerId;
    private final PlayerRole role;
    private final UUID ownerId; // null olabilir

    public RoleSyncPacket(UUID playerId, PlayerRole role, UUID ownerId) {
        this.playerId = playerId;
        this.role = role;
        this.ownerId = ownerId;
    }

    public static void encode(RoleSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeEnum(msg.role);
        buf.writeBoolean(msg.ownerId != null);
        if (msg.ownerId != null) {
            buf.writeUUID(msg.ownerId);
        }
    }

    public static RoleSyncPacket decode(FriendlyByteBuf buf) {
        UUID playerId = buf.readUUID();
        PlayerRole role = buf.readEnum(PlayerRole.class);
        UUID owner = buf.readBoolean() ? buf.readUUID() : null;
        return new RoleSyncPacket(playerId, role, owner);
    }

    public static void handle(RoleSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.olmod.client.ClientRoleCache.set(msg.playerId, msg.role, msg.ownerId))
        );
        ctx.setPacketHandled(true);
    }
}
