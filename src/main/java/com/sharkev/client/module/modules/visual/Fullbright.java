package com.sharkev.client.module.modules.visual;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fullbright extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> gamma = addSlider("Gamma", 1000.0f, 5.0f, 1000.0f);

    private float prevGamma = 1.0f;

    public Fullbright() {
        super("Fullbright", "Maximum brightness everywhere", Category.VISUAL, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        prevGamma = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.gammaSetting = prevGamma;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        mc.gameSettings.gammaSetting = gamma.getFloat();
    }
}
