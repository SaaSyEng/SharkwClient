package com.sharkev.client.util;

import net.minecraft.client.Minecraft;

public class MathUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // =====================================================================
    // GCD Fix - makes rotations look like real mouse input
    // MC internally computes mouse delta as:
    //   f = sensitivity * 0.6F + 0.2F
    //   gcd = f * f * f * 8.0F
    // Rotations must be multiples of this GCD to appear legitimate.
    // =====================================================================

    private static float gcdValue = 0.0f;

    static {
        updateGCD();
    }

    /**
     * Recalculate the GCD value based on the player's current mouse sensitivity.
     * Falls back to default 0.5f if game settings are unavailable.
     */
    public static void updateGCD() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.gameSettings != null) {
                float sens = mc.gameSettings.mouseSensitivity;
                float f = sens * 0.6F + 0.2F;
                gcdValue = f * f * f * 8.0F;
                return;
            }
        } catch (Exception ignored) {}
        // Fallback to default sensitivity
        float f = 0.5f * 0.6f + 0.2f;
        gcdValue = f * f * f * 8.0f;
    }

    /**
     * Apply GCD fix to rotation deltas.
     * Rounds the rotation change from prev to new to the nearest GCD multiple.
     */
    public static float[] applyGCD(float prevYaw, float prevPitch,
                                    float newYaw, float newPitch) {
        if (gcdValue <= 0.0f) {
            updateGCD();
        }

        float deltaYaw = newYaw - prevYaw;
        float deltaPitch = newPitch - prevPitch;

        // Round deltas to nearest multiple of GCD
        deltaYaw = Math.round(deltaYaw / gcdValue) * gcdValue;
        deltaPitch = Math.round(deltaPitch / gcdValue) * gcdValue;

        return new float[]{prevYaw + deltaYaw, prevPitch + deltaPitch};
    }

    public static float getGCDValue() {
        return gcdValue;
    }

    // =====================================================================
    // Math helpers
    // =====================================================================

    /**
     * Wrap angle to [-180, 180] range.
     */
    public static float wrapAngle(float angle) {
        angle = angle % 360.0f;
        if (angle > 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    /**
     * Clamp a float value between min and max.
     */
    public static float clampf(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Ease-out interpolation: current + (target - current) * speed
     */
    public static float easeOut(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    /**
     * Euclidean GCD for two floats.
     */
    public static float gcd(float a, float b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b > 0.0001f) {
            float t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    /**
     * Clamp a double value between min and max.
     */
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Distance between two 2D points.
     */
    public static double dist2D(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1, dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Random float in range [min, max).
     */
    public static float randFloat(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    /**
     * Random int in [min, max].
     */
    public static int randInt(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }
}
