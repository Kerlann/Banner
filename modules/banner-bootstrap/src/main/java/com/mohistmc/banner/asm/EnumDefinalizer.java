package com.mohistmc.banner.asm;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.Set;

public class EnumDefinalizer implements Implementer {

    static final Set<String> ENUM = Set.of(
            "org/bukkit/Material",
            "org/bukkit/potion/PotionType",
            "org/bukkit/entity/EntityType",
            // "org/bukkit/block/Biome", // Banner Fix: Biome is now an interface, not an enum in 1.21.4+
            // "org/bukkit/Art", // Banner Fix: Art is now an interface, not an enum in 1.21.4+
            "org/bukkit/Statistic",
            "org/bukkit/inventory/CreativeCategory",
            "org/bukkit/entity/SpawnCategory",
            "org/bukkit/entity/EnderDragon$Phase",
            "org/bukkit/inventory/recipe/CookingBookCategory",
            // "org/bukkit/Fluid", // Banner Fix: Fluid is now an interface, not an enum in 1.21.4+
            // "org/bukkit/entity/Spellcaster$Spell", // Need to check this one
            "org/bukkit/entity/Pose" // Still an enum in 1.21.4
    );

    @Override
    public boolean processClass(ClassNode node) {
        if (ENUM.contains(node.name)) {
            var find = false;
            for (FieldNode field : node.fields) {
                if (Modifier.isStatic(field.access) && Modifier.isFinal(field.access) && (field.name.equals("ENUM$VALUES") || field.name.equals("$VALUES"))) {
                    field.access &= ~Opcodes.ACC_FINAL;
                    if (find) {
                        throw new IllegalStateException("Duplicate static final field found for " + node.name + ": " + field.name);
                    } else {
                        find = true;
                    }
                }
            }
            if (!find) {
                throw new IllegalStateException("No static final field found for " + node.name);
            }
            return true;
        }
        return false;
    }
}
