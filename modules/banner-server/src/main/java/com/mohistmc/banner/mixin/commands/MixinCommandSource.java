package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
public interface MixinCommandSource extends InjectionCommandSource {

    /**
     * Implémentation générique héritée par *toutes* les instances de CommandSource
     * (y compris les classes anonymes générées par Mojang).
     */
    @Override                     // ← satisfait l’interface InjectionCommandSource
    default CommandSender banner$getBukkitSender(CommandSourceStack stack) {

        if (stack != null && stack.getEntity() instanceof ServerPlayer player) {
            // Source = un joueur → on renvoie le Bukkit Player associé
            return player.getBukkitEntity();
        }

        // Source = console (ou inconnu) → on renvoie la console Bukkit
        return Bukkit.getConsoleSender();
    }

    /**
     * Votre méthode « bridge » historique conserve la même logique
     * et délègue tout simplement.
     */
    @Override
    default CommandSender getBukkitSender(CommandSourceStack stack) {
        return banner$getBukkitSender(stack);
    }
}