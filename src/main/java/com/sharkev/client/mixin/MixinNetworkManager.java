package com.sharkev.client.mixin;

import com.sharkev.client.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts every outgoing packet at the lowest possible level.
 * This fires BEFORE Forge's packet events, making it invisible to
 * anti-cheat mods that hook Forge events.
 *
 * Any module can listen to PacketEvent on MinecraftForge.EVENT_BUS
 * to cancel or modify packets.
 */
@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    @Inject(
        method = "sendPacket(Lnet/minecraft/network/Packet;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSendPacket(Packet packet, CallbackInfo ci) {
        PacketEvent event = new PacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        // If a replacement was set, cancel original and send replacement
        if (event.getReplacement() != null) {
            ci.cancel();
            // Cast to self to call the real method with replacement
            ((NetworkManager)(Object)this).sendPacket(event.getReplacement());
        }
    }

    @Inject(
        method = "channelRead0",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onReceivePacket(ChannelHandlerContext ctx, Packet packet, CallbackInfo ci) {
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
