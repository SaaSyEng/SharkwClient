package com.sharkev.client.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    /**
     * Draw a filled rounded rectangle using GL11 TRIANGLE_FAN.
     */
    public static void drawRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        // Center point for the fan
        float cx = (x + x2) / 2f;
        float cy = (y + y2) / 2f;
        GL11.glVertex2f(cx, cy);

        int segments = 8;
        // Top-left corner
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (Math.PI + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x + radius + (float) Math.cos(angle) * radius,
                    y + radius + (float) Math.sin(angle) * radius);
        }
        // Top-right corner
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (3 * Math.PI / 2.0 + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x2 - radius + (float) Math.cos(angle) * radius,
                    y + radius + (float) Math.sin(angle) * radius);
        }
        // Bottom-right corner
        for (int i = 0; i <= segments; i++) {
            float angle = (float) ((Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x2 - radius + (float) Math.cos(angle) * radius,
                    y2 - radius + (float) Math.sin(angle) * radius);
        }
        // Bottom-left corner
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (Math.PI / 2.0 + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x + radius + (float) Math.cos(angle) * radius,
                    y2 - radius + (float) Math.sin(angle) * radius);
        }
        // Close back to first point
        GL11.glVertex2f(x, y + radius);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draw a rounded rectangle outline.
     */
    public static void drawRoundedRectOutline(float x, float y, float x2, float y2, float radius, float lineWidth, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        int segments = 8;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (Math.PI + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x + radius + (float) Math.cos(angle) * radius,
                    y + radius + (float) Math.sin(angle) * radius);
        }
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (3 * Math.PI / 2.0 + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x2 - radius + (float) Math.cos(angle) * radius,
                    y + radius + (float) Math.sin(angle) * radius);
        }
        for (int i = 0; i <= segments; i++) {
            float angle = (float) ((Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x2 - radius + (float) Math.cos(angle) * radius,
                    y2 - radius + (float) Math.sin(angle) * radius);
        }
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (Math.PI / 2.0 + (Math.PI / 2.0) * i / segments);
            GL11.glVertex2f(x + radius + (float) Math.cos(angle) * radius,
                    y2 - radius + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draw a filled circle.
     */
    public static void drawCircle(float cx, float cy, float radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 32; i++) {
            float angle = (float) (2 * Math.PI * i / 32);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * radius,
                    cy + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draw a horizontal gradient rect.
     */
    public static void drawGradientRectH(float x, float y, float x2, float y2, int leftColor, int rightColor) {
        float a1 = (leftColor >> 24 & 0xFF) / 255.0F;
        float r1 = (leftColor >> 16 & 0xFF) / 255.0F;
        float g1 = (leftColor >> 8 & 0xFF) / 255.0F;
        float b1 = (leftColor & 0xFF) / 255.0F;

        float a2 = (rightColor >> 24 & 0xFF) / 255.0F;
        float r2 = (rightColor >> 16 & 0xFF) / 255.0F;
        float g2 = (rightColor >> 8 & 0xFF) / 255.0F;
        float b2 = (rightColor & 0xFF) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(r1, g1, b1, a1);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y2);
        GL11.glColor4f(r2, g2, b2, a2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y);
        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draw a vertical gradient rect.
     */
    public static void drawGradientRectV(float x, float y, float x2, float y2, int topColor, int bottomColor) {
        float a1 = (topColor >> 24 & 0xFF) / 255.0F;
        float r1 = (topColor >> 16 & 0xFF) / 255.0F;
        float g1 = (topColor >> 8 & 0xFF) / 255.0F;
        float b1 = (topColor & 0xFF) / 255.0F;

        float a2 = (bottomColor >> 24 & 0xFF) / 255.0F;
        float r2 = (bottomColor >> 16 & 0xFF) / 255.0F;
        float g2 = (bottomColor >> 8 & 0xFF) / 255.0F;
        float b2 = (bottomColor & 0xFF) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(r1, g1, b1, a1);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x2, y);
        GL11.glColor4f(r2, g2, b2, a2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x, y2);
        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draw a drop shadow behind a rectangle.
     */
    public static void drawShadow(float x, float y, float x2, float y2, int layers) {
        for (int i = layers; i > 0; i--) {
            int alpha = 30 / i;
            drawRoundedRect(x - i, y - i, x2 + i, y2 + i, 4 + i,
                    (alpha << 24));
        }
    }

    /**
     * Interpolate between two colors.
     */
    public static int interpolateColor(int color1, int color2, float factor) {
        factor = Math.max(0, Math.min(1, factor));
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Get a rainbow color based on offset and speed.
     */
    public static int getRainbow(float offset, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f + offset;
        return java.awt.Color.HSBtoRGB(hue % 1.0f, saturation, brightness);
    }

    /**
     * Easing functions for smooth animations.
     */
    public static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    /**
     * Apply scissor test (for scroll clipping). Accounts for GUI scale.
     */
    public static void enableScissor(int x, int y, int width, int height) {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
                net.minecraft.client.Minecraft.getMinecraft());
        int scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale,
                net.minecraft.client.Minecraft.getMinecraft().displayHeight - (y + height) * scale,
                width * scale,
                height * scale);
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
