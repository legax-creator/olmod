package com.olmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.olmod.capability.PlayerRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Köpek/kedi/at rolündeki oyuncuların ekranda GERÇEKTEN o hayvan gibi görünmesini sağlar.
 *
 * Yöntem: vanilla oyuncu render'ını iptal edip (RenderPlayerEvent.Pre -> cancel), yerine
 * vanilla WolfModel/CatModel/HorseModel'i elle çiziyoruz. Bu modellerin setupAnim() metodu
 * spesifik olarak Wolf/Cat/Horse tipini beklediği için, dünyaya asla eklenmeyen, sadece
 * animasyon verisi taşımak amacıyla oluşturulmuş "hayalet" (proxy) bir Wolf/Cat/Horse
 * kullanıyoruz; oyuncunun yürüme animasyonunu (walkAnimation), bakış açısını ve (kedi/köpek
 * için) oturma durumunu bu proxy'ye aktarıp vanilla animasyon koduyla aynı bacak/kuyruk
 * hareketini elde ediyoruz. Bu, tam oyuncu iskeletiyle bire bir eşleşmediği için mükemmel
 * olmayabilir ama gerçek vanilla animasyon mantığını kullandığından oldukça sağlam bir sonuç verir.
 */
@Mod.EventBusSubscriber(modid = "olmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoleRenderHandler {

    private static final ResourceLocation WOLF_TEXTURE =
            new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation CAT_TEXTURE =
            new ResourceLocation("textures/entity/cat/tabby.png");
    private static final ResourceLocation HORSE_TEXTURE =
            new ResourceLocation("textures/entity/horse/horse_brown.png");

    private static WolfModel<Wolf> wolfModel;
    private static CatModel<Cat> catModel;
    private static HorseModel<Horse> horseModel;

    private static Wolf proxyWolf;
    private static Cat proxyCat;
    private static Horse proxyHorse;

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Modeller ilk render anında da bake edilebilir; burada dokunmuyoruz,
        // gerçek bake işlemi ihtiyaç anında (lazy) aşağıda yapılıyor.
    }

    private static void ensureModels() {
        if (wolfModel != null) return;
        var models = Minecraft.getInstance().getEntityModels();
        ModelPart wolfPart = models.bakeLayer(ModelLayers.WOLF);
        ModelPart catPart = models.bakeLayer(ModelLayers.CAT);
        ModelPart horsePart = models.bakeLayer(ModelLayers.HORSE);
        wolfModel = new WolfModel<>(wolfPart);
        catModel = new CatModel<>(catPart);
        horseModel = new HorseModel<>(horsePart);
    }

    private static void ensureProxies() {
        if (proxyWolf != null) return;
        var level = Minecraft.getInstance().level;
        proxyWolf = new Wolf(EntityType.WOLF, level);
        proxyCat = new Cat(EntityType.CAT, level);
        proxyHorse = new Horse(EntityType.HORSE, level);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        PlayerRole role = ClientRoleCache.get(player.getUUID());
        if (role == PlayerRole.OYUNCU) return; // normal oyuncu -> vanilla render devam etsin

        event.setCanceled(true);
        ensureModels();
        ensureProxies();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float partialTick = event.getPartialTick();

        // Oyuncunun yürüme animasyon durumunu (vanilla'nın kendi hesapladığı) proxy'ye aktar.
        float limbSwing = 0f;
        float limbSwingAmount = player.walkAnimation.speed(partialTick);
        float ageInTicks = player.tickCount + partialTick;
        float netHeadYaw = player.getViewYRot(partialTick) - player.yBodyRotO
                + (player.yBodyRot - player.yBodyRotO) * partialTick;
        float headPitch = player.getViewXRot(partialTick);

        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0); // ayakları yere yakın hizala (yaklaşık)

        switch (role) {
            case KOPEK -> {
                proxyWolf.setInSittingPose(sittingRoleCheck(player));
                wolfModel.setupAnim(proxyWolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(WOLF_TEXTURE));
                wolfModel.renderToBuffer(poseStack, consumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            }
            case KEDI -> {
                catModel.setupAnim(proxyCat, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(CAT_TEXTURE));
                catModel.renderToBuffer(poseStack, consumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            }
            case AT -> {
                horseModel.setupAnim(proxyHorse, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(HORSE_TEXTURE));
                horseModel.renderToBuffer(poseStack, consumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            }
            default -> {}
        }

        poseStack.popPose();
    }

    // Sunucudan sitting bilgisi de senkronize edilmediği için (şu anki paket sadece rol+sahip
    // taşıyor), burada basitçe false dönüyoruz. Oturma görselini tam yansıtmak istersen
    // RoleSyncPacket'e bir "sitting" alanı eklemek gerekir — TODO.
    private static boolean sittingRoleCheck(AbstractClientPlayer player) {
        return false;
    }
}
