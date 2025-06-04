package com.mohistmc.banner.bukkit.remapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.List;

/**
 * Generates proxy classes for versioned CraftBukkit classes at runtime
 * to make plugins that expect versioned classes work with Banner's non-versioned classes.
 */
public class VersionedClassGenerator {
    
    private static final Logger LOGGER = LogManager.getLogger("Banner-VersionGen");
    private static final String TARGET_VERSION = "v1_21_R1"; // Current version
    private static final List<String> COMMON_CLASSES = Arrays.asList(
        "CraftWorld", "CraftServer", "CraftChunk", "CraftOfflinePlayer",
        "entity/CraftPlayer", "entity/CraftEntity", "entity/CraftLivingEntity",
        "inventory/CraftItemStack", "inventory/CraftInventory",
        "block/CraftBlock", "util/CraftMagicNumbers"
    );
    
    /**
     * Generates all commonly used proxy classes
     */
    public static void generateProxyClasses() {
        LOGGER.info("Generating versioned CraftBukkit proxy classes...");
        
        for (String className : COMMON_CLASSES) {
            try {
                generateProxyClass(className);
            } catch (Exception e) {
                LOGGER.error("Failed to generate proxy class for: " + className, e);
            }
        }
        
        LOGGER.info("Finished generating {} proxy classes", COMMON_CLASSES.size());
    }
    
    /**
     * Generates a single proxy class that extends the non-versioned Banner class
     */
    private static void generateProxyClass(String className) {
        String versionedPackage = "org/bukkit/craftbukkit/" + TARGET_VERSION + "/";
        String originalPackage = "org/bukkit/craftbukkit/";
        
        String versionedClass = versionedPackage + className;
        String originalClass = originalPackage + className;
        
        // Instead of extending, create an alias by defining the versioned class
        // to be identical to the original class
        try {
            // First, load the original class to get its bytecode
            Class<?> originalClazz = Class.forName(originalClass.replace('/', '.'));
            
            // Get the bytecode of the original class
            String resourceName = "/" + originalClass + ".class";
            byte[] originalBytecode;
            
            try (var is = originalClazz.getResourceAsStream(resourceName)) {
                if (is == null) {
                    LOGGER.warn("Could not find bytecode for: " + originalClass);
                    return;
                }
                originalBytecode = is.readAllBytes();
            }
            
            // Modify the bytecode to change the class name
            ClassReader reader = new ClassReader(originalBytecode);
            ClassWriter writer = new ClassWriter(0);
            
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    // Change the class name from original to versioned
                    super.visit(version, access, versionedClass, signature, superName, interfaces);
                }
            };
            
            reader.accept(visitor, 0);
            byte[] modifiedBytecode = writer.toByteArray();
            
            // Define the class in the ClassLoader
            String javaClassName = versionedClass.replace('/', '.');
            Class<?> definedClass = Unsafe.defineClass(javaClassName, modifiedBytecode, 0, modifiedBytecode.length, 
                             VersionedClassGenerator.class.getClassLoader(), 
                             VersionedClassGenerator.class.getProtectionDomain());
            
            LOGGER.debug("Successfully generated proxy class: {}", javaClassName);
            
        } catch (Exception e) {
            LOGGER.error("Failed to generate proxy class for: " + className, e);
        }
    }
    
    /**
     * Generate proxy classes for a specific version
     */
    public static void generateProxyClassesForVersion(String version) {
        LOGGER.info("Generating proxy classes for version: {}", version);
        
        for (String className : COMMON_CLASSES) {
            try {
                generateProxyClassForVersion(className, version);
            } catch (Exception e) {
                LOGGER.error("Failed to generate proxy class for: " + className + " version: " + version, e);
            }
        }
    }
    
    private static void generateProxyClassForVersion(String className, String version) {
        String versionedPackage = "org/bukkit/craftbukkit/" + version + "/";
        String originalPackage = "org/bukkit/craftbukkit/";
        
        String versionedClass = versionedPackage + className;
        String originalClass = originalPackage + className;
        
        try {
            // First, load the original class to get its bytecode
            Class<?> originalClazz = Class.forName(originalClass.replace('/', '.'));
            
            // Get the bytecode of the original class
            String resourceName = "/" + originalClass + ".class";
            byte[] originalBytecode;
            
            try (var is = originalClazz.getResourceAsStream(resourceName)) {
                if (is == null) {
                    LOGGER.warn("Could not find bytecode for: " + originalClass);
                    return;
                }
                originalBytecode = is.readAllBytes();
            }
            
            // Modify the bytecode to change the class name
            ClassReader reader = new ClassReader(originalBytecode);
            ClassWriter writer = new ClassWriter(0);
            
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    // Change the class name from original to versioned
                    super.visit(version, access, versionedClass, signature, superName, interfaces);
                }
            };
            
            reader.accept(visitor, 0);
            byte[] modifiedBytecode = writer.toByteArray();
            
            // Define the class in the ClassLoader
            String javaClassName = versionedClass.replace('/', '.');
            Unsafe.defineClass(javaClassName, modifiedBytecode, 0, modifiedBytecode.length, 
                             VersionedClassGenerator.class.getClassLoader(), 
                             VersionedClassGenerator.class.getProtectionDomain());
            
            LOGGER.debug("Generated proxy for version {}: {}", version, javaClassName);
            
        } catch (Exception e) {
            LOGGER.error("Failed to generate proxy class: " + versionedClass + " for version: " + version, e);
        }
    }
    
    /**
     * Generate all proxy classes for common versions
     */
    public static void generateAllVersionProxies() {
        String[] versions = {"v1_20_R1", "v1_20_R2", "v1_20_R3", "v1_21_R0", "v1_21_R1", "v1_21_R3"};
        
        for (String version : versions) {
            generateProxyClassesForVersion(version);
        }
    }
}
