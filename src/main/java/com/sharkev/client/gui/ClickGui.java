package com.sharkev.client.gui;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.module.Module.Setting;
import com.sharkev.client.module.Module.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Premium full-screen ClickGUI for SharkevClient.
 * Dark theme with accent colors, smooth animations, and full settings support.
 */
public class ClickGui extends GuiScreen {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // ── Layout constants ──
    private static final int SIDEBAR_W = 70;
    private static final int TOP_BAR_H = 36;
    private static final int MODULE_H = 32;
    private static final int SETTING_H = 24;
    private static final int PADDING = 10;
    private static final int TOGGLE_W = 28;
    private static final int TOGGLE_H = 14;
    private static final int SMALL_TOGGLE_W = 22;
    private static final int SMALL_TOGGLE_H = 11;

    // ── Color palette ──
    private static final int COL_OVERLAY       = 0xCC0D0D1A;
    private static final int COL_BG            = 0xFF1A1A2E;
    private static final int COL_PANEL         = 0xFF16213E;
    private static final int COL_MODULE_OFF    = 0xFF0F3460;
    private static final int COL_MODULE_HOVER  = 0xFF143B6E;
    private static final int COL_ACCENT        = 0xFFE94560;
    private static final int COL_ACCENT_DIM    = 0xFFB8364D;
    private static final int COL_PURPLE        = 0xFF533483;
    private static final int COL_TEXT_PRIMARY   = 0xFFFFFFFF;
    private static final int COL_TEXT_SECONDARY = 0xFF8899AA;
    private static final int COL_TEXT_DIM       = 0xFF556677;
    private static final int COL_BORDER         = 0xFF2A2A4A;
    private static final int COL_TOGGLE_OFF     = 0xFF2A3050;
    private static final int COL_TOGGLE_ON      = 0xFFE94560;
    private static final int COL_KNOB           = 0xFFFFFFFF;
    private static final int COL_SLIDER_BG      = 0xFF1A2040;
    private static final int COL_SLIDER_FILL    = 0xFFE94560;
    private static final int COL_SETTING_BG     = 0xFF0C2A50;
    private static final int COL_SCROLLBAR_BG   = 0x22FFFFFF;
    private static final int COL_SCROLLBAR      = 0x66FFFFFF;
    private static final int COL_SIDEBAR_BG     = 0xFF111128;
    private static final int COL_SIDEBAR_SEL    = 0xFF1A1A40;
    private static final int COL_SIDEBAR_HOVER  = 0xFF151535;
    private static final int COL_EXPAND_BG      = 0xFF0B1E3A;

    // ── State ──
    private Category selectedCategory = Category.MOVEMENT;
    private final Map<Category, Float> scrollOffsets = new HashMap<>();
    private final Map<Category, Float> scrollTargets = new HashMap<>();
    private Module expandedModule = null;

    // ── Animation state ──
    private final Map<Module, Float> toggleAnims = new HashMap<>();
    private final Map<Module, Float> expandAnims = new HashMap<>();
    private final Map<Module, Float> hoverAnims = new HashMap<>();
    private final Map<String, Float> settingToggleAnims = new HashMap<>();
    private float sidebarAccentY = 0;
    private float sidebarAccentTargetY = 0;
    private float overlayAlpha = 0f;

    // ── Slider drag state ──
    private Setting<?> draggingSetting = null;
    private Module draggingModule = null;

    // ── Keybind listening ──
    private Module listeningModule = null;

    public ClickGui() {
        for (Category cat : Category.values()) {
            scrollOffsets.put(cat, 0.0f);
            scrollTargets.put(cat, 0.0f);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        overlayAlpha = 0f;
    }

    // =========================================================================
    //  DRAWING
    // =========================================================================

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fade in overlay
        if (overlayAlpha < 1f) {
            overlayAlpha += 0.08f;
            if (overlayAlpha > 1f) overlayAlpha = 1f;
        }

        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        // Full-screen dim overlay
        int overlayColor = applyAlpha(COL_OVERLAY, overlayAlpha);
        drawRect(0, 0, width, height, overlayColor);

        // ── Main content area ──
        int contentX = SIDEBAR_W;
        int contentY = TOP_BAR_H;
        int contentW = width - SIDEBAR_W;
        int contentH = height - TOP_BAR_H;

        // Background fill
        drawRect(SIDEBAR_W, TOP_BAR_H, width, height, COL_BG);

        // ── Top bar ──
        drawTopBar(fr, mouseX, mouseY);

        // ── Sidebar ──
        drawSidebar(fr, mouseX, mouseY);

        // ── Module list ──
        drawModuleList(fr, sr, mouseX, mouseY, contentX, contentY, contentW, contentH);

        // Restore GL state
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawTopBar(FontRenderer fr, int mouseX, int mouseY) {
        // Top bar background with gradient feel
        drawRect(0, 0, width, TOP_BAR_H, COL_PANEL);

        // Bottom border with accent gradient
        drawHorizontalGradient(0, TOP_BAR_H - 2, width, TOP_BAR_H, COL_ACCENT, COL_PURPLE);

        // Branding text
        String brand = "S";
        String brandRest = "harkev";
        String client = "Client";

        int brandX = SIDEBAR_W + 16;
        int brandY = (TOP_BAR_H - fr.FONT_HEIGHT) / 2;

        // Draw "S" in accent color, rest in white
        GlStateManager.pushMatrix();
        fr.drawStringWithShadow(brand, brandX, brandY, COL_ACCENT);
        int afterS = brandX + fr.getStringWidth(brand);
        fr.drawStringWithShadow(brandRest, afterS, brandY, COL_TEXT_PRIMARY);
        int afterBrandRest = afterS + fr.getStringWidth(brandRest);
        fr.drawStringWithShadow(client, afterBrandRest, brandY, COL_TEXT_SECONDARY);
        GlStateManager.popMatrix();

        // Version tag on the right
        String ver = "v1.0";
        fr.drawStringWithShadow(ver, width - fr.getStringWidth(ver) - 12,
                (TOP_BAR_H - fr.FONT_HEIGHT) / 2, COL_TEXT_DIM);

        // Selected category name centered
        String catName = selectedCategory.getDisplayName().toUpperCase();
        int catNameW = fr.getStringWidth(catName);
        int centerX = SIDEBAR_W + (width - SIDEBAR_W) / 2;
        fr.drawStringWithShadow(catName, centerX - catNameW / 2, brandY, COL_TEXT_SECONDARY);
    }

    private void drawSidebar(FontRenderer fr, int mouseX, int mouseY) {
        // Sidebar background
        drawRect(0, 0, SIDEBAR_W, height, COL_SIDEBAR_BG);

        // Right border
        drawRect(SIDEBAR_W - 1, 0, SIDEBAR_W, height, COL_BORDER);

        // Logo area at top
        int logoY = (TOP_BAR_H - fr.FONT_HEIGHT) / 2;
        String logo = "SC";
        int logoW = fr.getStringWidth(logo);
        fr.drawStringWithShadow(logo, (SIDEBAR_W - logoW) / 2, logoY, COL_ACCENT);

        // Separator under logo
        drawRect(10, TOP_BAR_H - 1, SIDEBAR_W - 10, TOP_BAR_H, COL_BORDER);

        // Category tabs
        Category[] cats = Category.values();
        int tabAreaY = TOP_BAR_H + 8;
        int tabH = 52;

        // Animated accent indicator
        int selectedIdx = 0;
        for (int i = 0; i < cats.length; i++) {
            if (cats[i] == selectedCategory) {
                selectedIdx = i;
                break;
            }
        }
        sidebarAccentTargetY = tabAreaY + selectedIdx * tabH;
        sidebarAccentY += (sidebarAccentTargetY - sidebarAccentY) * 0.25f;

        // Draw accent indicator (left bar, animated)
        drawRect(0, (int) sidebarAccentY, 3, (int) sidebarAccentY + tabH, COL_ACCENT);

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            int tabY = tabAreaY + i * tabH;
            boolean isSelected = (cat == selectedCategory);
            boolean isHovered = mouseX >= 0 && mouseX < SIDEBAR_W
                    && mouseY >= tabY && mouseY < tabY + tabH;

            // Tab background
            if (isSelected) {
                drawRect(3, tabY, SIDEBAR_W - 1, tabY + tabH, COL_SIDEBAR_SEL);
            } else if (isHovered) {
                drawRect(3, tabY, SIDEBAR_W - 1, tabY + tabH, COL_SIDEBAR_HOVER);
            }

            // Category icon circle (first letter)
            String initial = cat.getDisplayName().substring(0, 1).toUpperCase();
            int circleX = SIDEBAR_W / 2;
            int circleY = tabY + tabH / 2 - 6;
            int circleR = 12;
            int circleColor = isSelected ? COL_ACCENT : (isHovered ? COL_TEXT_SECONDARY : COL_TEXT_DIM);

            // Draw circle background using small rects (approximation)
            drawCircleFill(circleX, circleY + 3, circleR, isSelected ? 0x33E94560 : 0x22FFFFFF);

            // Draw letter
            int letterW = fr.getStringWidth(initial);
            fr.drawStringWithShadow(initial, circleX - letterW / 2, circleY, circleColor);

            // Draw category name below circle
            String catLabel = cat.getDisplayName();
            int labelW = fr.getStringWidth(catLabel);
            int labelColor = isSelected ? COL_TEXT_PRIMARY : (isHovered ? COL_TEXT_SECONDARY : COL_TEXT_DIM);
            fr.drawStringWithShadow(catLabel, (SIDEBAR_W - labelW) / 2, circleY + 16, labelColor);
        }
    }

    private void drawModuleList(FontRenderer fr, ScaledResolution sr,
                                 int mouseX, int mouseY,
                                 int areaX, int areaY, int areaW, int areaH) {
        // Smooth scroll interpolation
        float currentScroll = scrollOffsets.get(selectedCategory);
        float targetScroll = scrollTargets.get(selectedCategory);
        if (Math.abs(currentScroll - targetScroll) > 0.3f) {
            currentScroll += (targetScroll - currentScroll) * 0.3f;
        } else {
            currentScroll = targetScroll;
        }
        scrollOffsets.put(selectedCategory, currentScroll);

        List<Module> modules = SharkevClient.moduleManager.getByCategory(selectedCategory);

        // Calculate total content height
        int totalContentH = calculateTotalHeight(modules);

        // Enable scissor
        enableScissor(areaX, areaY, areaW, areaH, sr);

        int cardPadX = 12;
        int cardPadY = 8;
        int cardW = areaW - cardPadX * 2;
        int cardSpacing = 4;

        int drawY = areaY + cardPadY - (int) currentScroll;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);

            // ── Update animations ──
            float toggleTarget = m.isEnabled() ? 1.0f : 0.0f;
            float toggleCurrent = getOrDefault(toggleAnims, m, m.isEnabled() ? 1.0f : 0.0f);
            toggleCurrent = lerp(toggleCurrent, toggleTarget, 0.18f);
            toggleAnims.put(m, toggleCurrent);

            boolean isExpanded = (m == expandedModule);
            float expandTarget = isExpanded ? 1.0f : 0.0f;
            float expandCurrent = getOrDefault(expandAnims, m, 0.0f);
            expandCurrent = lerp(expandCurrent, expandTarget, 0.2f);
            if (expandCurrent < 0.01f && !isExpanded) expandCurrent = 0f;
            expandAnims.put(m, expandCurrent);

            int settingsCount = m.getSettings() != null ? m.getSettings().size() : 0;
            int expandedHeight = (int) (settingsCount * SETTING_H * expandCurrent);
            int totalCardH = MODULE_H + expandedHeight;

            // ── Visibility check ──
            if (drawY + totalCardH < areaY - 50 || drawY > areaY + areaH + 50) {
                drawY += totalCardH + cardSpacing;
                continue;
            }

            // ── Hover detection (header only) ──
            boolean isHovered = mouseX >= areaX + cardPadX && mouseX < areaX + cardPadX + cardW
                    && mouseY >= drawY && mouseY < drawY + MODULE_H
                    && mouseY >= areaY && mouseY < areaY + areaH;

            float hoverTarget = isHovered ? 1.0f : 0.0f;
            float hoverCurrent = getOrDefault(hoverAnims, m, 0.0f);
            hoverCurrent = lerp(hoverCurrent, hoverTarget, 0.2f);
            hoverAnims.put(m, hoverCurrent);

            int cardX = areaX + cardPadX;

            // ── Card background ──
            int cardBg = lerpColor(COL_MODULE_OFF, COL_MODULE_HOVER, hoverCurrent);
            drawRect(cardX, drawY, cardX + cardW, drawY + totalCardH, cardBg);

            // Left accent border when enabled
            if (toggleCurrent > 0.01f) {
                int accentBar = lerpColor(0x00E94560, COL_ACCENT, toggleCurrent);
                drawRect(cardX, drawY, cardX + 3, drawY + MODULE_H, accentBar);
            }

            // Hover left border hint
            if (hoverCurrent > 0.01f && toggleCurrent < 0.5f) {
                int hoverBorder = applyAlpha(COL_TEXT_DIM, hoverCurrent * 0.5f);
                drawRect(cardX, drawY, cardX + 2, drawY + MODULE_H, hoverBorder);
            }

            // ── Module name ──
            int nameColor = lerpColor(COL_TEXT_SECONDARY, COL_TEXT_PRIMARY, Math.max(toggleCurrent, hoverCurrent));
            fr.drawStringWithShadow(m.getName(), cardX + 12, drawY + (MODULE_H - fr.FONT_HEIGHT) / 2, nameColor);

            // ── Keybind display ──
            if (m.getKeybind() != 0) {
                String keyName = Keyboard.getKeyName(m.getKeybind());
                if (keyName == null) keyName = "?";
                if (listeningModule == m) {
                    keyName = "...";
                }
                String keyLabel = "[" + keyName + "]";
                int keyLabelW = fr.getStringWidth(keyLabel);
                int keyX = cardX + 14 + fr.getStringWidth(m.getName());
                fr.drawStringWithShadow(keyLabel, keyX, drawY + (MODULE_H - fr.FONT_HEIGHT) / 2, COL_TEXT_DIM);
            } else if (listeningModule == m) {
                fr.drawStringWithShadow("[...]", cardX + 14 + fr.getStringWidth(m.getName()),
                        drawY + (MODULE_H - fr.FONT_HEIGHT) / 2, COL_ACCENT);
            }

            // ── Expand arrow ──
            if (settingsCount > 0) {
                String arrow = expandCurrent > 0.5f ? "-" : "+";
                int arrowColor = expandCurrent > 0.5f ? COL_ACCENT : COL_TEXT_DIM;
                int arrowX = cardX + cardW - TOGGLE_W - 32;
                fr.drawStringWithShadow(arrow, arrowX, drawY + (MODULE_H - fr.FONT_HEIGHT) / 2, arrowColor);
            }

            // ── Toggle switch ──
            int toggleX = cardX + cardW - TOGGLE_W - 10;
            int toggleY = drawY + (MODULE_H - TOGGLE_H) / 2;
            drawToggleSwitch(toggleX, toggleY, TOGGLE_W, TOGGLE_H, toggleCurrent);

            // ── Bottom separator ──
            if (expandCurrent < 0.01f) {
                drawRect(cardX + 8, drawY + MODULE_H - 1, cardX + cardW - 8, drawY + MODULE_H, COL_BORDER);
            }

            // ── Expanded settings ──
            if (expandCurrent > 0.01f && m.getSettings() != null) {
                // Settings background
                drawRect(cardX, drawY + MODULE_H, cardX + cardW, drawY + MODULE_H + expandedHeight, COL_EXPAND_BG);

                // Top line separator for settings area
                drawRect(cardX + 8, drawY + MODULE_H, cardX + cardW - 8, drawY + MODULE_H + 1,
                        applyAlpha(COL_BORDER, expandCurrent));

                int settingY = drawY + MODULE_H;
                List<Setting<?>> settings = m.getSettings();

                for (int si = 0; si < settings.size(); si++) {
                    Setting<?> s = settings.get(si);
                    int sY = settingY + (int) (si * SETTING_H * expandCurrent);
                    int sH = (int) (SETTING_H * expandCurrent);

                    if (sH < 2) continue;

                    // Setting name
                    float settingAlpha = Math.min(1f, expandCurrent * 2f);
                    int settingNameColor = applyAlpha(COL_TEXT_SECONDARY, settingAlpha);
                    if (settingAlpha > 0.3f) {
                        fr.drawStringWithShadow(s.getName(), cardX + 20,
                                sY + (sH - fr.FONT_HEIGHT) / 2, settingNameColor);
                    }

                    if (s.getType() == Setting.SettingType.BOOLEAN) {
                        drawBooleanSetting(fr, s, cardX, sY, cardW, sH, mouseX, mouseY, settingAlpha);
                    } else if (s.getType() == Setting.SettingType.NUMBER) {
                        drawSliderSetting(fr, s, m, cardX, sY, cardW, sH, mouseX, mouseY, settingAlpha);
                    } else if (s.getType() == Setting.SettingType.MODE) {
                        drawModeSetting(fr, s, cardX, sY, cardW, sH, mouseX, mouseY, settingAlpha);
                    }
                }

                // Bottom border of expanded area
                drawRect(cardX + 8, drawY + MODULE_H + expandedHeight - 1,
                        cardX + cardW - 8, drawY + MODULE_H + expandedHeight,
                        applyAlpha(COL_BORDER, expandCurrent));
            }

            drawY += totalCardH + cardSpacing;
        }

        // ── Scrollbar ──
        if (totalContentH > areaH) {
            int maxScroll = totalContentH - areaH;
            float scrollPercent = maxScroll > 0 ? currentScroll / (float) maxScroll : 0;
            int barH = Math.max(20, (int) ((float) areaH / totalContentH * areaH));
            int barY = areaY + (int) (scrollPercent * (areaH - barH));
            int barX = areaX + areaW - 4;

            // Track
            drawRect(barX, areaY, barX + 3, areaY + areaH, COL_SCROLLBAR_BG);
            // Thumb
            drawRect(barX, barY, barX + 3, barY + barH, COL_SCROLLBAR);
        }

        // Disable scissor
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // =========================================================================
    //  Setting renderers
    // =========================================================================

    private void drawBooleanSetting(FontRenderer fr, Setting<?> s,
                                     int cardX, int sY, int cardW, int sH,
                                     int mouseX, int mouseY, float alpha) {
        if (alpha < 0.3f) return;

        String key = s.getName() + "_bool";
        float target = s.getBool() ? 1.0f : 0.0f;
        float current = settingToggleAnims.containsKey(key) ? settingToggleAnims.get(key) : target;
        current = lerp(current, target, 0.18f);
        settingToggleAnims.put(key, current);

        int tx = cardX + cardW - SMALL_TOGGLE_W - 16;
        int ty = sY + (sH - SMALL_TOGGLE_H) / 2;
        drawToggleSwitch(tx, ty, SMALL_TOGGLE_W, SMALL_TOGGLE_H, current);
    }

    @SuppressWarnings("unchecked")
    private void drawSliderSetting(FontRenderer fr, Setting<?> s, Module m,
                                    int cardX, int sY, int cardW, int sH,
                                    int mouseX, int mouseY, float alpha) {
        if (alpha < 0.3f) return;

        float min = (Float) s.getMin();
        float max = (Float) s.getMax();
        float val = s.getFloat();
        float percent = (val - min) / (max - min);

        int sliderX = cardX + cardW / 2 + 10;
        int sliderW = cardW / 2 - 36;
        int sliderY = sY + sH / 2 - 2;
        int sliderH = 4;

        // Slider track
        drawRect(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, COL_SLIDER_BG);

        // Slider fill
        int fillW = (int) (sliderW * percent);
        drawRect(sliderX, sliderY, sliderX + fillW, sliderY + sliderH, COL_SLIDER_FILL);

        // Handle
        int handleX = sliderX + fillW - 3;
        int handleY = sliderY - 2;
        drawRect(handleX, handleY, handleX + 6, handleY + sliderH + 4, COL_KNOB);

        // Value label
        String valStr;
        if (max - min >= 10) {
            valStr = String.format("%.0f", val);
        } else {
            valStr = String.format("%.1f", val);
        }
        int valW = fr.getStringWidth(valStr);
        fr.drawStringWithShadow(valStr, sliderX + sliderW + 6,
                sY + (sH - fr.FONT_HEIGHT) / 2, COL_TEXT_PRIMARY);

        // Handle drag
        if (draggingSetting == s) {
            float newPercent = (float) (mouseX - sliderX) / sliderW;
            newPercent = Math.max(0f, Math.min(1f, newPercent));
            float newVal = min + (max - min) * newPercent;

            // Snap to reasonable precision
            if (max - min >= 10) {
                newVal = Math.round(newVal);
            } else {
                newVal = Math.round(newVal * 10f) / 10f;
            }

            ((Setting<Float>) s).setValue(newVal);
        }
    }

    private void drawModeSetting(FontRenderer fr, Setting<?> s,
                                  int cardX, int sY, int cardW, int sH,
                                  int mouseX, int mouseY, float alpha) {
        if (alpha < 0.3f) return;

        String mode = s.getMode();
        int modeW = fr.getStringWidth(mode);

        int modeX = cardX + cardW - modeW - 16;
        int modeY = sY + (sH - fr.FONT_HEIGHT) / 2;

        // Mode button background
        boolean hovered = mouseX >= modeX - 4 && mouseX <= modeX + modeW + 4
                && mouseY >= modeY - 2 && mouseY <= modeY + fr.FONT_HEIGHT + 2;

        int modeBg = hovered ? COL_MODULE_HOVER : COL_SETTING_BG;
        drawRect(modeX - 4, modeY - 2, modeX + modeW + 4, modeY + fr.FONT_HEIGHT + 2, modeBg);

        // Mode text
        int modeColor = hovered ? COL_ACCENT : COL_TEXT_PRIMARY;
        fr.drawStringWithShadow(mode, modeX, modeY, modeColor);

        // Left/right arrows
        fr.drawStringWithShadow("<", modeX - 12, modeY, COL_TEXT_DIM);
        fr.drawStringWithShadow(">", modeX + modeW + 6, modeY, COL_TEXT_DIM);
    }

    // =========================================================================
    //  Toggle switch drawing
    // =========================================================================

    private void drawToggleSwitch(int x, int y, int w, int h, float progress) {
        // Track background with color interpolation
        int trackColor = lerpColor(COL_TOGGLE_OFF, COL_TOGGLE_ON, progress);
        drawRect(x, y, x + w, y + h, trackColor);

        // Rounded ends simulation (1px insets)
        drawRect(x + 1, y - 1, x + w - 1, y, trackColor);
        drawRect(x + 1, y + h, x + w - 1, y + h + 1, trackColor);

        // Knob
        int knobSize = h - 4;
        int knobMinX = x + 2;
        int knobMaxX = x + w - knobSize - 2;
        int knobX = (int) (knobMinX + (knobMaxX - knobMinX) * progress);
        int knobY = y + 2;

        drawRect(knobX, knobY, knobX + knobSize, knobY + knobSize, COL_KNOB);

        // Knob highlight (subtle inner)
        if (knobSize > 4) {
            drawRect(knobX + 1, knobY + 1, knobX + knobSize - 1, knobY + 2, 0x22FFFFFF);
        }
    }

    // =========================================================================
    //  Drawing helpers
    // =========================================================================

    private void drawHorizontalGradient(int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        int w = x2 - x1;
        if (w <= 0) return;

        // For thin bars, use fewer steps for performance
        int steps = Math.min(w, 64);
        int stepWidth = Math.max(1, w / steps);

        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int c = lerpColor(colorLeft, colorRight, t);
            int sx = x1 + (int) (w * t);
            int ex = Math.min(x1 + (int) (w * (t + 1.0f / steps)), x2);
            drawRect(sx, y1, ex, y2, c);
        }
    }

    private void drawCircleFill(int cx, int cy, int radius, int color) {
        // Approximate circle with horizontal lines
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            drawRect(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    // =========================================================================
    //  Scissor
    // =========================================================================

    private void enableScissor(int x, int y, int w, int h, ScaledResolution sr) {
        int sf = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * sf, mc.displayHeight - (y + h) * sf, w * sf, h * sf);
    }

    // =========================================================================
    //  Color utilities
    // =========================================================================

    private static int lerpColor(int c1, int c2, float t) {
        if (t <= 0f) return c1;
        if (t >= 1f) return c2;
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static float lerp(float current, float target, float speed) {
        if (Math.abs(current - target) < 0.005f) return target;
        return current + (target - current) * speed;
    }

    private static float getOrDefault(Map<Module, Float> map, Module key, float def) {
        Float val = map.get(key);
        return val != null ? val : def;
    }

    // =========================================================================
    //  Content height calculation
    // =========================================================================

    private int calculateTotalHeight(List<Module> modules) {
        int total = 16; // top + bottom padding
        for (Module m : modules) {
            total += MODULE_H + 4; // card + spacing
            if (m == expandedModule && m.getSettings() != null) {
                total += m.getSettings().size() * SETTING_H;
            }
        }
        return total;
    }

    // =========================================================================
    //  MOUSE INPUT
    // =========================================================================

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        // ── Sidebar clicks ──
        if (mouseX >= 0 && mouseX < SIDEBAR_W) {
            Category[] cats = Category.values();
            int tabAreaY = TOP_BAR_H + 8;
            int tabH = 52;

            for (int i = 0; i < cats.length; i++) {
                int tabY = tabAreaY + i * tabH;
                if (mouseY >= tabY && mouseY < tabY + tabH) {
                    if (selectedCategory != cats[i]) {
                        selectedCategory = cats[i];
                        expandedModule = null;
                        listeningModule = null;
                    }
                    return;
                }
            }
            return;
        }

        // ── Module list area ──
        int areaX = SIDEBAR_W;
        int areaY = TOP_BAR_H;
        int areaW = width - SIDEBAR_W;
        int areaH = height - TOP_BAR_H;

        if (mouseX < areaX || mouseX >= areaX + areaW || mouseY < areaY || mouseY >= areaY + areaH) {
            return;
        }

        float currentScroll = scrollOffsets.get(selectedCategory);
        List<Module> modules = SharkevClient.moduleManager.getByCategory(selectedCategory);

        int cardPadX = 12;
        int cardPadY = 8;
        int cardW = areaW - cardPadX * 2;
        int cardSpacing = 4;
        int drawY = areaY + cardPadY - (int) currentScroll;

        for (Module m : modules) {
            int settingsCount = m.getSettings() != null ? m.getSettings().size() : 0;
            float expandCurrent = getOrDefault(expandAnims, m, m == expandedModule ? 1f : 0f);
            int expandedHeight = (int) (settingsCount * SETTING_H * expandCurrent);
            int totalCardH = MODULE_H + expandedHeight;
            int cardX = areaX + cardPadX;

            // ── Click on module header ──
            if (mouseY >= drawY && mouseY < drawY + MODULE_H
                    && mouseX >= cardX && mouseX < cardX + cardW
                    && mouseY >= areaY && mouseY < areaY + areaH) {

                int toggleX = cardX + cardW - TOGGLE_W - 10;
                int toggleY = drawY + (MODULE_H - TOGGLE_H) / 2;

                if (button == 0) {
                    // Left click on toggle switch
                    if (mouseX >= toggleX && mouseX <= toggleX + TOGGLE_W
                            && mouseY >= toggleY && mouseY <= toggleY + TOGGLE_H) {
                        m.toggle();
                        return;
                    }

                    // Left click on expand area
                    if (settingsCount > 0) {
                        expandedModule = (expandedModule == m) ? null : m;
                    } else {
                        // No settings - just toggle
                        m.toggle();
                    }
                    listeningModule = null;
                    return;
                } else if (button == 1) {
                    // Right click - start keybind listening
                    listeningModule = (listeningModule == m) ? null : m;
                    return;
                }
            }

            // ── Click on expanded settings ──
            if (expandCurrent > 0.5f && m.getSettings() != null && m == expandedModule) {
                int settingY = drawY + MODULE_H;
                List<Setting<?>> settings = m.getSettings();

                for (int si = 0; si < settings.size(); si++) {
                    Setting<?> s = settings.get(si);
                    int sY = settingY + si * SETTING_H;
                    int sH = SETTING_H;

                    if (mouseY >= sY && mouseY < sY + sH
                            && mouseX >= cardX && mouseX < cardX + cardW
                            && mouseY >= areaY && mouseY < areaY + areaH) {

                        if (s.getType() == Setting.SettingType.BOOLEAN && button == 0) {
                            @SuppressWarnings("unchecked")
                            Setting<Boolean> boolSetting = (Setting<Boolean>) s;
                            boolSetting.setValue(!boolSetting.getBool());
                            return;
                        } else if (s.getType() == Setting.SettingType.NUMBER && button == 0) {
                            // Start slider drag
                            int sliderX = cardX + cardW / 2 + 10;
                            int sliderW = cardW / 2 - 36;
                            if (mouseX >= sliderX - 4 && mouseX <= sliderX + sliderW + 4) {
                                draggingSetting = s;
                                draggingModule = m;
                                // Apply initial value
                                handleSliderDrag(s, mouseX, sliderX, sliderW);
                                return;
                            }
                        } else if (s.getType() == Setting.SettingType.MODE && button == 0) {
                            if (s instanceof ModeSetting) {
                                ((ModeSetting) s).cycle();
                                return;
                            }
                        }
                    }
                }
            }

            drawY += totalCardH + cardSpacing;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSliderDrag(Setting<?> s, int mouseX, int sliderX, int sliderW) {
        float min = (Float) s.getMin();
        float max = (Float) s.getMax();
        float percent = (float) (mouseX - sliderX) / sliderW;
        percent = Math.max(0f, Math.min(1f, percent));
        float newVal = min + (max - min) * percent;

        if (max - min >= 10) {
            newVal = Math.round(newVal);
        } else {
            newVal = Math.round(newVal * 10f) / 10f;
        }

        ((Setting<Float>) s).setValue(newVal);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingSetting = null;
        draggingModule = null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        // Handle slider dragging via mouse move
        if (draggingSetting != null && draggingModule != null && Mouse.isButtonDown(0)) {
            int mx = Mouse.getEventX() * width / mc.displayWidth;
            // Recalculate slider position - this is approximate but works
            int areaX = SIDEBAR_W;
            int areaW = width - SIDEBAR_W;
            int cardPadX = 12;
            int cardW = areaW - cardPadX * 2;
            int cardX = areaX + cardPadX;
            int sliderX = cardX + cardW / 2 + 10;
            int sliderW = cardW / 2 - 36;
            handleSliderDrag(draggingSetting, mx, sliderX, sliderW);
        }

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mx = Mouse.getEventX() * width / mc.displayWidth;
            int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;

            int areaX = SIDEBAR_W;
            int areaY = TOP_BAR_H;
            int areaW = width - SIDEBAR_W;
            int areaH = height - TOP_BAR_H;

            if (mx >= areaX && mx < areaX + areaW && my >= areaY && my < areaY + areaH) {
                List<Module> modules = SharkevClient.moduleManager.getByCategory(selectedCategory);
                int totalContentH = calculateTotalHeight(modules);

                int maxScroll = Math.max(0, totalContentH - areaH);
                float target = scrollTargets.get(selectedCategory);
                target -= scroll > 0 ? MODULE_H * 2 : -(MODULE_H * 2);
                target = Math.max(0, Math.min(target, maxScroll));
                scrollTargets.put(selectedCategory, target);
            }
        }
    }

    // =========================================================================
    //  KEY INPUT
    // =========================================================================

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listeningModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                // Clear keybind
                listeningModule.setKeybind(0);
                listeningModule = null;
                return;
            } else if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                // Remove keybind
                listeningModule.setKeybind(0);
                listeningModule = null;
                return;
            } else {
                listeningModule.setKeybind(keyCode);
                listeningModule = null;
                return;
            }
        }

        // Default ESC handling
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
