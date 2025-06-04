package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Fox.class)
public abstract class MixinFox extends Animal {

    protected MixinFox(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

/*
    @Redirect(method = "pickUpItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;canHoldItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean banner$pickupEvent(Fox foxEntity, ItemStack stack, ItemEntity itemEntity) {
        return CraftEventFactory.callEntityPickupItemEvent((Fox) (Object) this, itemEntity, stack.getCount() - 1, !this.canHoldItem(stack)).isCancelled();
    }*/

    @Redirect(
            method = "pickUpItem(Lnet/minecraft/server/level/ServerLevel;" +
                    "Lnet/minecraft/world/entity/item/ItemEntity;)V",
            at     = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Fox;" +
                            "canHoldItem(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean banner$callPickupEvent(Fox fox, ItemStack stack,
                                           ServerLevel level, ItemEntity itemEntity) {

        boolean vanilla = fox.canHoldItem(stack);

        /*  Appel de l’event Bukkit ; s’il est annulé on renvoie false. */
        boolean cancelled = CraftEventFactory
                .callEntityPickupItemEvent(
                        (Fox) (Object) this,
                        itemEntity,
                        stack.getCount() - 1,
                        !vanilla)          // « willAddToInventory » du côté CB
                .isCancelled();

        return !cancelled && vanilla;
    }

}
