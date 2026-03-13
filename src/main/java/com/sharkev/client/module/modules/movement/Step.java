package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Step extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> height = addSlider("Height", 1.0f, 0.5f, 2.5f);
    private final ModeSetting mode = addMode("Mode", "Vanilla", "Vanilla", "Packet");

    private boolean wasOnGround = true;
    private double prevY = 0;

    public Step() {
        super("Step", "Step up blocks without jumping", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        float stepHeight = height.getFloat();
        String currentMode = mode.getMode();

        if (currentMode.equals("Packet")) {
            // Detect when we just stepped up (Y increased while on ground)
            boolean onGround = mc.thePlayer.onGround;
            double curY = mc.thePlayer.posY;

            // Set stepHeight so vanilla step logic triggers
            mc.thePlayer.stepHeight = stepHeight + 0.5f;

            if (onGround && wasOnGround) {
                double diff = curY - prevY;
                if (diff > 0.1 && diff <= stepHeight) {
                    // Send intermediate packets to smooth the step server-side
                    // Split into multiple sub-steps for larger heights
                    int steps = (int) Math.ceil(diff / 0.5);
                    for (int i = 1; i < steps; i++) {
                        double intermediateY = prevY + diff * ((double) i / steps);
                        PacketUtil.sendPosition(mc.thePlayer.posX, intermediateY,
                            mc.thePlayer.posZ, false);
                    }
                }
            }
            wasOnGround = onGround;
            prevY = curY;
        } else {
            // Vanilla mode: simply override step height
            mc.thePlayer.stepHeight = stepHeight + 0.5f;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        wasOnGround = true;
        prevY = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.5f;
        }
    }
}
