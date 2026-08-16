package com.olmod.event;

import com.olmod.capability.IRoleData;
import com.olmod.capability.PlayerRole;
import com.olmod.capability.RoleCapabilityHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Köpek/kedi/at rolündeki oyuncuların her tick'te işlenen davranışları:
 * - hitbox'ı role uygun boyuta getirme (yaklaşık; Mixin kullanmadan setBoundingBox ile)
 * - manuel zıplama engeli + önündeki engeli otomatik atlama (mob AI'sının yaklaşık taklidi)
 * - oturma: sahip tarafından dondurulunca yerinde kalır
 * - köpek/kedi: sahipten 12 blok uzaklaşınca yanına ışınlanma (at hariç)
 * - aşk modu (besleme sonrası) ile aynı role sahip iki oyuncu yakınlaşınca "çiftleşme":
 *   GERÇEK BİR YENİ OYUNCU/BEBEK OLUŞTURULAMAZ (bu bir mod ile bir oyuncuyu klonlayamayız),
 *   bu yüzden kalp parçacığı + mesajla sembolik olarak gösteriliyor.
 * - at: sadece sahibi biniyorsa, sahibinin WASD + zıplama girdisiyle sürülür (yaklaşık; bir
 *   tick gecikmeli olabilir çünkü Player'ın hareket kodunu tamamen değiştiremiyoruz)
 */
@Mod.EventBusSubscriber(modid = "olmod")
public class RoleMovementHandler {

    private static final double TELEPORT_DISTANCE = 12.0D;
    private static final Set<UUID> AUTO_JUMPING = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        IRoleData data = RoleCapabilityHandler.getRole(player);
        PlayerRole role = data.getRole();
        if (!role.isPet()) return;

        applyHitbox(player, role);
        tickLoveMode(player, data);

        if (data.isSitting()) {
            player.setDeltaMovement(0, player.getDeltaMovement().y < 0 ? player.getDeltaMovement().y : 0, 0);
            return;
        }

        if (role == PlayerRole.AT) {
            tickHorse(player, data);
        } else {
            tickJump(player);
            tickTeleportToOwner(player, data);
        }
    }

    // --- Hitbox (yaklaşık; gerçek fiziksel boyut değişimi Mixin olmadan bu şekilde uygulanabilir) ---
    private static void applyHitbox(ServerPlayer player, PlayerRole role) {
        float width;
        float height;
        switch (role) {
            case KOPEK -> { width = 0.6F; height = 0.85F; }
            case KEDI -> { width = 0.6F; height = 0.5F; }
            case AT -> { width = 1.3964844F; height = 1.6F; }
            default -> { return; }
        }
        Vec3 pos = player.position();
        double halfW = width / 2.0D;
        AABB box = new AABB(pos.x - halfW, pos.y, pos.z - halfW, pos.x + halfW, pos.y + height, pos.z + halfW);
        player.setBoundingBox(box);
    }

    // --- Manuel zıplama engeli + önündeki engeli otomatik atlama ---
    private static void tickJump(ServerPlayer player) {
        boolean weTriggeredJump = AUTO_JUMPING.remove(player.getUUID());

        if (player.jumping && player.onGround() && !weTriggeredJump) {
            // Manuel zıplamayı iptal et
            Vec3 v = player.getDeltaMovement();
            if (v.y > 0) {
                player.setDeltaMovement(v.x, 0, v.z);
            }
        }

        // Önünde alçak bir engel varsa (mob'ların "step assist" / auto-jump'ı gibi) otomatik zıpla
        if (player.horizontalCollision && player.onGround() && player.getDeltaMovement().y <= 0) {
            AUTO_JUMPING.add(player.getUUID());
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, 0.42D, v.z);
        }
    }

    // --- Sahipten uzaklaşınca ışınlanma (köpek/kedi) ---
    private static void tickTeleportToOwner(ServerPlayer player, IRoleData data) {
        UUID ownerId = data.getOwnerUUID();
        if (ownerId == null) return;
        ServerPlayer owner = player.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || owner.level() != player.level()) return;

        if (player.distanceTo(owner) > TELEPORT_DISTANCE) {
            player.teleportTo((ServerLevel) owner.level(),
                    owner.getX() + 1, owner.getY(), owner.getZ() + 1,
                    player.getYRot(), player.getXRot());
        }
    }

    // --- Aşk modu sayacı + yakındaki uygun eşle sembolik "çiftleşme" ---
    private static void tickLoveMode(ServerPlayer player, IRoleData data) {
        if (data.getLoveTicks() <= 0) return;
        data.setLoveTicks(data.getLoveTicks() - 1);
        if (data.getLoveTicks() <= 0) return;

        PlayerRole role = data.getRole();
        List<ServerPlayer> nearby = player.level().getEntitiesOfClass(ServerPlayer.class,
                player.getBoundingBox().inflate(4.0D),
                p -> p != player);

        for (ServerPlayer other : nearby) {
            IRoleData otherData = RoleCapabilityHandler.getRole(other);
            if (otherData.getRole() == role && otherData.getLoveTicks() > 0) {
                // İki taraf da aşk modundaysa "çiftleşme" tetiklenir.
                data.setLoveTicks(0);
                otherData.setLoveTicks(0);
                Vec3 mid = player.position().add(other.position()).scale(0.5D);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, mid.x, mid.y + 1, mid.z, 8, 0.3, 0.3, 0.3, 0.0);
                }
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Çiftleşme gerçekleşti! (Not: teknik olarak yeni bir oyuncu oluşturulamıyor, bu sembolik bir gösterim)"));
                return;
            }
        }
    }

    // --- At sürme: sadece sahibi biniyorsa, sahibinin girdisiyle hareket ---
    private static void tickHorse(ServerPlayer horse, IRoleData data) {
        if (horse.getPassengers().isEmpty()) {
            tickJump(horse); // binicisi yoksa kendi kendine otomatik zıplama davranışı sürsün
            return;
        }

        Player rider = horse.getPassengers().get(0) instanceof Player p ? p : null;
        UUID ownerId = data.getOwnerUUID();
        if (rider == null || ownerId == null || !rider.getUUID().equals(ownerId)) {
            return; // sadece sahibi sürebilir
        }

        // Atın yönünü binicinin baktığı yöne göre ayarla (görsel + hareket yönü için)
        horse.setYRot(rider.getYRot());
        horse.setYBodyRot(rider.getYRot());
        horse.setYHeadRot(rider.getYRot());

        float forwardInput = rider.zza;   // ileri/geri (public alan)
        float strafeInput = rider.xxa;    // sağ/sol (public alan)

        if (forwardInput != 0 || strafeInput != 0) {
            double speed = horse.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * 4.5D;
            Vec3 movementVec = new Vec3(strafeInput, 0, forwardInput).normalize().scale(speed);
            double yaw = Math.toRadians(horse.getYRot());
            double sin = Math.sin(-yaw);
            double cos = Math.cos(yaw);
            double dx = movementVec.x * cos - movementVec.z * sin;
            double dz = movementVec.z * cos + movementVec.x * sin;
            Vec3 current = horse.getDeltaMovement();
            horse.setDeltaMovement(dx, current.y, dz);
        } else {
            Vec3 current = horse.getDeltaMovement();
            horse.setDeltaMovement(current.x * 0.6D, current.y, current.z * 0.6D); // sürtünme ile yavaşça dur
        }

        // Vanilla at zıplama yüksekliğine yakın bir zıplama (rider zıplama tuşuna basarsa)
        if (rider.jumping && horse.onGround()) {
            Vec3 current = horse.getDeltaMovement();
            horse.setDeltaMovement(current.x, 0.6D, current.z); // ~vanilla at zıplama yüksekliğine yakın
        }
    }
}
