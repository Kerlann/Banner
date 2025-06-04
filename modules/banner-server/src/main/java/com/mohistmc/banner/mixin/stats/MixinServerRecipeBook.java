package com.mohistmc.banner.mixin.stats;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
@Mixin(ServerRecipeBook.class)
public abstract class MixinServerRecipeBook {

    @Inject(
            method = "addRecipes",
            at = @At(
                    value = "INVOKE",
                    target = // même cible qu'avant
                            "Lnet/minecraft/stats/ServerRecipeBook;add(Lnet/minecraft/resources/ResourceKey;)V"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void banner$callRecipeEvent(
            Collection<RecipeHolder<?>> recipes,
            ServerPlayer player,
            CallbackInfoReturnable<Integer> cir,
            @Local ResourceKey<Recipe<?>> recipeKey        // ①
    ) {
        // ② on passe ResourceLocation aux API Bukkit :
        if (!CraftEventFactory.handlePlayerRecipeListUpdateEvent(
                player, recipeKey.location())) {           // ResourceKey#location()
            cir.cancel();                                   // empêche l'ajout dans le livre
        }
    }
}