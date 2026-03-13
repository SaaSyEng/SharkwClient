package com.sharkev.client.module.modules.combat;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Strafe extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> speed = addSlider("Speed", 0.5f, 0.1f, 1.0f);
    private final Setting<Float> switchInterval = addSlider("Switch Interval", 8f, 3f, 15f);

    private int tickCounter = 0;
    private int strafeDir = 1; // 1 = right, -1 = left

    public Strafe() {
        super("Strafe", "Alternate strafe around KillAura target", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        // Only strafe if KillAura is active and has a target
        KillAura ka = (KillAura) SharkevClient.moduleManager.getByName("KillAura");
        if (ka == null || !ka.isEnabled()) return;

        EntityLivingBase target = ka.getCurrentTarget();
        if (target == null) return;

        // Alternate strafe direction every switchInterval ticks
        tickCounter++;
        if (tickCounter >= (int) switchInterval.getFloat()) {
            tickCounter = 0;
            strafeDir *= -1;
        }

        // Use the player's actual input as base and add strafe on top
        float playerForward = mc.thePlayer.movementInput.moveForward;
        float playerStrafe = mc.thePlayer.movementInput.moveStrafe;

        // Add our strafe movement scaled by speed setting
        float strafeAmount = strafeDir * speed.getFloat();
        mc.thePlayer.movementInput.moveStrafe = playerStrafe + strafeAmount;

        // Preserve the player's forward input; if they're not pressing anything,
        // add a small forward component to keep moving toward the target
        if (playerForward == 0f) {
            mc.thePlayer.movementInput.moveForward = 0.2f * speed.getFloat();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        tickCounter = 0;
        strafeDir = 1;
    }
}
