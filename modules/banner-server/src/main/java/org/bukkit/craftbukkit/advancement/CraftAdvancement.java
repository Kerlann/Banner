package org.bukkit.craftbukkit.advancement;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.advancements.AdvancementHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.advancement.AdvancementRequirements;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.Nullable;

public class CraftAdvancement implements org.bukkit.advancement.Advancement {

    private final AdvancementHolder handle;

    public CraftAdvancement(AdvancementHolder handle) {
        this.handle = handle;
    }

    public AdvancementHolder getHandle() {
        return this.handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(this.handle.id());
    }

    @Override
    public Collection<String> getCriteria() {
        return Collections.unmodifiableCollection(this.handle.value().criteria().keySet());
    }

    @Override
    public AdvancementRequirements getRequirements() {
        return new CraftAdvancementRequirements(this.handle.value().requirements());
    }

    @Override
    public @Nullable AdvancementDisplay getDisplay() { // May be called by plugins via Commodore
        return this.handle.value().display().map(CraftAdvancementDisplay::new).orElse(null);
    }
}
