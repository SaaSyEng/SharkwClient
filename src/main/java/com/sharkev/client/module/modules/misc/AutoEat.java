package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoEat extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> hungerThreshold = addSlider("Hunger Threshold", 14f, 1f, 19f);
    private final Setting<Float> delay = addSlider("Delay", 0f, 0f, 10f);

    private int prevSlot = -1;
    private boolean eating = false;
    private int eatTimer = 0;
    private int delayCounter = 0;
    private int lastDamageTick = 0;

    // Vanilla eating time is 32 ticks (1.6 seconds)
    private static final int EAT_DURATION = 32;
    // Don't eat if damaged within this many ticks (combat check)
    private static final int COMBAT_COOLDOWN = 40;

    public AutoEat() {
        super("AutoEat", "Auto eat food when hunger is low", Category.MISC, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        // Track damage for combat detection
        if (mc.thePlayer.hurtTime > 0) {
            lastDamageTick = mc.thePlayer.ticksExisted;
        }

        int hunger = mc.thePlayer.getFoodStats().getFoodLevel();

        if (!eating && hunger <= (int) hungerThreshold.getFloat()) {
            // Check combat cooldown - don't eat during combat
            if (mc.thePlayer.ticksExisted - lastDamageTick < COMBAT_COOLDOWN) {
                return;
            }

            // Apply delay between checks
            int delayTicks = (int) delay.getFloat();
            if (delayTicks > 0) {
                delayCounter++;
                if (delayCounter < delayTicks) return;
                delayCounter = 0;
            }

            int foodSlot = findFoodSlot();
            if (foodSlot == -1) return;

            prevSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = foodSlot;
            mc.thePlayer.sendQueue.addToSendQueue(
                new C09PacketHeldItemChange(foodSlot));
            eating   = true;
            eatTimer = EAT_DURATION;
        }

        if (eating) {
            // Stop eating if we take damage (combat interruption)
            if (mc.thePlayer.hurtTime > 0) {
                stopEating();
                return;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
            eatTimer--;
            if (eatTimer <= 0 || mc.thePlayer.getFoodStats().getFoodLevel() >= 20) {
                stopEating();
            }
        }
    }

    private void stopEating() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        eating = false;
        if (prevSlot != -1) {
            mc.thePlayer.inventory.currentItem = prevSlot;
            mc.thePlayer.sendQueue.addToSendQueue(
                new C09PacketHeldItemChange(prevSlot));
            prevSlot = -1;
        }
    }

    private int findFoodSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s != null && s.getItem() instanceof ItemFood) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null) return;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        eating = false;
        if (prevSlot != -1) {
            mc.thePlayer.inventory.currentItem = prevSlot;
            prevSlot = -1;
        }
    }
}
