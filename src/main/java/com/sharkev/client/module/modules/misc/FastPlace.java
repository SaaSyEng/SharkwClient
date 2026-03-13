package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FastPlace extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> delay = addSlider("Delay", 0f, 0f, 3f);

    public FastPlace() {
        super("FastPlace", "Place blocks faster", Category.MISC, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        // Reset rightClickDelayTimer to allow faster placement
        try {
            java.lang.reflect.Field f = Minecraft.class.getDeclaredField("rightClickDelayTimer");
            f.setAccessible(true);
            int currentDelay = f.getInt(mc);
            int targetDelay = (int) delay.getFloat();
            if (currentDelay > targetDelay) {
                f.setInt(mc, targetDelay);
            }
        } catch (Exception e) {
            try {
                java.lang.reflect.Field f = Minecraft.class.getDeclaredField("field_71467_ac");
                f.setAccessible(true);
                int currentDelay = f.getInt(mc);
                int targetDelay = (int) delay.getFloat();
                if (currentDelay > targetDelay) {
                    f.setInt(mc, targetDelay);
                }
            } catch (Exception ignored) {}
        }
    }
}
