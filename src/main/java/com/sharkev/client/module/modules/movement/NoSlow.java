package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoSlow extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> percentage = addSlider("Percentage", 100f, 0f, 100f);

    private boolean wasUsingItem = false;

    public NoSlow() {
        super("NoSlow", "No slowdown when blocking/eating/charging bow", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) {
            wasUsingItem = false;
            return;
        }

        boolean isSlowing = mc.thePlayer.isUsingItem() && (
            held.getItem() instanceof ItemSword  // blocking
         || held.getItem() instanceof ItemFood   // eating
         || held.getItem() instanceof ItemBow    // charging
        );

        if (isSlowing) {
            if (!wasUsingItem) {
                // Send C07 (release) at start of item use to cancel server-side slow
                mc.thePlayer.sendQueue.addToSendQueue(
                    new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                    )
                );
            }

            // Apply percentage-based slow reduction
            float pct = percentage.getFloat() / 100f;
            if (pct > 0) {
                // Vanilla item-use multiplier is 0.2; we interpolate toward 1.0
                // motionX/Z *= 0.2 is vanilla slow; we scale it back up
                float slowFactor = 0.2f + (1.0f - 0.2f) * pct;
                mc.thePlayer.motionX *= slowFactor / 0.2f;
                mc.thePlayer.motionZ *= slowFactor / 0.2f;
            }

            wasUsingItem = true;
        } else {
            if (wasUsingItem) {
                // Send C08 at end of item use to re-sync with server
                mc.thePlayer.sendQueue.addToSendQueue(
                    new C08PacketPlayerBlockPlacement(
                        new BlockPos(-1, -1, -1),
                        255,
                        mc.thePlayer.getHeldItem(),
                        0f, 0f, 0f
                    )
                );
            }
            wasUsingItem = false;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        wasUsingItem = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        wasUsingItem = false;
    }
}
