package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Phase / NoClip module.
 * Note: This is primarily client-side. Servers with proper collision validation
 * will rubberband the player back. Useful for testing server-side collision checks.
 */
public class Phase extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeSetting mode = addMode("Mode", "Vanilla", "Vanilla", "Packet");

    public Phase() {
        super("Phase", "Clip through blocks (noclip)", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        String currentMode = mode.getMode();

        if (currentMode.equals("Vanilla")) {
            // Disable collision detection for this entity
            mc.thePlayer.noClip = true;

            // Also disable for all parts of the model
            for (Entity part : mc.thePlayer.getParts()) {
                if (part != null) part.noClip = true;
            }
        } else if (currentMode.equals("Packet")) {
            // Packet mode: allow movement through blocks by disabling collision
            // and sending spoofed position packets
            mc.thePlayer.noClip = true;

            // Send current position as valid to try to bypass server-side checks
            PacketUtil.sendPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.onGround
            );
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.noClip = false;
        }
    }
}
