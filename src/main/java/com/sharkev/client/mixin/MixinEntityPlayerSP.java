package com.sharkev.client.mixin;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Module;
import com.sharkev.client.module.modules.movement.NoSlow;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(
        method = "onUpdateWalkingPlayer",
        at = @At("HEAD")
    )
    private void onWalkingPlayerHead(CallbackInfo ci) {
        if (SharkevClient.moduleManager == null) return;

        EntityPlayerSP player = (EntityPlayerSP)(Object)this;

        // NoSlow: cancel item-use speed penalty at movement source
        NoSlow noSlow = (NoSlow) SharkevClient.moduleManager.getByName("NoSlow");
        if (noSlow != null && noSlow.isEnabled()) {
            player.movementInput.sneak = false;
        }

        // Step: modify step height - Step module handles its own stepHeight in onTick
        // We only need to reset to default when Step is disabled
        Module step = SharkevClient.moduleManager.getByName("Step");
        if (step == null || !step.isEnabled()) {
            player.stepHeight = 0.5F;
        }
    }
}
