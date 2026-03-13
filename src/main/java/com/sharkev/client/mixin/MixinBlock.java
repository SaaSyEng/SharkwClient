package com.sharkev.client.mixin;

import com.sharkev.client.module.modules.visual.XRay;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void onShouldSideBeRendered(IBlockAccess worldIn, BlockPos pos,
            EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if (XRay.isActive()) {
            Block self = (Block)(Object) this;
            if (XRay.isXRayBlock(self)) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getLightOpacity", at = @At("HEAD"), cancellable = true)
    private void onGetLightOpacity(CallbackInfoReturnable<Integer> cir) {
        if (XRay.isActive()) {
            Block self = (Block)(Object) this;
            if (XRay.isXRayBlock(self)) {
                cir.setReturnValue(255);
            } else {
                cir.setReturnValue(0);
            }
        }
    }
}
