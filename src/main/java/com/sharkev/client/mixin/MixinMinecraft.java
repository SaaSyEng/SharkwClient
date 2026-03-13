package com.sharkev.client.mixin;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.util.MathUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(
        method = "runTick",
        at = @At("HEAD")
    )
    private void onTickHead(CallbackInfo ci) {
        // Ensure client is initialized even if @Mod didn't fire
        SharkevClient.doInit();
        MathUtil.updateGCD();
    }
}
