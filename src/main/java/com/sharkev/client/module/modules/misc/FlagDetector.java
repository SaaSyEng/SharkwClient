package com.sharkev.client.module.modules.misc;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.event.PacketEvent;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Passive module that monitors for signs the AC is actively checking the player.
 * When enough flags accumulate, enters safe mode and reduces intensity on
 * combat, movement, and misc modules.
 *
 * Signals monitored:
 *  - Sudden teleport (rubber-banding) = server rejected movement
 *  - Chat messages containing AC flag keywords
 */
public class FlagDetector extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> sensitivity = addSlider("Sensitivity", 3f, 1f, 10f);
    private final Setting<Float> safeModeDuration = addSlider("Safe Duration", 10f, 5f, 30f);

    private int flagCount     = 0;
    private int safeModeTimer = 0;
    private boolean inSafeMode = false;
    private int tickCounter = 0;

    // Positions for rubber-band detection
    private double lastX, lastY, lastZ;

    // Saved setting values to restore after safe mode
    private final List<SettingBackup> savedSettings = new ArrayList<>();

    // AC flag keywords to watch for in chat
    private static final String[] FLAG_KEYWORDS = {
        "banned", "kicked", "cheat", "hack", "illegal", "violation",
        "warning", "staff", "suspicious", "flagged", "detected",
        "anticheat", "watchdog", "alert", "freeze"
    };

    public FlagDetector() {
        super("FlagDetector", "Reduces module intensity when AC activity detected", Category.MISC, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        flagCount = 0;
        safeModeTimer = 0;
        inSafeMode = false;
        tickCounter = 0;
        savedSettings.clear();
        if (mc.thePlayer != null) {
            lastX = mc.thePlayer.posX;
            lastY = mc.thePlayer.posY;
            lastZ = mc.thePlayer.posZ;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        tickCounter++;

        // Decay safe mode timer
        if (inSafeMode) {
            safeModeTimer--;
            if (safeModeTimer <= 0) {
                exitSafeMode();
            }
        }

        // Decay flag count slowly - every 100 ticks instead of every tick
        if (!inSafeMode && flagCount > 0 && tickCounter % 100 == 0) {
            flagCount = Math.max(0, flagCount - 1);
        }

        // Check for rubber-banding (server teleported us back)
        double dx = Math.abs(mc.thePlayer.posX - lastX);
        double dz = Math.abs(mc.thePlayer.posZ - lastZ);
        // Lower threshold: 1.5 blocks in one tick without significant vertical motion = rubber band
        if ((dx > 1.5 || dz > 1.5) && Math.abs(mc.thePlayer.motionY) < 1.0) {
            addFlag("RubberBand detected (dx=" + String.format("%.1f", dx) + " dz=" + String.format("%.1f", dz) + ")");
        }

        lastX = mc.thePlayer.posX;
        lastY = mc.thePlayer.posY;
        lastZ = mc.thePlayer.posZ;

        // Check if flagCount reached threshold
        int threshold = (int) sensitivity.getFloat();
        if (flagCount >= threshold && !inSafeMode) {
            enterSafeMode();
        }
    }

    /**
     * Monitor incoming chat messages for anticheat keywords.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.thePlayer == null) return;
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            String message = chatPacket.getChatComponent().getUnformattedText().toLowerCase();
            for (String keyword : FLAG_KEYWORDS) {
                if (message.contains(keyword)) {
                    addFlag("Chat keyword: \"" + keyword + "\"");
                    break; // only flag once per message
                }
            }
        }
    }

    public void addFlag(String reason) {
        flagCount++;
        int threshold = (int) sensitivity.getFloat();
        mc.thePlayer.addChatMessage(
            new ChatComponentText("[SC] Flag detected: " + reason + " (" + flagCount + "/" + threshold + ")")
        );
    }

    private void enterSafeMode() {
        inSafeMode    = true;
        int durationSeconds = (int) safeModeDuration.getFloat();
        safeModeTimer = durationSeconds * 20; // convert seconds to ticks

        mc.thePlayer.addChatMessage(
            new ChatComponentText("[SC] SAFE MODE: Reducing module intensity for " + durationSeconds + "s")
        );

        savedSettings.clear();
        reduceCombatIntensity();
        reduceMovementIntensity();
    }

    private void exitSafeMode() {
        inSafeMode = false;
        flagCount  = 0;

        // Restore all saved settings
        for (SettingBackup backup : savedSettings) {
            backup.restore();
        }
        savedSettings.clear();

        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(
                new ChatComponentText("[SC] Safe mode ended, settings restored to original values")
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void reduceCombatIntensity() {
        // KillAura - reduce range
        reduceFloatSetting("KillAura", "Range", 3.0f);
        // KillAura - reduce CPS
        reduceFloatSetting("KillAura", "Max CPS", 10f);
        reduceFloatSetting("KillAura", "Min CPS", 7f);
        // AimAssist - reduce range and speed
        reduceFloatSetting("AimAssist", "Range", 3.5f);
        reduceFloatSetting("AimAssist", "Speed", 0.2f);
        // Reach - reduce if present
        reduceFloatSetting("Reach", "Range", 3.0f);
    }

    @SuppressWarnings("unchecked")
    private void reduceMovementIntensity() {
        // Speed - reduce multiplier
        reduceFloatSetting("Speed", "Multiplier", 1.0f);
        // Fly - reduce speed
        reduceFloatSetting("Fly", "Speed", 0.8f);
        // Bhop - reduce speed
        reduceFloatSetting("Bhop", "Speed", 0.8f);
    }

    /**
     * Finds a float setting on a module by name, saves its current value,
     * and sets it to the safe value (only if the safe value is lower).
     */
    @SuppressWarnings("unchecked")
    private void reduceFloatSetting(String moduleName, String settingName, float safeValue) {
        Module mod = SharkevClient.moduleManager.getByName(moduleName);
        if (mod == null || !mod.isEnabled()) return;

        for (Setting<?> s : mod.getSettings()) {
            if (s.getName().equals(settingName) && s.getType() == Setting.SettingType.NUMBER) {
                Setting<Float> fs = (Setting<Float>) s;
                float original = fs.getFloat();
                // Only reduce, never increase
                if (safeValue < original) {
                    savedSettings.add(new SettingBackup(fs, original));
                    fs.setValue(safeValue);
                }
                return;
            }
        }
    }

    public boolean isInSafeMode()  { return inSafeMode; }
    public int     getFlagCount()  { return flagCount; }

    /**
     * Helper class to store original setting values for restoration.
     */
    private static class SettingBackup {
        private final Setting<Float> setting;
        private final float originalValue;

        SettingBackup(Setting<Float> setting, float originalValue) {
            this.setting = setting;
            this.originalValue = originalValue;
        }

        void restore() {
            setting.setValue(originalValue);
        }
    }
}
