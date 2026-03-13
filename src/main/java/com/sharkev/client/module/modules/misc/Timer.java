package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class Timer extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> speed = addSlider("Speed", 1.5f, 0.5f, 5.0f);

    private Field timerField = null;
    private Field ticksPerSecondField = null;
    private final float defaultTimer = 20f;

    public Timer() {
        super("Timer", "Speed up game tick rate", Category.MISC, 0);
        resolveTimerFields();
    }

    private void resolveTimerFields() {
        try {
            // Try to find the Timer field in Minecraft class
            // It can be obfuscated, so we search by type rather than name
            for (Field f : Minecraft.class.getDeclaredFields()) {
                // Check by simple name (works in dev) and by full name (works in production)
                String typeName = f.getType().getSimpleName();
                String fullName = f.getType().getName();
                if (typeName.equals("Timer") || fullName.equals("net.minecraft.util.Timer")
                        || fullName.contains("Timer")) {
                    timerField = f;
                    timerField.setAccessible(true);
                    break;
                }
            }

            if (timerField != null) {
                Object timerObj = timerField.get(mc);
                if (timerObj != null) {
                    // Find the ticksPerSecond field - it's a float field with value 20.0
                    for (Field f : timerObj.getClass().getDeclaredFields()) {
                        if (f.getType() == float.class) {
                            f.setAccessible(true);
                            float val = f.getFloat(timerObj);
                            if (Math.abs(val - 20.0f) < 0.01f) {
                                ticksPerSecondField = f;
                                break;
                            }
                        }
                    }
                    // Fallback: try by field name
                    if (ticksPerSecondField == null) {
                        try {
                            ticksPerSecondField = timerObj.getClass().getDeclaredField("ticksPerSecond");
                            ticksPerSecondField.setAccessible(true);
                        } catch (NoSuchFieldException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (timerField == null || ticksPerSecondField == null) return;
        try {
            Object timerObj = timerField.get(mc);
            ticksPerSecondField.setFloat(timerObj, defaultTimer * speed.getFloat());
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (timerField == null || ticksPerSecondField == null) return;
        try {
            Object timerObj = timerField.get(mc);
            ticksPerSecondField.setFloat(timerObj, defaultTimer);
        } catch (Exception ignored) {}
    }
}
