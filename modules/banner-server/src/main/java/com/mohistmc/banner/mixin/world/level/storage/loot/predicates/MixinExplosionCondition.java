package com.mohistmc.banner.mixin.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExplosionCondition.class)
public class MixinExplosionCondition {

    /**
     * @author wdog5
     * @reason bukkit
     */

    //TODO : banner fix me (server crash on startup)
    //Caused by: java.util.NoSuchElementException: minecraft:explosion_radius
    //at …class_10352.method_64967(ContextParameterMap#getOrThrow)
   /* @Overwrite
    public boolean test(LootContext lootContext) {
        Float float_ = (Float)lootContext.getParameter(LootContextParams.EXPLOSION_RADIUS);
        if (float_ != null) {
            RandomSource randomSource = lootContext.getRandom();
            float f = 1.0F / float_;
            // CraftBukkit - <= to < to allow for plugins to completely disable block drops from explosions
            return randomSource.nextFloat() < f;
        } else {
            return true;
        }
    }*/
}
