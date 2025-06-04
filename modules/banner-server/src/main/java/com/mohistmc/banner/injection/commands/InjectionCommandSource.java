package com.mohistmc.banner.injection.commands;


import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

public interface InjectionCommandSource {

    CommandSender banner$getBukkitSender(CommandSourceStack wrapper);

    CommandSender getBukkitSender(CommandSourceStack stack);
}
