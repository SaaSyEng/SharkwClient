package com.sharkev.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class RotationUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    /**
     * Calculate yaw/pitch to the center of an entity's bounding box (eye height).
     * Uses player eye position as origin.
     */
    public static float[] getRotationsToEntity(Entity target) {
        double eyeX = mc.thePlayer.posX;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double eyeZ = mc.thePlayer.posZ;

        double targetX = target.posX;
        double targetY = target.posY + target.getEyeHeight();
        double targetZ = target.posZ;

        double dx = targetX - eyeX;
        double dy = targetY - eyeY;
        double dz = targetZ - eyeZ;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -(Math.toDegrees(Math.atan2(dy, horizontalDist)));

        return new float[]{yaw, pitch};
    }

    /**
     * Smooth rotation with ease-out interpolation.
     * speed: 0.0 = no movement, 1.0 = instant snap
     */
    public static float[] smoothRotate(float curYaw, float curPitch,
                                        float targetYaw, float targetPitch,
                                        float speed) {
        float yawDiff = MathUtil.wrapAngle(targetYaw - curYaw);
        float pitchDiff = targetPitch - curPitch;

        // Ease-out: faster at start, slower at end
        float newYaw = curYaw + yawDiff * speed;
        float newPitch = curPitch + pitchDiff * speed;

        newPitch = MathHelper.clamp_float(newPitch, -90.0f, 90.0f);

        return new float[]{newYaw, newPitch};
    }

    /**
     * Check if an entity is within the given FOV angle from current look direction.
     */
    public static boolean isInFOV(Entity entity, float fov) {
        float[] needed = getRotationsToEntity(entity);
        float yawDiff = Math.abs(MathUtil.wrapAngle(needed[0] - mc.thePlayer.rotationYaw));
        float pitchDiff = Math.abs(needed[1] - mc.thePlayer.rotationPitch);
        float totalDiff = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        return totalDiff <= fov / 2.0f;
    }

    /**
     * Add gaussian noise to yaw and pitch.
     * sigma controls the standard deviation of the noise.
     */
    public static float[] addNoise(float yaw, float pitch, float sigma) {
        yaw += (float) (random.nextGaussian() * sigma);
        pitch += (float) (random.nextGaussian() * sigma);
        pitch = MathHelper.clamp_float(pitch, -90.0f, 90.0f);
        return new float[]{yaw, pitch};
    }
}
