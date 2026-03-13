package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoRespawn extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> delay = addSlider("Delay", 5f, 0f, 20f);

    private int deathTicks = 0;
    private boolean waitingToRespawn = false;

    public AutoRespawn() {
        super("AutoRespawn", "Auto click respawn on death screen", Category.MISC, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        if (mc.currentScreen instanceof GuiGameOver) {
            if (!waitingToRespawn) {
                waitingToRespawn = true;
                deathTicks = 0;
            }

            deathTicks++;

            if (deathTicks >= (int) delay.getFloat()) {
                mc.thePlayer.sendQueue.addToSendQueue(
                    new C16PacketClientStatus(
                        C16PacketClientStatus.EnumState.PERFORM_RESPAWN
                    )
                );
                mc.displayGuiScreen(null);
                waitingToRespawn = false;
                deathTicks = 0;
            }
        } else {
            waitingToRespawn = false;
            deathTicks = 0;
        }
    }
}
