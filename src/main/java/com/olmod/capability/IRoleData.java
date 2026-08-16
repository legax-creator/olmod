package com.olmod.capability;

import java.util.UUID;

/**
 * Bir oyuncunun rolünü (oyuncu/köpek/kedi/at), varsa sahibinin UUID'sini,
 * oturma durumunu ve çiftleştirme ("aşk modu") durumunu tutar.
 * Sahip her zaman OYUNCU rolündeki kişidir.
 */
public interface IRoleData {
    PlayerRole getRole();
    void setRole(PlayerRole role);

    UUID getOwnerUUID();
    void setOwnerUUID(UUID uuid);

    boolean isSitting();
    void setSitting(boolean sitting);

    /** Doğru yemle beslendikten sonra kalan "aşk modu" tick sayısı (vanilla hayvanlardaki gibi). */
    int getLoveTicks();
    void setLoveTicks(int ticks);
}
