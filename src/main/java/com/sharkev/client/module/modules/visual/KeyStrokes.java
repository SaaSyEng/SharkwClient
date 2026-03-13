package com.sharkev.client.module.modules.visual;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeyStrokes extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Boolean> showCPS = addBool("Show CPS", true);
    private final ModeSetting position = addMode("Position", "BottomLeft", "BottomLeft", "BottomRight", "TopLeft");

    // Dimensions
    private static final int KEY_SIZE = 26;
    private static final int GAP = 2;
    private static final int MOUSE_BTN_W = 40; // each mouse button width

    // Colors
    private static final int BG_NORMAL = 0xCC1A1A2E;
    private static final int BG_PRESSED = 0xFFE94560; // accent red-pink
    private static final int TEXT_WHITE = 0xFFE0E0E0;
    private static final int TEXT_DARK = 0xFF1A1A2E;
    private static final int TEXT_CPS = 0xFF808090;

    // CPS tracking
    private final List<Long> lmbClicks = new ArrayList<>();
    private final List<Long> rmbClicks = new ArrayList<>();
    private boolean lmbWasDown = false;
    private boolean rmbWasDown = false;

    // Smooth fade per key (0.0 = released, 1.0 = fully pressed)
    private float fadeW = 0, fadeA = 0, fadeS = 0, fadeD = 0;
    private float fadeLMB = 0, fadeRMB = 0;

    public KeyStrokes() {
        super("KeyStrokes", "Premium WASD and mouse key display", Category.VISUAL, 0);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        long now = System.currentTimeMillis();

        // -- CPS tracking --
        boolean lmbDown = Mouse.isButtonDown(0);
        boolean rmbDown = Mouse.isButtonDown(1);

        if (lmbDown && !lmbWasDown) lmbClicks.add(now);
        if (rmbDown && !rmbWasDown) rmbClicks.add(now);
        lmbWasDown = lmbDown;
        rmbWasDown = rmbDown;

        pruneOld(lmbClicks, now);
        pruneOld(rmbClicks, now);

        int lmbCPS = lmbClicks.size();
        int rmbCPS = rmbClicks.size();

        // -- Key states --
        boolean w = mc.gameSettings.keyBindForward.isKeyDown();
        boolean a = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean s = mc.gameSettings.keyBindBack.isKeyDown();
        boolean d = mc.gameSettings.keyBindRight.isKeyDown();

        // -- Smooth fade animation --
        fadeW = animateFade(fadeW, w);
        fadeA = animateFade(fadeA, a);
        fadeS = animateFade(fadeS, s);
        fadeD = animateFade(fadeD, d);
        fadeLMB = animateFade(fadeLMB, lmbDown);
        fadeRMB = animateFade(fadeRMB, rmbDown);

        // -- Calculate total block size --
        int totalW = KEY_SIZE * 3 + GAP * 2;
        int totalH = KEY_SIZE * 2 + GAP; // WASD rows
        int mouseRowH = KEY_SIZE;
        int cpsRowH = showCPS.getBool() ? fr.FONT_HEIGHT + 2 : 0;
        int fullH = totalH + GAP + mouseRowH + (showCPS.getBool() ? GAP + cpsRowH : 0);

        // -- Position based on setting --
        int ox, oy;
        String pos = position.getMode();
        if (pos.equals("BottomRight")) {
            ox = sr.getScaledWidth() - totalW - 8;
            oy = sr.getScaledHeight() - fullH - 8;
        } else if (pos.equals("TopLeft")) {
            ox = 8;
            oy = 40; // below watermark area
        } else { // BottomLeft
            ox = 8;
            oy = sr.getScaledHeight() - fullH - 8;
        }

        // Row 0: W centered above ASD row
        int wX = ox + KEY_SIZE + GAP;
        drawKey(fr, wX, oy, KEY_SIZE, KEY_SIZE, "W", fadeW);

        // Row 1: A S D
        int row1y = oy + KEY_SIZE + GAP;
        drawKey(fr, ox, row1y, KEY_SIZE, KEY_SIZE, "A", fadeA);
        drawKey(fr, ox + KEY_SIZE + GAP, row1y, KEY_SIZE, KEY_SIZE, "S", fadeS);
        drawKey(fr, ox + (KEY_SIZE + GAP) * 2, row1y, KEY_SIZE, KEY_SIZE, "D", fadeD);

        // Row 2: LMB and RMB
        int row2y = row1y + KEY_SIZE + GAP;
        drawKey(fr, ox, row2y, MOUSE_BTN_W, KEY_SIZE, "LMB", fadeLMB);
        drawKey(fr, ox + MOUSE_BTN_W + GAP, row2y, MOUSE_BTN_W, KEY_SIZE, "RMB", fadeRMB);

        // Row 3: CPS counters
        if (showCPS.getBool()) {
            int row3y = row2y + KEY_SIZE + GAP + 1;
            String lmbText = "[" + lmbCPS + "] CPS";
            String rmbText = "[" + rmbCPS + "] CPS";

            int lmbTextX = ox + (MOUSE_BTN_W - fr.getStringWidth(lmbText)) / 2;
            fr.drawStringWithShadow(lmbText, lmbTextX, row3y, TEXT_CPS);

            int rmbTextX = ox + MOUSE_BTN_W + GAP + (MOUSE_BTN_W - fr.getStringWidth(rmbText)) / 2;
            fr.drawStringWithShadow(rmbText, rmbTextX, row3y, TEXT_CPS);
        }
    }

    private void drawKey(FontRenderer fr, int x, int y, int w, int h, String label, float fade) {
        // Interpolate background: dark to accent
        int bg = interpolateColor(BG_NORMAL, BG_PRESSED, fade);
        // Interpolate text: white to dark
        int fg = interpolateColor(TEXT_WHITE, TEXT_DARK, fade);

        Gui.drawRect(x, y, x + w, y + h, bg);

        // Subtle border when not pressed (1px darker outline)
        if (fade < 0.5f) {
            int borderAlpha = (int) ((1.0f - fade * 2.0f) * 40);
            int borderColor = (borderAlpha << 24) | 0x00FFFFFF;
            // Top
            Gui.drawRect(x, y, x + w, y + 1, borderColor);
            // Bottom
            Gui.drawRect(x, y + h - 1, x + w, y + h, borderColor);
            // Left
            Gui.drawRect(x, y, x + 1, y + h, borderColor);
            // Right
            Gui.drawRect(x + w - 1, y, x + w, y + h, borderColor);
        }

        // Center the label
        int lx = x + (w - fr.getStringWidth(label)) / 2;
        int ly = y + (h - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(label, lx, ly, fg);
    }

    /**
     * Smooth fade: fast press-in, slow release.
     */
    private float animateFade(float current, boolean pressed) {
        if (pressed) {
            current = Math.min(current + 0.25f, 1.0f);
        } else {
            current = Math.max(current - 0.06f, 0.0f);
        }
        return current;
    }

    private int interpolateColor(int c1, int c2, float factor) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void pruneOld(List<Long> clicks, long now) {
        Iterator<Long> it = clicks.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000L) {
                it.remove();
            }
        }
    }
}
