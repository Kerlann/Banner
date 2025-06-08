package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter; // For Invoker
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock; // For event
import org.bukkit.craftbukkit.block.CraftBlockState; // Keep for casting
import org.bukkit.craftbukkit.block.data.CraftBlockData; // For BlockData conversion
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockFormEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect; // Added back Redirect import
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.Invoker; // Removed import, will use FQN
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// CraftBlockState import is not strictly necessary if variable is typed as org.bukkit.block.BlockState
// but CraftBlockStates.getBlockState returns CraftBlockState, so it's fine.
// All other necessary imports are already present.

// Removed ConcretePowderBlockAccess class

@Mixin(ConcretePowderBlock.class)
public abstract class MixinConcretePowderBlock extends Block {

    @Shadow @Final private Block concrete;

    @Shadow
    protected static boolean shouldSolidify(BlockGetter level, BlockPos pos, BlockState state) {
        // This body is a placeholder for the shadow method and won't be executed.
        // Mixin will replace calls to this with calls to the actual ConcretePowderBlock.shouldSolidify.
        throw new AbstractMethodError("Mixin shadow for shouldSolidify not implemented");
    }

    public MixinConcretePowderBlock(Properties properties) {
        super(properties);
    }

    // This existing Redirect for onLand should remain as it's not part of the subtask to remove it.
    @Redirect(method = "onLand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$blockFormOnLand(Level world, BlockPos pos, BlockState newState, int flags) { // Renamed to avoid collision, though not strictly necessary due to signature
        return CraftEventFactory.handleBlockFormEvent(world, pos, newState, flags);
    }

    // This existing Redirect for getStateForPlacement should also remain.
    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState banner$blockFormPlacement(Block instance, BlockPlaceContext context) { // Renamed to avoid collision
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        org.bukkit.block.Block bukkitBlockAtPos = CraftBlock.at(world, blockPos);
        // Create a CraftBlockState representing the *intended* new state (the concrete block)
        CraftBlockState newCraftBlockState = (CraftBlockState) bukkitBlockAtPos.getState(); // Get current state shell
        // newCraftBlockState.setHandle(this.concrete.defaultBlockState()); // Incorrect
        newCraftBlockState.setBlockData(CraftBlockData.fromData(this.concrete.defaultBlockState())); // Corrected

        BlockFormEvent event = new BlockFormEvent(bukkitBlockAtPos, newCraftBlockState);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            // If not cancelled, the event.getNewState() (which is newCraftBlockState) holds the state to place.
            // We need to return its NMS handle.
            return ((CraftBlockState) event.getNewState()).getHandle();
        }
        // If cancelled, proceed with original logic (which might be getting default state of powder, not this.concrete)
        // This redirect is on defaultBlockState(), so returning super.getStateForPlacement is not quite right.
        // It should return the original powder block's default state if cancelled.
        // The 'instance' is the ConcretePowderBlock itself.
        return instance.defaultBlockState(); // Or context.getClickedState() if it's about replacing something existing.
                                          // Given it's getStateForPlacement, returning default powder state makes sense if event cancelled.
    }

    // Removed the old commented-out @Redirect block for updateShape

    @Inject(method = "updateShape",
            at = @At("HEAD"),
            cancellable = true)
    public void banner$onUpdateShapeConcrete(BlockState stateIn, Direction facing, BlockState facingState, // Made public as per typical injector style, though private should also work.
                                              LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos,
                                              CallbackInfoReturnable<BlockState> cir) {
        // Call the shadowed static method
        if (shouldSolidify(worldIn, currentPos, stateIn)) { // No class prefix, calls the @Shadow method
            // Ensure worldIn is a Level for CraftBlockState and Bukkit event context
            if (worldIn instanceof Level) {
                Level level = (Level) worldIn;
                org.bukkit.block.Block bukkitBlock = CraftBlock.at(level, currentPos);

                // Create a CraftBlockState that represents the *new* state (the concrete block)
                CraftBlockState newBlockCraftState = (CraftBlockState) bukkitBlock.getState(); // Get a CraftBlockState for current location
                // newBlockCraftState.setHandle(this.concrete.defaultBlockState()); // Incorrect
                newBlockCraftState.setBlockData(CraftBlockData.fromData(this.concrete.defaultBlockState())); // Corrected

                BlockFormEvent event = new BlockFormEvent(bukkitBlock, newBlockCraftState);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    // If not cancelled, the event.getNewState() (which is newBlockCraftState) holds the state to use.
                    cir.setReturnValue(((CraftBlockState) event.getNewState()).getHandle());
                } else {
                    // Event was cancelled, prevent solidification, return original powder state
                    cir.setReturnValue(stateIn);
                }
                cir.cancel(); // Always cancel if we handled it (event fired)
            } else {
                // Not a fully realized Level, cannot fire Bukkit events. Let original logic proceed.
                // This case might occur during world generation or other non-standard contexts.
            }
        }
        // If shouldSolidify is false, do nothing, let original updateShape logic run.
    }
}
