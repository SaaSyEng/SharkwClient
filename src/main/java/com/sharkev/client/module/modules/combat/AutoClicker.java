package com.sharkev.client.module.modules.combat;

import com.sharkev.client.SharkevClient;
import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class AutoClicker extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> minCPS = addSlider("Min CPS", 9f, 5f, 20f);
    private final Setting<Float> maxCPS = addSlider("Max CPS", 13f, 5f, 20f);

    private long nextClickTime = 0;
    private boolean wasDown = false;
    private int ticksSinceLastClick = 0;

    public AutoClicker() {
        super("AutoClicker", "Gaussian CPS autoclicker", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;

        // Don't fire if KillAura is active (it handles its own attacks)
        Module killAura = SharkevClient.moduleManager.getByName("KillAura");
        if (killAura != null && killAura.isEnabled()) return;

        boolean down = Mouse.isButtonDown(0);

        if (!down) {
            wasDown = false;
            nextClickTime = 0;
            ticksSinceLastClick = 0;
            return;
        }

        // First press - let vanilla handle the initial click
        if (!wasDown) {
            wasDown = true;
            nextClickTime = System.currentTimeMillis() + RandomUtil.nextClickDelay(
                RandomUtil.nextCPS(minCPS.getFloat(), maxCPS.getFloat()));
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        // Perform click
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            mc.thePlayer.swingItem();
            mc.playerController.attackEntity(mc.thePlayer, mc.objectMouseOver.entityHit);
        } else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            // Block breaking - just swing, vanilla handles the rest
            mc.thePlayer.swingItem();
            // Reset left click counter so vanilla processes block damage
            // Try MCP mapped name first, then SRG name
            try {
                java.lang.reflect.Field f = Minecraft.class.getDeclaredField("leftClickCounter");
                f.setAccessible(true);
                f.setInt(mc, 0);
            } catch (Exception ignored) {
                try {
                    java.lang.reflect.Field f = Minecraft.class.getDeclaredField("field_71429_W");
                    f.setAccessible(true);
                    f.setInt(mc, 0);
                } catch (Exception ignored2) {}
            }
        } else {
            mc.thePlayer.swingItem();
        }

        // Schedule next click
        float cps = RandomUtil.nextCPS(minCPS.getFloat(), maxCPS.getFloat());
        nextClickTime = now + RandomUtil.nextClickDelay(cps);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        wasDown = false;
        nextClickTime = 0;
    }
}
