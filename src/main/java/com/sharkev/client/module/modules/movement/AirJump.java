package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AirJump extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> maxJumps = addSlider("Max Jumps", 2f, 1f, 5f);

    private int airJumpsLeft = 0;
    private boolean wasJumpDown = false;

    public AirJump() {
        super("AirJump", "Jump multiple times in the air", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        boolean jumpDown = mc.gameSettings.keyBindJump.isKeyDown();
        int maxAirJumps = (int) maxJumps.getFloat();

        if (mc.thePlayer.onGround) {
            airJumpsLeft = maxAirJumps;
        }

        // Detect fresh press (rising edge)
        if (jumpDown && !wasJumpDown && !mc.thePlayer.onGround && airJumpsLeft > 0) {
            mc.thePlayer.motionY = 0.42;
            mc.thePlayer.fallDistance = 0;
            airJumpsLeft--;
        }

        wasJumpDown = jumpDown;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        airJumpsLeft = 0;
        wasJumpDown = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        airJumpsLeft = 0;
        wasJumpDown = false;
    }
}
