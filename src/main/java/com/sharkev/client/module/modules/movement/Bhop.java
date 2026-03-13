package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Bhop extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> speed = addSlider("Speed", 1.0f, 0.5f, 2.0f);
    private final Setting<Boolean> autoJump = addBool("Auto Jump", true);

    private boolean wasInAir = false;

    public Bhop() {
        super("Bhop", "Auto bunny hop with speed boost on landing", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        boolean moving = mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;

        if (mc.thePlayer.onGround && moving) {
            // Speed boost on landing: apply a small horizontal boost when hitting ground from air
            if (wasInAir) {
                double baseSpeed = 0.2873;
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
                }

                double currentSpeed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                    + mc.thePlayer.motionZ * mc.thePlayer.motionZ);

                // Apply landing boost scaled by the speed setting
                double targetSpeed = baseSpeed * speed.getFloat();
                if (currentSpeed < targetSpeed) {
                    double factor = targetSpeed / Math.max(currentSpeed, 0.001);
                    mc.thePlayer.motionX *= factor;
                    mc.thePlayer.motionZ *= factor;
                }
            }

            // Auto jump or manual jump
            if (autoJump.getBool() || mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.jump();
                // Ensure sprinting while moving forward
                if (mc.thePlayer.moveForward > 0) {
                    mc.thePlayer.setSprinting(true);
                }
            }
        }

        wasInAir = !mc.thePlayer.onGround;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        wasInAir = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        wasInAir = false;
    }
}
