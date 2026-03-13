package com.sharkev.client.module.modules.combat;

import com.sharkev.client.event.PacketEvent;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public class Velocity extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> horizontal = addSlider("Horizontal %", 0f, 0f, 100f);
    private final Setting<Float> vertical = addSlider("Vertical %", 0f, 0f, 100f);
    private final ModeSetting mode = addMode("Mode", "Cancel", "Cancel", "Reduce");

    // Cached reflection fields for S12PacketEntityVelocity
    private static Field motionXField;
    private static Field motionYField;
    private static Field motionZField;

    static {
        try {
            // Try MCP mapped names first
            motionXField = S12PacketEntityVelocity.class.getDeclaredField("motionX");
            motionYField = S12PacketEntityVelocity.class.getDeclaredField("motionY");
            motionZField = S12PacketEntityVelocity.class.getDeclaredField("motionZ");
        } catch (Exception e) {
            try {
                // SRG names
                motionXField = S12PacketEntityVelocity.class.getDeclaredField("field_149415_b");
                motionYField = S12PacketEntityVelocity.class.getDeclaredField("field_149416_c");
                motionZField = S12PacketEntityVelocity.class.getDeclaredField("field_149414_d");
            } catch (Exception ignored) {}
        }
        if (motionXField != null) motionXField.setAccessible(true);
        if (motionYField != null) motionYField.setAccessible(true);
        if (motionZField != null) motionZField.setAccessible(true);
    }

    public Velocity() {
        super("Velocity", "Cancel/reduce knockback", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.thePlayer == null) return;

        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;

            float hPct = horizontal.getFloat() / 100f;
            float vPct = vertical.getFloat() / 100f;

            if (mode.getMode().equals("Cancel") || (hPct == 0f && vPct == 0f)) {
                // Full cancel
                event.setCanceled(true);
            } else if (mode.getMode().equals("Reduce")) {
                // Modify packet velocity values via reflection
                if (motionXField != null && motionYField != null && motionZField != null) {
                    try {
                        int origX = motionXField.getInt(packet);
                        int origY = motionYField.getInt(packet);
                        int origZ = motionZField.getInt(packet);

                        motionXField.setInt(packet, (int) (origX * hPct));
                        motionYField.setInt(packet, (int) (origY * vPct));
                        motionZField.setInt(packet, (int) (origZ * hPct));
                    } catch (Exception e) {
                        // Reflection failed, fall back to cancel
                        event.setCanceled(true);
                    }
                } else {
                    // Fields not found, fall back to cancel
                    event.setCanceled(true);
                }
            }
        }

        if (event.getPacket() instanceof S27PacketExplosion) {
            float hPct = horizontal.getFloat() / 100f;
            float vPct = vertical.getFloat() / 100f;
            if (hPct == 0f && vPct == 0f) {
                event.setCanceled(true);
            }
            // Explosion packets are less common; cancel is fine for non-zero too
            // since partial explosion KB is rarely useful
        }
    }
}
