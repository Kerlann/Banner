package com.mohistmc.banner.bukkit.remapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CraftBukkitCompatibilityWrapper
 * 
 * Handles compatibility between versioned CraftBukkit classes (like v1_21_R3) 
 * and non-versioned classes used by Banner.
 * 
 * @author Banner Team
 */
public class CraftBukkitCompatibilityWrapper {
    
    private static final Logger LOGGER = LogManager.getLogger("Banner-Remapping");
    private static final Pattern VERSION_PATTERN = Pattern.compile("v\\d+_\\d+_R\\d+");
    private static final Map<String, String> VERSION_MAPPINGS = new HashMap<>();
    
    static {
        // Initialize version mappings
        initializeVersionMappings();
        LOGGER.debug("Initialized CraftBukkit compatibility mappings with {} entries", VERSION_MAPPINGS.size());
    }
    
    private static void initializeVersionMappings() {
        // Common CraftBukkit classes that plugins try to access
        String[] commonClasses = {
            "CraftWorld", "CraftServer", "CraftPlayer", "CraftItemStack",
            "CraftInventory", "CraftEntity", "CraftBlock", "CraftChunk",
            "CraftMagicNumbers", "CraftScheduler", "CraftPluginManager",
            "CraftScoreboard", "CraftTeam", "CraftObjective"
        };
        
        String[] entityClasses = {
            "CraftPlayer", "CraftEntity", "CraftLivingEntity", "CraftHumanEntity",
            "CraftAnimal", "CraftMonster", "CraftCreature", "CraftVillager",
            "CraftZombie", "CraftSkeleton", "CraftCreeper", "CraftEnderman"
        };
        
        String[] inventoryClasses = {
            "CraftInventory", "CraftItemStack", "CraftInventoryView",
            "CraftInventoryPlayer", "CraftInventoryChest", "CraftInventoryFurnace",
            "CraftInventoryBrewingStand", "CraftInventoryEnchanting"
        };
        
        String[] blockClasses = {
            "CraftBlock", "CraftBlockState", "CraftChest", "CraftFurnace",
            "CraftSign", "CraftBeacon", "CraftBrewingStand", "CraftDispenser"
        };
        
        // Map main classes
        for (String className : commonClasses) {
            addMapping("org.bukkit.craftbukkit.v1_21_R3." + className, 
                      "org.bukkit.craftbukkit." + className);
        }
        
        // Map entity classes
        for (String className : entityClasses) {
            addMapping("org.bukkit.craftbukkit.v1_21_R3.entity." + className, 
                      "org.bukkit.craftbukkit.entity." + className);
        }
        
        // Map inventory classes
        for (String className : inventoryClasses) {
            addMapping("org.bukkit.craftbukkit.v1_21_R3.inventory." + className, 
                      "org.bukkit.craftbukkit.inventory." + className);
        }
        
        // Map block classes
        for (String className : blockClasses) {
            addMapping("org.bukkit.craftbukkit.v1_21_R3.block." + className, 
                      "org.bukkit.craftbukkit.block." + className);
        }
        
        // Add other version patterns that might be encountered
        String[] versions = {"v1_20_R1", "v1_20_R2", "v1_20_R3", "v1_21_R0", "v1_21_R1", "v1_21_R3"};
        for (String version : versions) {
            for (String className : commonClasses) {
                addMapping("org.bukkit.craftbukkit." + version + "." + className, 
                          "org.bukkit.craftbukkit." + className);
            }
        }
    }
    
    private static void addMapping(String versionedClass, String nonVersionedClass) {
        VERSION_MAPPINGS.put(versionedClass, nonVersionedClass);
        // Also add internal name mappings (with slashes)
        VERSION_MAPPINGS.put(versionedClass.replace('.', '/'), nonVersionedClass.replace('.', '/'));
    }
    
    /**
     * Maps a versioned CraftBukkit class name to its non-versioned equivalent
     * 
     * @param className The class name to map
     * @return The mapped class name, or the original if no mapping exists
     */
    public static String mapVersionedClass(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }
        
        // Check direct mapping first
        String mapped = VERSION_MAPPINGS.get(className);
        if (mapped != null) {
            LOGGER.debug("Direct mapping: {} -> {}", className, mapped);
            return mapped;
        }
        
        // Check if it's a versioned class and try pattern-based mapping
        if (isVersionedCraftBukkitClass(className)) {
            // Try to remove version pattern
            String withoutVersion = VERSION_PATTERN.matcher(className).replaceFirst("");
            // Fix double dots that might occur
            withoutVersion = withoutVersion.replace("..", ".");
            
            if (!withoutVersion.equals(className)) {
                LOGGER.debug("Pattern-based mapping: {} -> {}", className, withoutVersion);
                return withoutVersion;
            }
        }
        
        return className;
    }
    
    /**
     * Checks if a class name is a versioned CraftBukkit class
     * 
     * @param className The class name to check
     * @return true if it's a versioned CraftBukkit class
     */
    public static boolean isVersionedCraftBukkitClass(String className) {
        if (className == null) {
            return false;
        }
        return className.contains("craftbukkit") && VERSION_PATTERN.matcher(className).find();
    }
    
    /**
     * Maps a class name for reflection purposes
     * Handles both regular class names and internal names (with slashes)
     * 
     * @param className The class name to map
     * @return The mapped class name
     */
    public static String mapForReflection(String className) {
        if (className == null) {
            return null;
        }
        
        String mapped = mapVersionedClass(className);
        if (!mapped.equals(className)) {
            LOGGER.debug("Reflection mapping: {} -> {}", className, mapped);
        }
        
        return mapped;
    }
    
    /**
     * Gets all registered mappings (for debugging)
     * 
     * @return A copy of the mappings map
     */
    public static Map<String, String> getAllMappings() {
        return new HashMap<>(VERSION_MAPPINGS);
    }
    
    /**
     * Adds a custom mapping at runtime
     * 
     * @param versionedClass The versioned class name
     * @param nonVersionedClass The non-versioned class name
     */
    public static void addCustomMapping(String versionedClass, String nonVersionedClass) {
        VERSION_MAPPINGS.put(versionedClass, nonVersionedClass);
        VERSION_MAPPINGS.put(versionedClass.replace('.', '/'), nonVersionedClass.replace('.', '/'));
        LOGGER.info("Added custom mapping: {} -> {}", versionedClass, nonVersionedClass);
    }
}