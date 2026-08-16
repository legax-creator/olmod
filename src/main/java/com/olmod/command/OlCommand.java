package com.olmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.olmod.capability.IRoleData;
import com.olmod.capability.PlayerRole;
import com.olmod.capability.RoleCapabilityHandler;
import com.olmod.event.RoleAttributes;
import com.olmod.network.RoleSync;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OlCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ol")
                        .requires(src -> src.getEntity() instanceof ServerPlayer)
                        .then(Commands.argument("rol", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("oyuncu");
                                    builder.suggest("köpek");
                                    builder.suggest("kedi");
                                    builder.suggest("at");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String arg = StringArgumentType.getString(ctx, "rol");
                                    PlayerRole role = PlayerRole.fromArg(arg);
                                    if (role == null) {
                                        ctx.getSource().sendFailure(
                                                Component.literal("Geçersiz rol. Kullan: oyuncu, köpek, kedi, at"));
                                        return 0;
                                    }

                                    ServerPlayer player = (ServerPlayer) ctx.getSource().getEntity();
                                    IRoleData data = RoleCapabilityHandler.getRole(player);
                                    data.setRole(role);

                                    // Oyuncu rolüne dönerse sahiplik/oturma/aşk modu sıfırlanır
                                    if (role == PlayerRole.OYUNCU) {
                                        data.setOwnerUUID(null);
                                        data.setSitting(false);
                                        data.setLoveTicks(0);
                                    }

                                    RoleAttributes.apply(player, role);
                                    RoleSync.broadcast(player);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Rolün artık: " + role.name().toLowerCase()),
                                            true);
                                    return 1;
                                })));
    }
}
