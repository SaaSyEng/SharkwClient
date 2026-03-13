package com.sharkev.client.module.modules.combat;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WTap extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final ModeSetting mode = addMode("Mode", "WTap", "WTap", "STap");
    private final Setting<Float> delay = addSlider("Delay", 2f, 1f, 5f);

    private boolean shouldReSprint = false;
    private int reSprintTimer = 0;
    private int sTapDir = 1;

    public WTap() {
        super("WTap", "Auto W-tap/S-tap for maximum knockback on hit", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.entityPlayer != mc.thePlayer) return;
        doTap();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        // Handle re-sprint after delay
        if (shouldReSprint) {
            reSprintTimer--;
            if (reSprintTimer <= 0) {
                mc.thePlayer.sendQueue.addToSendQueue(
                    new C0BPacketEntityAction(mc.thePlayer,
                        C0BPacketEntityAction.Action.START_SPRINTING)
                );
                mc.thePlayer.setSprinting(true);
                shouldReSprint = false;
            }
        }

        // Check if KillAura actually attacked this tick (not just has a target)
        if (!shouldReSprint) {
            KillAura ka = (KillAura) SharkevClient.moduleManager.getByName("KillAura");
            if (ka != null && ka.isEnabled() && ka.didAttackThisTick()) {
                doTap();
            }
        }
    }

    private void doTap() {
        if (!mc.thePlayer.isSprinting()) return;

        if (mode.getMode().equals("WTap")) {
            // W-tap: stop sprinting on hit frame -> re-sprint after delay
            mc.thePlayer.sendQueue.addToSendQueue(
                new C0BPacketEntityAction(mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SPRINTING)
            );
            mc.thePlayer.setSprinting(false);
            shouldReSprint = true;
            reSprintTimer = (int) delay.getFloat();

        } else if (mode.getMode().equals("STap")) {
            // S-tap: briefly move backward for one tick then forward again
            // This causes a sprint reset that maximizes knockback
            mc.thePlayer.sendQueue.addToSendQueue(
                new C0BPacketEntityAction(mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SPRINTING)
            );
            mc.thePlayer.setSprinting(false);
            shouldReSprint = true;
            reSprintTimer = (int) delay.getFloat();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        shouldReSprint = false;
        reSprintTimer = 0;
    }
}
