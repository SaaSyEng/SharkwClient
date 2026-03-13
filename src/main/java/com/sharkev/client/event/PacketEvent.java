package com.sharkev.client.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired by MixinNetworkManager before every outgoing packet.
 * Cancel it to silently drop the packet.
 * Modify packet fields before cancelling to replace it.
 */
@Cancelable
public class PacketEvent extends Event {

    private Packet packet;
    private Packet replacement; // if non-null, send this instead

    public PacketEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket()                      { return packet; }
    public void   setPacket(Packet p)              { this.packet = p; }
    public Packet getReplacement()                 { return replacement; }
    public void   setReplacement(Packet p)         { this.replacement = p; }

    /**
     * Fired by MixinNetworkManager before every incoming packet.
     * Cancel it to silently drop the packet.
     */
    public static class Receive extends Event {
        private final Packet packet;
        public Receive(Packet packet) { this.packet = packet; }
        public Packet getPacket() { return packet; }
        @Override public boolean isCancelable() { return true; }
    }
}
