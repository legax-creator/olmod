package com.olmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("olmod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++, RoleSyncPacket.class,
                RoleSyncPacket::encode, RoleSyncPacket::decode, RoleSyncPacket::handle);
        CHANNEL.registerMessage(id++, HorseJumpPacket.class,
                HorseJumpPacket::encode, HorseJumpPacket::decode, HorseJumpPacket::handle);
    }
}
