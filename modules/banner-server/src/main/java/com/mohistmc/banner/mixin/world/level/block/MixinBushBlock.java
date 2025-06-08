package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
// Removed Redirect import, added Inject and CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// LevelAccessor, Direction, BlockPos are already imported via wildcard or directly earlier.

@Mixin(BushBlock.class)
public abstract class MixinBushBlock extends Block {

    public MixinBushBlock(Properties properties) {
        super(properties);
    }

    // Removed the old commented-out @Redirect block

    @Inject(method = "updateShape", // Rely on Mixin to match signature from method parameters
            at = @At("HEAD"),
            cancellable = true)
    private void banner$onUpdateShape(BlockState stateIn, Direction facing, BlockState facingState,
                                      LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos,
                                      CallbackInfoReturnable<BlockState> cir) {
        // No need to check for ServerLevel explicitly for CraftEventFactory.callBlockPhysicsEvent
        // as it's designed to work with LevelAccessor and often handles the check internally or the event is NOP on client.
        // The primary concern is whether Bukkit API is available, which it is in a server context.
        if (CraftEventFactory.callBlockPhysicsEvent(worldIn, currentPos).isCancelled()) {
            cir.setReturnValue(stateIn);
            // cir.cancel(); // Not strictly needed after setReturnValue if this is the only modification at HEAD.
                           // However, if there were other HEAD injectors or if we want to be absolutely sure no more processing happens, it's good practice.
                           // The subtask asks for it.
            cir.cancel();
        }
    }
}
