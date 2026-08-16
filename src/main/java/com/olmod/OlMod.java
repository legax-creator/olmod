package com.olmod;

import com.olmod.command.OlCommand;
import com.olmod.network.ModNetwork;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod("olmod")
public class OlMod {

    public OlMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
        ModNetwork.register();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        OlCommand.register(event.getDispatcher());
    }
}
