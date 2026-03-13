package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AntiAFK extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> interval = addSlider("Interval", 3f, 1f, 10f);
    private final ModeSetting mode = addMode("Mode", "Both", "Rotate", "Move", "Both");

    private int tickCounter = 0;
    private int rotDir = 1;

    public AntiAFK() {
        super("AntiAFK", "Prevent AFK kick by simulating movement", Category.MISC, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tickCounter = 0;
        rotDir = 1;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        tickCounter++;

        int intervalTicks = (int) (interval.getFloat() * 20f);
        if (intervalTicks < 1) intervalTicks = 1;

        if (tickCounter % intervalTicks != 0) return;

        String currentMode = mode.getMode();

        // Rotate action - oscillate yaw relative to current player yaw (no drift)
        if (currentMode.equals("Rotate") || currentMode.equals("Both")) {
            float yawOffset = 15f * rotDir;
            rotDir *= -1; // alternate direction each time to avoid drift

            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C05PacketPlayerLook(
                    mc.thePlayer.rotationYaw + yawOffset,
                    mc.thePlayer.rotationPitch,
                    mc.thePlayer.onGround
                )
            );
        }

        // Move action - small position jitter to reset idle timer
        if (currentMode.equals("Move") || currentMode.equals("Both")) {
            double offsetX = (tickCounter % (intervalTicks * 2) == 0) ? 0.04 : -0.04;
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX + offsetX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.onGround
                )
            );
        }

        // Periodic sneak toggle to further reset idle timer
        if (tickCounter % (intervalTicks * 3) == 0) {
            mc.thePlayer.setSneaking(true);
        }
        if (tickCounter % (intervalTicks * 3) == 5) {
            mc.thePlayer.setSneaking(false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.setSneaking(false);
        }
    }
}
