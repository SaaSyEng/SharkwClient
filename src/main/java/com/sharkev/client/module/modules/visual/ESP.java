package com.sharkev.client.module.modules.visual;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ESP extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Boolean> players = addBool("Players", true);
    private final Setting<Boolean> mobs = addBool("Mobs", false);
    private final Setting<Boolean> items = addBool("Items", false);
    private final ModeSetting boxStyle = addMode("Box Style", "2D", "2D", "Outline");
    private final Setting<Float> lineWidth = addSlider("Line Width", 2.0f, 1.0f, 5.0f);

    // Colors
    private static final int COLOR_ENEMY = 0xFFE94560;   // accent red-pink
    private static final int COLOR_TEAM = 0xFF00E676;     // green for teammates
    private static final int COLOR_MOB = 0xFFFF9800;      // orange for mobs
    private static final int COLOR_ITEM = 0xFF64B5F6;     // light blue for items

    // Max render distance for alpha calculation
    private static final float MAX_DISTANCE = 200.0f;
    private static final float MIN_ALPHA = 0.15f;
    private static final float MAX_ALPHA = 0.85f;

    public ESP() {
        super("ESP", "Entity boxes and outlines through walls", Category.VISUAL, 0);
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
        GL11.glLineWidth(lineWidth.getFloat());

        double px = mc.getRenderManager().viewerPosX;
        double py = mc.getRenderManager().viewerPosY;
        double pz = mc.getRenderManager().viewerPosZ;

        for (Entity e : new ArrayList<>(mc.theWorld.loadedEntityList)) {
            if (e == mc.thePlayer) continue;

            // Filter by entity type and settings
            boolean isPlayer = e instanceof EntityPlayer;
            boolean isMob = e instanceof EntityMob || e instanceof EntityAnimal;
            boolean isItem = e instanceof EntityItem;

            if (isPlayer && !players.getBool()) continue;
            if (isMob && !mobs.getBool()) continue;
            if (isItem && !items.getBool()) continue;
            if (!isPlayer && !isMob && !isItem) continue;

            // Interpolated position
            double ix = e.lastTickPosX + (e.posX - e.lastTickPosX) * event.partialTicks - px;
            double iy = e.lastTickPosY + (e.posY - e.lastTickPosY) * event.partialTicks - py;
            double iz = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * event.partialTicks - pz;

            // Use entity bounding box dimensions
            AxisAlignedBB bb = e.getEntityBoundingBox();
            float halfW = (float) ((bb.maxX - bb.minX) / 2.0);
            float height = (float) (bb.maxY - bb.minY);

            // Distance-based alpha
            float dist = mc.thePlayer.getDistanceToEntity(e);
            float alphaFactor = 1.0f - Math.min(dist / MAX_DISTANCE, 1.0f);
            float alpha = MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * alphaFactor;

            // Determine color
            int color;
            if (isPlayer) {
                boolean sameTeam = mc.thePlayer.getTeam() != null
                        && mc.thePlayer.getTeam().equals(((EntityPlayer) e).getTeam());
                color = sameTeam ? COLOR_TEAM : COLOR_ENEMY;
            } else if (isMob) {
                color = COLOR_MOB;
            } else {
                color = COLOR_ITEM;
            }

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            if (boxStyle.getMode().equals("Outline")) {
                drawBox3D(ix - halfW, iy, iz - halfW, ix + halfW, iy + height, iz + halfW, r, g, b, alpha);
            } else {
                drawBox2D(ix - halfW, iy, iz - halfW, ix + halfW, iy + height, iz + halfW, r, g, b, alpha);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void preRender() {
        // Called from MixinEntityRenderer before world render pass
    }

    /**
     * 2D style: only draws top and bottom rectangles + vertical corner lines.
     */
    private void drawBox2D(double x1, double y1, double z1,
                           double x2, double y2, double z2,
                           float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        // Bottom face
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y1, z2).color(r, g, b, a).endVertex();
        tess.draw();

        // Top face
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z2).color(r, g, b, a).endVertex();
        tess.draw();

        // Vertical corner lines
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z2).color(r, g, b, a).endVertex();
        tess.draw();
    }

    /**
     * Full 3D outline: all 12 edges of the box.
     */
    private void drawBox3D(double x1, double y1, double z1,
                           double x2, double y2, double z2,
                           float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        // Bottom face
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y1, z2).color(r, g, b, a).endVertex();
        tess.draw();

        // Top face
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z2).color(r, g, b, a).endVertex();
        tess.draw();

        // Vertical edges
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        wr.pos(x2, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y1, z2).color(r, g, b, a).endVertex();
        wr.pos(x1, y2, z2).color(r, g, b, a).endVertex();
        tess.draw();
    }
}
