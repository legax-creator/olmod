package com.olmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * "jumping" alanı LivingEntity'de protected olduğu için, at rolündeki bir oyuncuya
 * binen kişinin zıplama tuşuna basıp basmadığını sunucuya bu paketle bildiriyoruz.
 * Client, her tick riding+jump durumunu bu paketle gönderir; sunucu son bilinen
 * durumu RoleMovementHandler.JUMP_HELD haritasında tutar.
 */
public class HorseJumpPacket {

    private final boolean jumping;

    public HorseJumpPacket(boolean jumping) {
        this.jumping = jumping;
    }

    public static void encode(HorseJumpPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.jumping);
    }

    public static HorseJumpPacket decode(FriendlyByteBuf buf) {
        return new HorseJumpPacket(buf.readBoolean());
    }

    public static void handle(HorseJumpPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                com.olmod.event.RoleMovementHandler.setJumpHeld(ctx.getSender().getUUID(), msg.jumping);
            }
        });
        ctx.setPacketHandled(true);
    }
}
