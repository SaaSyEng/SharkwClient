package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Speed extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeSetting mode = addMode("Mode", "Sprint", "Sprint", "SlowHop", "GroundStrafe");
    private final Setting<Float> speedMultiplier = addSlider("Multiplier", 1.0f, 1.0f, 2.0f);

    private int stage = 0;
    private double moveSpeed = 0;
    private double lastDist = 0;

    public Speed() {
        super("Speed", "Speed boost (Sprint/SlowHop/GroundStrafe)", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        boolean moving = mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;

        // Calculate distance moved last tick
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        String currentMode = mode.getMode();

        if (currentMode.equals("Sprint")) {
            // Just auto-sprint - completely safe, no flags ever
            if (moving && mc.thePlayer.moveForward > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.setSprinting(true);
            }
        } else if (currentMode.equals("SlowHop")) {
            if (!moving) {
                stage = 0;
                return;
            }

            // Force sprint
            if (mc.thePlayer.moveForward > 0) {
                mc.thePlayer.setSprinting(true);
            }

            if (mc.thePlayer.onGround) {
                stage = 0;
                // Randomize jump height slightly to avoid constant signature
                double jumpY = ThreadLocalRandom.current().nextDouble(0.3990, 0.4005);
                mc.thePlayer.motionY = jumpY;

                // Get base speed with potion effects
                double baseSpeed = 0.2873;
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
                }

                moveSpeed = baseSpeed * 1.35 * speedMultiplier.getFloat();
                setMoveSpeed(moveSpeed);
            } else {
                stage++;
                // Reduce speed in air to match vanilla deceleration curve
                if (stage == 1) {
                    moveSpeed = lastDist - 0.01 * (lastDist - getBaseSpeed());
                } else {
                    moveSpeed = lastDist - lastDist / 159.0;
                }
                setMoveSpeed(Math.max(getBaseSpeed(), moveSpeed));
            }
        } else if (currentMode.equals("GroundStrafe")) {
            if (!moving) return;

            // Force sprint
            if (mc.thePlayer.moveForward > 0) {
                mc.thePlayer.setSprinting(true);
            }

            if (mc.thePlayer.onGround) {
                // Optimize strafe direction on ground without jumping
                double speed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                    + mc.thePlayer.motionZ * mc.thePlayer.motionZ);

                // Only boost if below sprint cap (don't flag speed checks)
                double sprintCap = getBaseSpeed() * 1.3 * speedMultiplier.getFloat();
                if (speed < sprintCap) {
                    // Apply additive boost instead of compound multiplication
                    double boost = (sprintCap - speed) * 0.05;
                    setMoveSpeed(speed + boost);
                }
            }
        }
    }

    private void setMoveSpeed(double speed) {
        float yaw = mc.thePlayer.rotationYaw;
        float forward = mc.thePlayer.moveForward;
        float strafe = mc.thePlayer.moveStrafing;

        if (forward == 0 && strafe == 0) return;

        double direction = yaw;
        if (forward != 0) {
            if (strafe > 0) direction -= (forward > 0 ? 45 : -45);
            else if (strafe < 0) direction += (forward > 0 ? 45 : -45);

            if (forward < 0) direction -= 180; // backward
            strafe = 0;
            forward = 1;
        }

        double rad = Math.toRadians(direction);
        mc.thePlayer.motionX = -Math.sin(rad) * speed;
        mc.thePlayer.motionZ = Math.cos(rad) * speed;
    }

    private double getBaseSpeed() {
        double base = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            base *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return base;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        stage = 0;
        moveSpeed = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stage = 0;
        moveSpeed = 0;
    }
}
