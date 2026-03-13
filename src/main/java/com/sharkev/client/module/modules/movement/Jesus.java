package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Jesus extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeSetting mode = addMode("Mode", "Bounce", "Bounce", "Solid");

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Check if standing in liquid
        boolean inLiquid = mc.thePlayer.isInWater() || mc.thePlayer.isInLava();

        // Check block below for liquid
        BlockPos below = new BlockPos(mc.thePlayer).down();
        Material mat = mc.theWorld.getBlockState(below).getBlock().getMaterial();
        boolean liquidBelow = mat == Material.water || mat == Material.lava;

        String currentMode = mode.getMode();

        if (currentMode.equals("Bounce")) {
            if (inLiquid) {
                // Bounce on liquid surface with randomized motionY to avoid constant signature
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY = ThreadLocalRandom.current().nextDouble(0.10, 0.12);
                }
                mc.thePlayer.motionX *= 1.02;
                mc.thePlayer.motionZ *= 1.02;
            }

            if (liquidBelow && !inLiquid) {
                mc.thePlayer.motionY = 0.05;
                mc.thePlayer.fallDistance = 0;
            }
        } else if (currentMode.equals("Solid")) {
            // Solid mode: treat liquid surface as solid ground
            if (inLiquid) {
                // Push player up to surface
                mc.thePlayer.motionY = ThreadLocalRandom.current().nextDouble(0.10, 0.12);
                mc.thePlayer.motionX *= 0.98;
                mc.thePlayer.motionZ *= 0.98;
            }

            if (liquidBelow && !inLiquid) {
                // On liquid surface: zero vertical motion, simulate ground
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.onGround = true;
                mc.thePlayer.fallDistance = 0;
            }
        }
    }
}
