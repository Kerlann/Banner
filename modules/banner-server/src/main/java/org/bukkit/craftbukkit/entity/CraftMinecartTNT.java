package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.mohistmc.banner.injection.world.entity.vehicle.BridgeMinecartTNT;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.minecart.ExplosiveMinecart;

public final class CraftMinecartTNT extends CraftMinecart implements ExplosiveMinecart {
    CraftMinecartTNT(CraftServer server, MinecartTNT entity) {
        super(server, entity);
    }

    @Override
    public float getYield() {
        return ((BridgeMinecartTNT) this.getHandle()).banner$getExplosionPowerBase();
    }

    @Override
    public boolean isIncendiary() {
        return this.getHandle().bridge$isIncendiary();
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        this.getHandle().isInPowderSnow = isIncendiary;
    }

    @Override
    public void setYield(float yield) {
        ((BridgeMinecartTNT) this.getHandle()).banner$setExplosionPowerBase(yield);
    }

    @Override
    public float getExplosionSpeedFactor() {
        return ((BridgeMinecartTNT) this.getHandle()).banner$getExplosionSpeedFactor();
    }

    @Override
    public void setExplosionSpeedFactor(float factor) {
        ((BridgeMinecartTNT) this.getHandle()).banner$setExplosionSpeedFactor(factor);
    }

    @Override
    public void setFuseTicks(int ticks) {
        this.getHandle().fuse = ticks;
    }

    @Override
    public int getFuseTicks() {
        return this.getHandle().getFuse();
    }

    @Override
    public void ignite() {
        this.getHandle().primeFuse();
    }

    @Override
    public boolean isIgnited() {
        return this.getHandle().isPrimed();
    }

    @Override
    public void explode() {
        this.getHandle().explode(this.getHandle().getDeltaMovement().horizontalDistanceSqr());
    }

    @Override
    public void explode(double power) {
        Preconditions.checkArgument(0 <= power && power <= 5, "Power must be in range [0, 5] (got %s)", power);

        this.getHandle().explode(power);
    }

    @Override
    public MinecartTNT getHandle() {
        return (MinecartTNT) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftMinecartTNT";
    }
}
