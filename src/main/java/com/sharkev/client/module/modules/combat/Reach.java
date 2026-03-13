package com.sharkev.client.module.modules.combat;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class Reach extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings - default 3.1 is just barely above vanilla 3.0 (safest)
    private final Setting<Float> range = addSlider("Range", 3.1f, 3.0f, 6.0f);

    public Reach() {
        super("Reach", "Slightly extended reach distance", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!Mouse.isButtonDown(0)) return;
        if (mc.currentScreen != null) return;

        float reachDist = range.getFloat();

        // Only extend if vanilla didn't already hit something
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) return;

        // Raycast from eyes in look direction at extended range
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 lookVec = mc.thePlayer.getLook(1.0f);
        Vec3 extendedPos = eyePos.addVector(
            lookVec.xCoord * reachDist,
            lookVec.yCoord * reachDist,
            lookVec.zCoord * reachDist
        );

        Entity bestEntity = null;
        double bestDist = reachDist;

        for (Entity e : new ArrayList<>(mc.theWorld.loadedEntityList)) {
            if (e == mc.thePlayer) continue;
            if (!(e instanceof EntityPlayer) && !(e instanceof EntityMob)) continue;
            if (((EntityLivingBase) e).getHealth() <= 0) continue;

            float expand = e.getCollisionBorderSize();
            AxisAlignedBB bb = e.getEntityBoundingBox().expand(expand, expand, expand);
            MovingObjectPosition intercept = bb.calculateIntercept(eyePos, extendedPos);

            if (intercept != null) {
                double d = eyePos.distanceTo(intercept.hitVec);
                if (d < bestDist) {
                    bestDist = d;
                    bestEntity = e;
                }
            }
        }

        if (bestEntity != null && bestDist > 3.0 && bestDist <= reachDist) {
            mc.thePlayer.swingItem();
            mc.playerController.attackEntity(mc.thePlayer, bestEntity);
        }
    }
}
