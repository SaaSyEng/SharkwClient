package com.sharkev.client.mixin;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.modules.visual.ESP;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(
        method = "renderWorldPass",
        at = @At("HEAD")
    )
    private void onRenderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (SharkevClient.moduleManager == null) return;
        ESP esp = (ESP) SharkevClient.moduleManager.getByName("ESP");
        if (esp != null && esp.isEnabled()) {
            esp.preRender();
        }
    }
}
