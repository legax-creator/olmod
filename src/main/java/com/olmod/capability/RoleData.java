package com.olmod.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class RoleData implements IRoleData {

    private PlayerRole role = PlayerRole.OYUNCU;
    private UUID ownerUUID = null;
    private boolean sitting = false;
    private int loveTicks = 0;

    @Override
    public PlayerRole getRole() {
        return role;
    }

    @Override
    public void setRole(PlayerRole role) {
        this.role = role;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public boolean isSitting() {
        return sitting;
    }

    @Override
    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    @Override
    public int getLoveTicks() {
        return loveTicks;
    }

    @Override
    public void setLoveTicks(int ticks) {
        this.loveTicks = ticks;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("role", role.name());
        tag.putBoolean("sitting", sitting);
        tag.putInt("loveTicks", loveTicks);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("role")) {
            try {
                role = PlayerRole.valueOf(tag.getString("role"));
            } catch (IllegalArgumentException e) {
                role = PlayerRole.OYUNCU;
            }
        }
        sitting = tag.getBoolean("sitting");
        loveTicks = tag.getInt("loveTicks");
        if (tag.hasUUID("owner")) {
            ownerUUID = tag.getUUID("owner");
        } else {
            ownerUUID = null;
        }
    }
}
