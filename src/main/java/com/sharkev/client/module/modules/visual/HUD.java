package com.sharkev.client.module.modules.visual;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.module.modules.misc.Blink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HUD extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Boolean> watermark = addBool("Watermark", true);
    private final Setting<Boolean> moduleList = addBool("Module List", true);
    private final ModeSetting arrayListSide = addMode("Array Side", "Right", "Right", "Left");

    // Accent color: #E94560
    private static final int ACCENT = 0xFFE94560;
    private static final int BG_DARK = 0xCC0D0D1A;
    private static final int BG_WATERMARK = 0xDD0D0D1A;
    private static final int TEXT_WHITE = 0xFFE0E0E0;
    private static final int TEXT_GRAY = 0xFF808090;

    // Slide animation tracking: module name -> current slide progress (0 = hidden, 1 = fully visible)
    private final Map<String, Float> slideProgress = new HashMap<>();
    // Track which modules were active last frame for exit animation
    private final Map<String, Float> exitingModules = new HashMap<>();

    public HUD() {
        super("HUD", "Premium dark HUD overlay", Category.VISUAL, 0);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        if (watermark.getBool()) {
            drawWatermark(fr);
            drawInfoLine(fr);
        }
        if (moduleList.getBool()) {
            drawModuleList(fr, sr);
        }
    }

    /**
     * Sleek watermark pill: "SharkevClient" top-left with dark background.
     */
    private void drawWatermark(FontRenderer fr) {
        String text = "SharkevClient";
        int textW = fr.getStringWidth(text);
        int padX = 8;
        int padY = 4;
        int pillW = textW + padX * 2;
        int pillH = fr.FONT_HEIGHT + padY * 2;
        int x = 4;
        int y = 4;

        // Dark pill background (simulated rounded corners)
        Gui.drawRect(x + 2, y, x + pillW - 2, y + pillH, BG_WATERMARK);
        Gui.drawRect(x, y + 2, x + pillW, y + pillH - 2, BG_WATERMARK);
        Gui.drawRect(x + 1, y + 1, x + pillW - 1, y + pillH - 1, BG_WATERMARK);

        // Thin accent line at top of pill
        Gui.drawRect(x + 2, y, x + pillW - 2, y + 1, ACCENT);

        // Text with shadow
        fr.drawStringWithShadow(text, x + padX, y + padY, ACCENT);
    }

    /**
     * Info line below watermark: FPS, ping, coordinates.
     */
    private void drawInfoLine(FontRenderer fr) {
        int fps = Minecraft.getDebugFPS();

        // Try to get ping
        int ping = -1;
        if (mc.getNetHandler() != null && mc.thePlayer != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info != null) {
                ping = info.getResponseTime();
            }
        }

        // Coordinates
        String coords = String.format("%.0f, %.0f, %.0f",
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        StringBuilder sb = new StringBuilder();
        sb.append(fps).append(" FPS");
        if (ping >= 0) {
            sb.append("  |  ").append(ping).append("ms");
        }
        sb.append("  |  ").append(coords);

        int infoY = 4 + fr.FONT_HEIGHT + 4 + 4 + 4;
        fr.drawStringWithShadow(sb.toString(), 6, infoY, TEXT_GRAY);
    }

    /**
     * Module list: sorted by text width descending. Each module has a slim dark bar
     * with a 2px colored accent line on the edge. Smooth rainbow gradient shifts by Y position.
     * Slide animation for appearing/disappearing.
     */
    private void drawModuleList(FontRenderer fr, ScaledResolution sr) {
        boolean rightSide = arrayListSide.getMode().equals("Right");
        int screenW = sr.getScaledWidth();

        // Gather active modules
        List<Module> active = new ArrayList<>();
        for (Module m : SharkevClient.moduleManager.getModules()) {
            if (m.isEnabled() && !(m instanceof HUD)) {
                active.add(m);
            }
        }
        active.sort((a, b) -> Integer.compare(
                fr.getStringWidth(getLabel(b)), fr.getStringWidth(getLabel(a))
        ));

        // Update slide progress: increase for active, decrease for exiting
        // Mark active modules
        for (Module m : active) {
            String key = m.getName();
            float progress = slideProgress.containsKey(key) ? slideProgress.get(key) : 0.0f;
            progress = Math.min(progress + 0.1f, 1.0f);
            slideProgress.put(key, progress);
            exitingModules.remove(key);
        }

        // Handle exiting modules (were in slideProgress but no longer active)
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Float> entry : slideProgress.entrySet()) {
            String key = entry.getKey();
            boolean found = false;
            for (Module m : active) {
                if (m.getName().equals(key)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                float progress = entry.getValue() - 0.1f;
                if (progress <= 0.0f) {
                    toRemove.add(key);
                } else {
                    entry.setValue(progress);
                    exitingModules.put(key, progress);
                }
            }
        }
        for (String key : toRemove) {
            slideProgress.remove(key);
            exitingModules.remove(key);
        }

        int y = 2;
        int padH = 5;
        int padV = 2;
        int index = 0;

        for (Module m : active) {
            String label = getLabel(m);
            int textW = fr.getStringWidth(label);
            int entryW = textW + padH * 2 + 2; // +2 for accent line
            int entryH = fr.FONT_HEIGHT + padV * 2;

            float progress = slideProgress.containsKey(m.getName()) ? slideProgress.get(m.getName()) : 1.0f;
            // Ease out cubic
            float eased = 1.0f - (1.0f - progress) * (1.0f - progress) * (1.0f - progress);

            int x;
            if (rightSide) {
                int targetX = screenW - entryW;
                int startX = screenW;
                x = (int) (startX + (targetX - startX) * eased);
            } else {
                int targetX = 0;
                int startX = -entryW;
                x = (int) (startX + (targetX - startX) * eased);
            }

            // Gradient color based on Y position (smooth rainbow/gradient shift)
            int accentColor = getGradientColor(y);

            // Dark background bar
            if (rightSide) {
                Gui.drawRect(x, y, screenW, y + entryH, BG_DARK);
                // 2px accent line on the right edge
                Gui.drawRect(screenW - 2, y, screenW, y + entryH, accentColor);
                // Module name
                fr.drawStringWithShadow(label, x + padH, y + padV, TEXT_WHITE);
            } else {
                Gui.drawRect(x, y, x + entryW, y + entryH, BG_DARK);
                // 2px accent line on the left edge
                Gui.drawRect(x, y, x + 2, y + entryH, accentColor);
                // Module name
                fr.drawStringWithShadow(label, x + 2 + padH, y + padV, TEXT_WHITE);
            }

            y += entryH;
            index++;
        }
    }

    /**
     * Returns a smooth gradient color that shifts based on Y position.
     * Goes through the accent hue range for a premium look.
     */
    private int getGradientColor(int y) {
        // Shift hue based on y position and time for smooth animation
        long time = System.currentTimeMillis();
        float hue = ((y * 2.5f + time / 20.0f) % 360.0f) / 360.0f;
        // Keep saturation high, brightness high for vivid colors
        int rgb = Color.HSBtoRGB(hue, 0.7f, 1.0f);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    private String getLabel(Module m) {
        String label = m.getName();
        if (m instanceof Blink) {
            label += " [" + ((Blink) m).getBufferSize() + "]";
        }
        return label;
    }
}
