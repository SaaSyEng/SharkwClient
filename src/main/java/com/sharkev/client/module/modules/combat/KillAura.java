package com.sharkev.client.module.modules.combat;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.module.modules.misc.FlagDetector;
import com.sharkev.client.util.MathUtil;
import com.sharkev.client.util.RandomUtil;
import com.sharkev.client.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> range = addSlider("Range", 3.0f, 2.5f, 6.0f);
    private final Setting<Float> minCPS = addSlider("Min CPS", 10f, 5f, 20f);
    private final Setting<Float> maxCPS = addSlider("Max CPS", 14f, 5f, 20f);
    private final Setting<Float> rotSpeed = addSlider("Rot Speed", 25f, 5f, 30f);
    private final ModeSetting mode = addMode("Mode", "Single", "Single", "Switch");
    private final Setting<Boolean> targetPlayers = addBool("Target Players", true);
    private final Setting<Boolean> targetMobs = addBool("Target Mobs", false);
    private final Setting<Boolean> teamCheck = addBool("Team Check", true);
    private final Setting<Boolean> throughWall = addBool("Through Wall", false);

    private long nextAttackAt = 0;
    private EntityLivingBase currentTarget = null;
    private float serverYaw, serverPitch;
    private boolean tracking = false;
    private int switchIndex = 0;
    /** Set to true on the tick that an attack packet is actually sent. */
    private boolean attackedThisTick = false;

    public KillAura() {
        super("KillAura", "Silent aura with proper packet rotation", Category.COMBAT, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tracking = false;
        nextAttackAt = 0;
        currentTarget = null;
        attackedThisTick = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
        tracking = false;
        attackedThisTick = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;

        attackedThisTick = false;

        FlagDetector fd = (FlagDetector) SharkevClient.moduleManager.getByName("FlagDetector");
        if (fd != null && fd.isInSafeMode()) return;

        long now = System.currentTimeMillis();

        // Build target list
        List<EntityLivingBase> targets = getTargets();
        if (targets.isEmpty()) {
            currentTarget = null;
            tracking = false;
            return;
        }

        // Select target based on mode
        if (mode.getMode().equals("Switch")) {
            switchIndex = switchIndex % targets.size();
            currentTarget = targets.get(switchIndex);
        } else {
            currentTarget = targets.get(0);
        }

        if (!tracking) {
            serverYaw = mc.thePlayer.rotationYaw;
            serverPitch = mc.thePlayer.rotationPitch;
            tracking = true;
        }

        // Calculate rotation to target with interpolation for moving targets
        double targetX = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX);
        double targetY = currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY)
                + currentTarget.getEyeHeight() * 0.9;
        double targetZ = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ);

        // Check Backtrack for delayed positions
        Backtrack bt = (Backtrack) SharkevClient.moduleManager.getByName("Backtrack");
        if (bt != null && bt.isEnabled()) {
            double[] delayed = bt.getDelayedPosition(currentTarget);
            if (delayed != null) {
                double delayedDist = Math.sqrt(
                    (delayed[0] - mc.thePlayer.posX) * (delayed[0] - mc.thePlayer.posX) +
                    (delayed[2] - mc.thePlayer.posZ) * (delayed[2] - mc.thePlayer.posZ)
                );
                double currentDist = mc.thePlayer.getDistanceToEntity(currentTarget);
                // Use delayed position if it's closer (enemy was moving toward us)
                if (delayedDist < currentDist) {
                    targetX = delayed[0];
                    targetY = delayed[1] + currentTarget.getEyeHeight() * 0.9;
                    targetZ = delayed[2];
                }
            }
        }

        double dx = targetX - mc.thePlayer.posX;
        double dy = targetY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = targetZ - mc.thePlayer.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float neededYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float neededPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        // Smooth server-side rotation with speed limit (clamped to human max 25 deg/tick)
        float maxRot = rotSpeed.getFloat();
        float yawDiff = MathUtil.wrapAngle(neededYaw - serverYaw);
        float pitchDiff = neededPitch - serverPitch;

        yawDiff = MathUtil.clampf(yawDiff, -maxRot, maxRot);
        pitchDiff = MathUtil.clampf(pitchDiff, -maxRot * 0.6f, maxRot * 0.6f);

        serverYaw += yawDiff;
        serverPitch += pitchDiff;
        serverPitch = MathUtil.clampf(serverPitch, -90f, 90f);

        // Apply GCD fix
        float[] fixed = MathUtil.applyGCD(
            mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch,
            serverYaw, serverPitch
        );
        serverYaw = fixed[0];
        serverPitch = fixed[1];

        // Check if we're actually facing the target (within ~30 degrees)
        float aimDiff = Math.abs(MathUtil.wrapAngle(neededYaw - serverYaw));
        if (aimDiff > 30f) {
            // Still rotating, don't attack yet - just send rotation update
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C05PacketPlayerLook(serverYaw, serverPitch, mc.thePlayer.onGround)
            );
            return;
        }

        // CPS timing check
        if (now < nextAttackAt) {
            // Still send rotation even if not attacking
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C05PacketPlayerLook(serverYaw, serverPitch, mc.thePlayer.onGround)
            );
            return;
        }

        // Send look packet, then attack via packet, then swing
        mc.thePlayer.sendQueue.addToSendQueue(
            new C03PacketPlayer.C05PacketPlayerLook(serverYaw, serverPitch, mc.thePlayer.onGround)
        );

        // Attack via packet (server uses our last sent rotation to validate)
        mc.thePlayer.sendQueue.addToSendQueue(
            new C02PacketUseEntity(currentTarget, C02PacketUseEntity.Action.ATTACK)
        );

        // Visual swing (client-side only)
        mc.thePlayer.swingItem();

        attackedThisTick = true;

        // Switch target after attack in Switch mode
        if (mode.getMode().equals("Switch")) {
            switchIndex++;
        }

        // Schedule next attack
        float cps = RandomUtil.nextCPS(minCPS.getFloat(), maxCPS.getFloat());
        nextAttackAt = now + RandomUtil.nextClickDelay(cps);
    }

    private List<EntityLivingBase> getTargets() {
        List<EntityLivingBase> candidates = new ArrayList<>();

        for (Entity e : new ArrayList<>(mc.theWorld.loadedEntityList)) {
            if (e == mc.thePlayer) continue;

            boolean isPlayer = e instanceof EntityPlayer;
            boolean isMob = e instanceof EntityMob;

            if (isPlayer && !targetPlayers.getBool()) continue;
            if (isMob && !targetMobs.getBool()) continue;
            if (!isPlayer && !isMob) continue;

            EntityLivingBase living = (EntityLivingBase) e;
            if (living.getHealth() <= 0 || living.isDead) continue;

            if (teamCheck.getBool() && isPlayer) {
                if (mc.thePlayer.getTeam() != null
                        && mc.thePlayer.getTeam().equals(living.getTeam())) continue;
            }

            double dist = mc.thePlayer.getDistanceToEntity(e);
            if (dist > range.getFloat()) continue;
            if (!throughWall.getBool() && !mc.thePlayer.canEntityBeSeen(e)) continue;

            candidates.add(living);
        }

        if (candidates.isEmpty()) return candidates;

        // Sort by distance, then health (finish low HP first)
        candidates.sort(Comparator.<EntityLivingBase>comparingDouble(
            e -> mc.thePlayer.getDistanceToEntity(e)
        ).thenComparingDouble(EntityLivingBase::getHealth));

        return candidates;
    }

    public EntityLivingBase getCurrentTarget() { return currentTarget; }

    /** Returns true if KillAura sent an attack packet this tick. */
    public boolean didAttackThisTick() { return attackedThisTick; }
}
