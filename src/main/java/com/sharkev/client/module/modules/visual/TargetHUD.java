package com.sharkev.client.module.modules.visual;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.module.modules.combat.KillAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TargetHUD extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Boolean> showArmor = addBool("Show Armor", true);
    private final Setting<Boolean> showDistance = addBool("Show Distance", true);

    // Dimensions
    private static final int BOX_W = 170;
    private static final int BOX_H = 52;

    // Colors
    private static final int BG_COLOR = 0xDD0D0D1A;
    private static final int BORDER_COLOR = 0x40FFFFFF;
    private static final int BAR_BG = 0xFF2A2A3A;
    private static final int TEXT_WHITE = 0xFFE0E0E0;
    private static final int TEXT_GRAY = 0xFF808090;
    private static final int ACCENT = 0xFFE94560;

    // Smooth health animation
    private float displayedHealth = 0.0f;
    private EntityLivingBase lastTarget = null;

    public TargetHUD() {
        super("TargetHUD", "Premium target information panel", Category.VISUAL, 0);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        // Find target: from KillAura or from crosshair entity
        EntityLivingBase target = null;

        // Check KillAura first
        KillAura ka = (KillAura) SharkevClient.moduleManager.getByName("KillAura");
        if (ka != null && ka.isEnabled() && ka.getCurrentTarget() != null) {
            target = ka.getCurrentTarget();
        }

        // Check crosshair entity
        if (target == null && mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            target = (EntityLivingBase) mc.objectMouseOver.entityHit;
        }

        if (target == null) {
            lastTarget = null;
            return;
        }

        // Reset displayed health when target changes
        if (target != lastTarget) {
            displayedHealth = target.getHealth();
            lastTarget = target;
        }

        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        // Position: center screen, 65% down
        int bx = sr.getScaledWidth() / 2 - BOX_W / 2;
        int by = (int) (sr.getScaledHeight() * 0.65f);

        // Calculate actual box height based on settings
        int actualH = BOX_H;
        if (showArmor.getBool() && target instanceof EntityPlayer) {
            actualH += 14;
        }

        // -- Dark panel background with subtle border --
        // Main background
        Gui.drawRect(bx, by, bx + BOX_W, by + actualH, BG_COLOR);
        // Subtle 1px border
        Gui.drawRect(bx, by, bx + BOX_W, by + 1, BORDER_COLOR); // top
        Gui.drawRect(bx, by + actualH - 1, bx + BOX_W, by + actualH, BORDER_COLOR); // bottom
        Gui.drawRect(bx, by, bx + 1, by + actualH, BORDER_COLOR); // left
        Gui.drawRect(bx + BOX_W - 1, by, bx + BOX_W, by + actualH, BORDER_COLOR); // right

        // Accent line at top
        Gui.drawRect(bx + 1, by, bx + BOX_W - 1, by + 2, ACCENT);

        // -- Target name --
        String name = target instanceof EntityPlayer ? target.getName() : target.getClass().getSimpleName();
        fr.drawStringWithShadow(name, bx + 8, by + 6, TEXT_WHITE);

        // -- Distance --
        if (showDistance.getBool()) {
            double dist = mc.thePlayer.getDistanceToEntity(target);
            String distStr = String.format("%.1fm", dist);
            int distW = fr.getStringWidth(distStr);
            fr.drawStringWithShadow(distStr, bx + BOX_W - distW - 8, by + 6, TEXT_GRAY);
        }

        // -- Health bar with smooth animation --
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();

        // Interpolate displayed health toward actual health
        displayedHealth += (hp - displayedHealth) * 0.15f;
        if (Math.abs(displayedHealth - hp) < 0.01f) displayedHealth = hp;

        float ratio = Math.max(0, Math.min(displayedHealth / maxHp, 1.0f));

        int barX = bx + 8;
        int barY = by + 20;
        int barW = BOX_W - 16;
        int barH = 10;

        // Bar background
        Gui.drawRect(barX, barY, barX + barW, barY + barH, BAR_BG);

        // Health bar: gradient based on health PERCENTAGE (green at high, red at low)
        int filledW = (int) (barW * ratio);
        if (filledW > 0) {
            int barColor = getHealthColor(ratio);
            Gui.drawRect(barX, barY, barX + filledW, barY + barH, barColor);

            // Subtle highlight on top edge of health bar
            int highlightColor = 0x30FFFFFF;
            Gui.drawRect(barX, barY, barX + filledW, barY + 1, highlightColor);
        }

        // -- HP text below bar --
        String hpStr = String.format("%.1f / %.0f", displayedHealth, maxHp);
        int hpStrW = fr.getStringWidth(hpStr);
        fr.drawStringWithShadow(hpStr, barX + (barW - hpStrW) / 2, barY + barH + 3, TEXT_GRAY);

        // -- Armor display --
        if (showArmor.getBool() && target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            int armorY = barY + barH + 3 + fr.FONT_HEIGHT + 2;
            int totalArmor = player.getTotalArmorValue();
            String armorStr = "Armor: " + totalArmor;
            fr.drawStringWithShadow(armorStr, barX, armorY, TEXT_GRAY);
        }
    }

    /**
     * Returns a color from red to green based on health percentage.
     */
    private int getHealthColor(float ratio) {
        // ratio: 0.0 = dead (red), 1.0 = full (green)
        int r, g;
        if (ratio > 0.5f) {
            // Green to yellow range
            float t = (ratio - 0.5f) * 2.0f;
            r = (int) (255 * (1.0f - t));
            g = 255;
        } else {
            // Yellow to red range
            float t = ratio * 2.0f;
            r = 255;
            g = (int) (255 * t);
        }
        return 0xFF000000 | (r << 16) | (g << 8) | 0x00;
    }
}
