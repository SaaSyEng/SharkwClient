package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeSetting mode = addMode("Mode", "Vanilla", "Vanilla", "Glide");
    private final Setting<Float> speed = addSlider("Speed", 1.0f, 0.5f, 5.0f);
    private final Setting<Float> glideSpeed = addSlider("Glide Speed", 0.04f, 0.01f, 0.1f);

    private int tickCounter = 0;

    public Fly() {
        super("Fly", "Fly freely in the air", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        tickCounter++;

        float flySpeed = speed.getFloat();
        String currentMode = mode.getMode();

        // Anti-kick: send a small downward position packet every 40 ticks
        // to prevent the server from kicking for "flying is not enabled"
        if (tickCounter % 40 == 0) {
            PacketUtil.sendPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY - 0.04,
                mc.thePlayer.posZ,
                false
            );
        }

        if (currentMode.equals("Vanilla")) {
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.motionY = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = flySpeed;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                mc.thePlayer.motionY = -flySpeed;
            }

            // Horizontal movement
            float yaw = (float) Math.toRadians(mc.thePlayer.rotationYaw);
            double moveX = 0, moveZ = 0;

            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                moveX -= Math.sin(yaw) * flySpeed;
                moveZ += Math.cos(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindBack.isKeyDown()) {
                moveX += Math.sin(yaw) * flySpeed;
                moveZ -= Math.cos(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                moveX -= Math.cos(yaw) * flySpeed;
                moveZ -= Math.sin(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindRight.isKeyDown()) {
                moveX += Math.cos(yaw) * flySpeed;
                moveZ += Math.sin(yaw) * flySpeed;
            }

            mc.thePlayer.motionX = moveX;
            mc.thePlayer.motionZ = moveZ;
        } else if (currentMode.equals("Glide")) {
            // Glide mode: slowly descend while moving fast horizontally
            mc.thePlayer.motionY = -glideSpeed.getFloat();
            mc.thePlayer.capabilities.isFlying = false;

            // If holding jump, neutralize descent
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = 0;
            }
            // If holding sneak, descend faster
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                mc.thePlayer.motionY = -flySpeed * 0.5;
            }

            // Horizontal glide at configured speed
            float yaw = (float) Math.toRadians(mc.thePlayer.rotationYaw);
            double moveX = 0, moveZ = 0;

            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                moveX -= Math.sin(yaw) * flySpeed;
                moveZ += Math.cos(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindBack.isKeyDown()) {
                moveX += Math.sin(yaw) * flySpeed;
                moveZ -= Math.cos(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                moveX -= Math.cos(yaw) * flySpeed;
                moveZ -= Math.sin(yaw) * flySpeed;
            }
            if (mc.gameSettings.keyBindRight.isKeyDown()) {
                moveX += Math.cos(yaw) * flySpeed;
                moveZ += Math.sin(yaw) * flySpeed;
            }

            mc.thePlayer.motionX = moveX;
            mc.thePlayer.motionZ = moveZ;
        }

        // Send zero fall distance to avoid damage
        mc.thePlayer.fallDistance = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.capabilities.isFlying = false;
        }
    }
}
