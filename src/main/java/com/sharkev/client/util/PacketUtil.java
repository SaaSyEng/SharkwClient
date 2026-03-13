package com.sharkev.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class PacketUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Send a packet directly to the server.
     */
    public static void send(Packet p) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.sendQueue.addToSendQueue(p);
    }

    /**
     * Spoof onGround=true for the next position packet.
     */
    public static void sendGroundSpoof() {
        if (mc.thePlayer == null) return;
        send(new C03PacketPlayer(true));
    }

    /**
     * Send position without modifying actual client position.
     */
    public static void sendPosition(double x, double y, double z, boolean onGround) {
        send(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, onGround));
    }

    /**
     * Send rotation to server using C06PacketPlayerPosLook (position + look).
     * This is the correct packet for silent rotations - servers validate
     * rotations against position, so sending both ensures consistency.
     */
    public static void sendRotation(float yaw, float pitch, boolean onGround) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.sendQueue.addToSendQueue(
            new C03PacketPlayer.C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.getEntityBoundingBox().minY,
                mc.thePlayer.posZ,
                yaw,
                pitch,
                onGround
            )
        );
    }

    /**
     * Send full position + rotation packet.
     */
    public static void sendPosLook(double x, double y, double z,
                                    float yaw, float pitch, boolean onGround) {
        send(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, yaw, pitch, onGround));
    }

    /**
     * Simulate a micro-jump via packets for packet criticals.
     * Server sees the player as airborne for one tick -> registers as critical hit.
     */
    public static void sendCritPackets() {
        if (mc.thePlayer == null) return;
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        sendPosition(x, y + 0.0625, z, false);
        sendPosition(x, y + 0.03125, z, false);
        sendPosition(x, y, z, false);
        sendPosition(x, y - 0.0625, z, false);
    }
}
