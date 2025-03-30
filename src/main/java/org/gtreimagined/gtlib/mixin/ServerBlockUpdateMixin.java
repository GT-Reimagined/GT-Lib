package org.gtreimagined.gtlib.mixin;

import org.gtreimagined.gtlib.GTAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerBlockUpdateMixin {
    @Inject(at = @At("HEAD"), method = "sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V")
    private void onNotifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo info) {
        GTAPI.onNotifyBlockUpdate((Level) (Object) this, pos, oldState, newState, flags);
    }
}
