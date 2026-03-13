package com.sharkev.client.module.modules.combat;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Random;

public class AimAssist extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rand = new Random();

    // Settings
    private final Setting<Float> range = addSlider("Range", 4.5f, 2.0f, 8.0f);
    private final Setting<Float> fov = addSlider("FOV", 90f, 15f, 180f);
    private final Setting<Float> speed = addSlider("Speed", 0.45f, 0.1f, 1.0f);
    private final Setting<Boolean> onlyClick = addBool("Only Click", true);
    private final Setting<Boolean> correctPitch = addBool("Correct Pitch", false);
    private final Setting<Boolean> targetMobs = addBool("Target Mobs", false);
    private final Setting<Boolean> teamCheck = addBool("Team Check", true);

    private float lastTargetYaw = 0;

    public AimAssist() {
        super("AimAssist", "Smooth aim correction towards targets", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (onlyClick.getBool() && !org.lwjgl.input.Mouse.isButtonDown(0)) return;

        EntityLivingBase target = getTarget();
        if (target == null) return;

        // Use actual partialTicks for frame independence
        float partialTicks = event.renderTickTime;

        // Calculate needed rotation with interpolated target position
        double interpX = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks;
        double interpY = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks
                + target.getEyeHeight() * 0.9;
        double interpZ = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks;

        double dx = interpX - mc.thePlayer.posX;
        double dy = interpY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = interpZ - mc.thePlayer.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float neededYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float neededPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        float yawDiff = MathUtil.wrapAngle(neededYaw - mc.thePlayer.rotationYaw);

        // Adaptive speed: fast for big corrections, slow for small
        float absDiff = Math.abs(yawDiff);
        float adaptiveSpeed = speed.getFloat();
        if (absDiff < 3.0f) adaptiveSpeed *= 0.3f;
        else if (absDiff < 10.0f) adaptiveSpeed *= 0.6f;
        else if (absDiff > 45.0f) adaptiveSpeed *= 1.2f;

        // Per-frame delta scaled by partialTicks for frame independence
        float correction = yawDiff * adaptiveSpeed * 0.5f * Math.min(partialTicks, 1.0f);

        // Add micro-noise with increased sigma (0.15) to look human
        correction += (float) (rand.nextGaussian() * 0.15);

        // Apply GCD fix
        float newYaw = mc.thePlayer.rotationYaw + correction;
        float[] fixed = MathUtil.applyGCD(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
            newYaw, mc.thePlayer.rotationPitch);

        mc.thePlayer.rotationYaw = fixed[0];

        // Optionally correct pitch too
        if (correctPitch.getBool()) {
            float pitchDiff = neededPitch - mc.thePlayer.rotationPitch;
            float pitchCorrection = pitchDiff * adaptiveSpeed * 0.3f * Math.min(partialTicks, 1.0f);
            pitchCorrection += (float) (rand.nextGaussian() * 0.15);
            float[] fixedP = MathUtil.applyGCD(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
                mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch + pitchCorrection);
            mc.thePlayer.rotationPitch = MathUtil.clampf(fixedP[1], -90f, 90f);
        }
    }

    private EntityLivingBase getTarget() {
        EntityLivingBase closest = null;
        float smallestAngle = fov.getFloat();

        for (Entity e : new ArrayList<>(mc.theWorld.loadedEntityList)) {
            if (e == mc.thePlayer) continue;

            boolean isPlayer = e instanceof EntityPlayer;
            boolean isMob = e instanceof EntityMob;

            if (!isPlayer && !isMob) continue;
            if (isMob && !targetMobs.getBool()) continue;

            EntityLivingBase living = (EntityLivingBase) e;
            if (living.getHealth() <= 0 || living.isDead) continue;

            if (teamCheck.getBool() && isPlayer) {
                if (mc.thePlayer.getTeam() != null
                        && mc.thePlayer.getTeam().equals(living.getTeam())) continue;
            }

            double d = mc.thePlayer.getDistanceToEntity(e);
            if (d > range.getFloat()) continue;

            // Get angle to target
            double dx = e.posX - mc.thePlayer.posX;
            double dz = e.posZ - mc.thePlayer.posZ;
            float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            float angle = Math.abs(MathUtil.wrapAngle(targetYaw - mc.thePlayer.rotationYaw));

            if (angle < smallestAngle) {
                smallestAngle = angle;
                closest = living;
            }
        }
        return closest;
    }
}
