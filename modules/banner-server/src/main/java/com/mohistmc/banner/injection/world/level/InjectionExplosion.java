package com.mohistmc.banner.injection.world.level;

public interface InjectionExplosion {

    default float bridge$getYield() { // Keep this one as it might be used by other parts of Arclight/Banner
        throw new IllegalStateException("Not implemented");
    }

    // New methods for yield
    default void banner$setYield(float yield) {
        throw new IllegalStateException("Not implemented");
    }

    default float banner$getYield() {
        throw new IllegalStateException("Not implemented");
    }

    // New methods for other shadowed fields
    default float banner$getRadius() {
        throw new IllegalStateException("Not implemented");
    }

    default net.minecraft.world.level.Explosion.BlockInteraction banner$getBlockInteraction() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$wasCanceled() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setWasCanceled(boolean wasCanceled) {
        throw new IllegalStateException("Not implemented");
    }
}
