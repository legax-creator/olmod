package com.olmod.client;

import com.olmod.capability.PlayerRole;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client tarafında, sunucudan senkronize edilen oyuncu rollerini tutar.
 * Render kodu (RoleRenderHandler) bunu okuyarak hangi oyuncunun köpek/kedi/at
 * gibi çizileceğine karar verir.
 */
public class ClientRoleCache {

    private static final Map<UUID, PlayerRole> ROLES = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> OWNERS = new ConcurrentHashMap<>();

    public static void set(UUID playerId, PlayerRole role, UUID ownerId) {
        ROLES.put(playerId, role);
        if (ownerId != null) {
            OWNERS.put(playerId, ownerId);
        } else {
            OWNERS.remove(playerId);
        }
    }

    public static PlayerRole get(UUID playerId) {
        return ROLES.getOrDefault(playerId, PlayerRole.OYUNCU);
    }

    public static UUID getOwner(UUID playerId) {
        return OWNERS.get(playerId);
    }
}
