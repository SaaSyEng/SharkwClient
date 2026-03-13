package com.sharkev.client.module.modules.visual;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class Tracers extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Boolean> players = addBool("Players", true);
    private final Setting<Boolean> mobs = addBool("Mobs", false);
    private final Setting<Float> lineWidthSetting = addSlider("Line Width", 1.0f, 1.0f, 3.0f);
    private final Setting<Float> maxDistance = addSlider("Max Distance", 200.0f, 50.0f, 500.0f);

    // Colors
    private static final float[] COLOR_ENEMY = {0.914f, 0.271f, 0.376f}; // #E94560
    private static final float[] COLOR_TEAM = {0.0f, 0.902f, 0.463f};     // #00E676
    private static final float[] COLOR_MOB = {1.0f, 0.596f, 0.0f};        // #FF9800

    public Tracers() {
        super("Tracers", "Draw lines to entities from crosshair", Category.VISUAL, 0);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidthSetting.getFloat());

        double px = mc.getRenderManager().viewerPosX;
        double py = mc.getRenderManager().viewerPosY;
        double pz = mc.getRenderManager().viewerPosZ;

        float maxDist = maxDistance.getFloat();

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        for (Entity e : new ArrayList<>(mc.theWorld.loadedEntityList)) {
            if (e == mc.thePlayer) continue;

            boolean isPlayer = e instanceof EntityPlayer;
            boolean isMob = e instanceof EntityMob || e instanceof EntityAnimal;

            if (isPlayer && !players.getBool()) continue;
            if (isMob && !mobs.getBool()) continue;
            if (!isPlayer && !isMob) continue;

            // Distance filter
            float dist = mc.thePlayer.getDistanceToEntity(e);
            if (dist > maxDist) continue;

            // Interpolated target position (center of entity)
            double tx = e.lastTickPosX + (e.posX - e.lastTickPosX) * event.partialTicks - px;
            double ty = (e.lastTickPosY + (e.posY - e.lastTickPosY) * event.partialTicks) - py
                    + (e.height / 2.0);
            double tz = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * event.partialTicks - pz;

            // Color: team color for players, orange for mobs
            float r, g, b;
            if (isPlayer) {
                boolean sameTeam = mc.thePlayer.getTeam() != null
                        && mc.thePlayer.getTeam().equals(((EntityPlayer) e).getTeam());
                r = sameTeam ? COLOR_TEAM[0] : COLOR_ENEMY[0];
                g = sameTeam ? COLOR_TEAM[1] : COLOR_ENEMY[1];
                b = sameTeam ? COLOR_TEAM[2] : COLOR_ENEMY[2];
            } else {
                r = COLOR_MOB[0];
                g = COLOR_MOB[1];
                b = COLOR_MOB[2];
            }

            // Alpha based on distance: closer = more opaque
            float alpha = 0.3f + 0.6f * (1.0f - dist / maxDist);

            wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            wr.pos(0, mc.thePlayer.getEyeHeight(), 0).color(r, g, b, alpha).endVertex();
            wr.pos(tx, ty, tz).color(r, g, b, alpha).endVertex();
            tess.draw();
        }

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
