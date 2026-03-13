package com.sharkev.client.module.modules.combat;

import com.sharkev.client.event.PacketEvent;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final ModeSetting mode = addMode("Mode", "Packet", "Packet", "MiniJump");

    public Criticals() {
        super("Criticals", "Packet criticals on every hit", Category.COMBAT, 0);
    }

    /**
     * Listen to outgoing C02PacketUseEntity (ATTACK action).
     * This fires for both vanilla attacks AND KillAura's packet attacks.
     * We inject the crit packets BEFORE the attack packet reaches the server
     * by sending them here and letting the original packet go through after.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketSend(PacketEvent event) {
        if (mc.thePlayer == null) return;
        if (!(event.getPacket() instanceof C02PacketUseEntity)) return;

        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        // Only trigger on ATTACK, not INTERACT
        if (packet.getAction() != C02PacketUseEntity.Action.ATTACK) return;

        // Don't crit if in water/lava or not on ground
        if (!mc.thePlayer.onGround) return;
        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) return;

        if (mode.getMode().equals("Packet")) {
            // Cancel the original packet, send crit packets first, then re-send attack
            event.setCanceled(true);

            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY;
            double z = mc.thePlayer.posZ;

            // Watchdog-safe offsets: simulate a tiny hop
            // Must go up, then down - server thinks we fell = critical
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.0625000004, z, false));
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y + 1.1E-5, z, false));
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));

            // Now send the attack packet after the crit packets
            mc.thePlayer.sendQueue.addToSendQueue(packet);

        } else if (mode.getMode().equals("MiniJump")) {
            // Cancel original, do a mini visual jump, then re-send
            event.setCanceled(true);

            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY;
            double z = mc.thePlayer.posZ;

            // Smaller offsets for mini-jump (less detectable visually)
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.04132332, z, false));
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.0023324, z, false));
            mc.thePlayer.sendQueue.addToSendQueue(
                new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));

            // Re-send attack after crit packets
            mc.thePlayer.sendQueue.addToSendQueue(packet);
        }
    }
}
