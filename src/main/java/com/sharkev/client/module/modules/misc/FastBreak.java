package com.sharkev.client.module.modules.misc;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FastBreak extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Float> multiplier = addSlider("Speed", 1.5f, 1.0f, 5.0f);

    public FastBreak() {
        super("FastBreak", "Mine blocks faster", Category.MISC, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.playerController == null) return;

        // Access curBlockDamageMP via reflection to speed up breaking
        try {
            java.lang.reflect.Field f = mc.playerController.getClass().getDeclaredField("curBlockDamageMP");
            f.setAccessible(true);
            float damage = f.getFloat(mc.playerController);
            if (damage > 0) {
                float boost = (multiplier.getFloat() - 1.0f) * damage;
                f.setFloat(mc.playerController, Math.min(1.0f, damage + boost));
            }
        } catch (Exception e) {
            // Try obfuscated name
            try {
                java.lang.reflect.Field f = mc.playerController.getClass().getDeclaredField("field_78770_f");
                f.setAccessible(true);
                float damage = f.getFloat(mc.playerController);
                if (damage > 0) {
                    float boost = (multiplier.getFloat() - 1.0f) * damage;
                    f.setFloat(mc.playerController, Math.min(1.0f, damage + boost));
                }
            } catch (Exception ignored) {}
        }
    }
}
