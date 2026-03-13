package com.sharkev.client.module.modules.misc;

import com.sharkev.client.event.PacketEvent;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Blink - Intercepts all outgoing C03PacketPlayer packets (position, look, pos+look, ground)
 * via the PacketEvent system. Stores them in a buffer and sends them all at once on disable.
 * This properly prevents vanilla packets from reaching the server while active.
 */
public class Blink extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> maxPackets = addSlider("Max Packets", 200f, 50f, 500f);

    private final List<Packet> buffer = new ArrayList<>();
    private boolean flushing = false;

    public Blink() {
        super("Blink", "Buffer position packets then send all at once", Category.MISC, 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        buffer.clear();
        flushing = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent event) {
        if (mc.thePlayer == null) return;
        // Don't intercept packets we're flushing
        if (flushing) return;

        if (event.getPacket() instanceof C03PacketPlayer) {
            event.setCanceled(true);
            buffer.add(event.getPacket());

            // Auto-disable if buffer is too large to prevent server timeout
            if (buffer.size() >= (int) maxPackets.getFloat()) {
                toggle();
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        flush();
    }

    private void flush() {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            buffer.clear();
            return;
        }
        flushing = true;
        for (Packet p : buffer) {
            mc.thePlayer.sendQueue.addToSendQueue(p);
        }
        buffer.clear();
        flushing = false;
    }

    public int getBufferSize() {
        return buffer.size();
    }
}
