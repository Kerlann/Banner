package com.mohistmc.banner.mixin.server;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerAdvancementManager.class)
public abstract class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow
    public Map<ResourceLocation, AdvancementHolder> advancements;

    @Shadow private AdvancementTree tree;

    @Shadow protected abstract void validate(ResourceLocation resourceLocation, Advancement advancement);

    @Shadow @Final private HolderLookup.Provider registries;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "FIELD", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/ServerAdvancementManager;advancements:Ljava/util/Map;"))
    private void banner$buildMutable(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        this.advancements = new HashMap<>(this.advancements);
    }

    /**
     * @author wdog5
     * @reason bukkit - Fixed ClassCastException with advancement loading
     */
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);
        ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
        
        // Banner Fix: Cast the map to Object to handle mixed content types safely
        Map<ResourceLocation, Object> rawMap = (Map<ResourceLocation, Object>) (Map<?, ?>) map;
        
        rawMap.forEach((resourceLocation, element) -> {
            // Spigot start
            if (org.spigotmc.SpigotConfig.disabledAdvancements != null
                    && (org.spigotmc.SpigotConfig.disabledAdvancements.contains("*")
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.toString())
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.getNamespace()))) {
                return;
            }
            // Spigot end
            try {
                Advancement advancement;
                
                // Banner Fix: Handle different object types in the map
                if (element instanceof JsonElement) {
                    // Normal case: parse JsonElement to Advancement
                    JsonElement jsonElement = (JsonElement) element;
                    advancement = (Advancement) Advancement.CODEC.parse(registryOps, jsonElement).getOrThrow(JsonParseException::new);
                } else if (element.getClass().getName().equals("net.minecraft.class_161")) {
                    // Banner Fix: Object is already an Advancement (obfuscated class_161)
                    advancement = (Advancement) element;
                    LOGGER.debug("Banner: Found pre-parsed advancement for {}", resourceLocation);
                } else if (element instanceof Advancement) {
                    // Banner Fix: Object is already an Advancement (non-obfuscated)
                    advancement = (Advancement) element;
                    LOGGER.debug("Banner: Found pre-parsed advancement for {}", resourceLocation);
                } else {
                    // Unknown type - log and skip
                    LOGGER.error("Banner: Unexpected object type in advancement map for {}: {} (expected JsonElement or Advancement)", 
                               resourceLocation, element != null ? element.getClass().getName() : "null");
                    return; // Skip this advancement
                }
                
                this.validate(resourceLocation, advancement);
                builder.put(resourceLocation, new AdvancementHolder(resourceLocation, advancement));
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var6.getMessage());
                // Banner: Log more details for debugging
                if (var6 instanceof ClassCastException) {
                    LOGGER.error("Banner: ClassCastException details - element type: {}", 
                               element != null ? element.getClass().getName() : "null");
                }
            }
        });
        
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementTree = new AdvancementTree();
        advancementTree.addAll(this.advancements.values());

        for (AdvancementNode advancementNode : advancementTree.roots()) {
            if (advancementNode.holder().value().display().isPresent()) {
                TreeNodePosition.run(advancementNode);
            }
        }
        this.tree = advancementTree;
    }
}
