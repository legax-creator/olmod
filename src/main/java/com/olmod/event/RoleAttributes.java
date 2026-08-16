package com.olmod.event;

import com.olmod.capability.PlayerRole;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Rol değiştiğinde oyuncunun can/hız değerlerini ilgili vanilla hayvanla
 * eşitler. Değerler vanilla Wolf/Cat/Horse temel attribute'larına yakın
 * (at için sabit ortalama bir değer kullanılıyor; vanilla atlarda bu
 * rastgele bir aralıktan seçilir).
 */
public class RoleAttributes {

    public static void apply(Player player, PlayerRole role) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (maxHealth == null || speed == null) {
            return;
        }

        switch (role) {
            case KOPEK -> {
                maxHealth.setBaseValue(8.0D);   // vanilla köpek (evcil kurt) canı
                speed.setBaseValue(0.30D);      // vanilla kurt hızı
            }
            case KEDI -> {
                maxHealth.setBaseValue(10.0D);  // vanilla kedi canı
                speed.setBaseValue(0.30D);      // vanilla kedi hızı
            }
            case AT -> {
                maxHealth.setBaseValue(22.5D);  // vanilla at canı ortalama (15-30 arası rastgele)
                speed.setBaseValue(0.225D);     // vanilla at hızı ortalama
            }
            case OYUNCU -> {
                maxHealth.setBaseValue(20.0D);  // normal oyuncu canı
                speed.setBaseValue(0.10D);      // normal oyuncu hızı
            }
        }

        // Değişen max cana göre canı taşırma/eksik kalmama için sınırla.
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
