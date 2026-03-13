package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoFall extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeSetting mode = addMode("Mode", "Packet", "Packet", "NoOP");

    public NoFall() {
        super("NoFall", "Cancels fall damage", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.fallDistance > 2.5f) {
            String currentMode = mode.getMode();

            if (currentMode.equals("Packet")) {
                // Spoof onGround=true in the packet when falling to cancel fall damage
                mc.thePlayer.sendQueue.addToSendQueue(
                    new C03PacketPlayer(true)
                );
            } else if (currentMode.equals("NoOP")) {
                // Simply reset fall distance client-side; server may still apply damage
                // but this tests whether the server validates fall distance independently
                mc.thePlayer.fallDistance = 0;
            }
        }
    }
}
