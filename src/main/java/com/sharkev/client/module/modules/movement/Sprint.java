package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Sprint extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Boolean> omniSprint = addBool("Omni Sprint", false);

    public Sprint() {
        super("Sprint", "Always sprint (toggle omni for all directions)", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.getFoodStats().getFoodLevel() <= 6) return;
        if (mc.thePlayer.isCollidedHorizontally) return;

        if (omniSprint.getBool()) {
            // Omni sprint: sprint in any movement direction (detectable by AC)
            boolean anyMove = mc.gameSettings.keyBindForward.isKeyDown()
                    || mc.gameSettings.keyBindBack.isKeyDown()
                    || mc.gameSettings.keyBindLeft.isKeyDown()
                    || mc.gameSettings.keyBindRight.isKeyDown();

            if (anyMove && !mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            // Legit sprint: only when moving forward (vanilla behavior)
            if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }
}
