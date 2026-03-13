package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * FreeCam - Detaches the camera from the player for local observation.
 * The server sees the player standing still at the saved position.
 * The camera moves freely using WASD + Space/Shift.
 */
public class FreeCam extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> speed = addSlider("Speed", 1.0f, 0.5f, 5.0f);

    private double savedX, savedY, savedZ;
    private float savedYaw, savedPitch;
    private double camX, camY, camZ;

    public FreeCam() {
        super("FreeCam", "Detach camera from player (spectator-like)", Category.MISC, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.thePlayer == null) return;
        savedX     = mc.thePlayer.posX;
        savedY     = mc.thePlayer.posY;
        savedZ     = mc.thePlayer.posZ;
        savedYaw   = mc.thePlayer.rotationYaw;
        savedPitch = mc.thePlayer.rotationPitch;
        camX = savedX;
        camY = savedY;
        camZ = savedZ;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null) return;
        // Snap player back to saved position
        mc.thePlayer.setPositionAndRotation(savedX, savedY, savedZ, savedYaw, savedPitch);
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionY = 0;
        mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        float moveSpeed = speed.getFloat() * 0.3f;
        float yawRad = (float) Math.toRadians(mc.thePlayer.rotationYaw);

        double mx = 0, my = 0, mz = 0;
        if (mc.gameSettings.keyBindForward.isKeyDown()) {
            mx -= Math.sin(yawRad) * moveSpeed;
            mz += Math.cos(yawRad) * moveSpeed;
        }
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            mx += Math.sin(yawRad) * moveSpeed;
            mz -= Math.cos(yawRad) * moveSpeed;
        }
        if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            mx -= Math.cos(yawRad) * moveSpeed;
            mz -= Math.sin(yawRad) * moveSpeed;
        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {
            mx += Math.cos(yawRad) * moveSpeed;
            mz += Math.sin(yawRad) * moveSpeed;
        }
        if (mc.gameSettings.keyBindJump.isKeyDown())  my += moveSpeed;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))  my -= moveSpeed;

        camX += mx;
        camY += my;
        camZ += mz;

        // Move player entity to camera position visually
        // Server still sees saved position since we don't send position packets
        mc.thePlayer.setPositionAndRotation(camX, camY, camZ,
            mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionY = 0;
        mc.thePlayer.motionZ = 0;
    }
}
