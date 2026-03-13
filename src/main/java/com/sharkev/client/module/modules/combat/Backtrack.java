package com.sharkev.client.module.modules.combat;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Backtrack extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> delay = addSlider("Delay", 3f, 1f, 10f);
    private final Setting<Float> range = addSlider("Range", 6f, 2f, 8f);

    // Store recent positions for each entity
    private final Map<Integer, LinkedList<double[]>> positionHistory = new HashMap<>();

    public Backtrack() {
        super("Backtrack", "Extend hit window by delaying enemy position updates", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        int maxTicks = (int) delay.getFloat();
        float maxRange = range.getFloat();

        // Track positions of nearby players
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;
            if (player.getHealth() <= 0) continue;

            double dist = mc.thePlayer.getDistanceToEntity(player);
            if (dist > maxRange) {
                positionHistory.remove(player.getEntityId());
                continue;
            }

            int id = player.getEntityId();
            LinkedList<double[]> history = positionHistory.get(id);
            if (history == null) {
                history = new LinkedList<>();
                positionHistory.put(id, history);
            }

            // Store current position
            history.addLast(new double[]{player.posX, player.posY, player.posZ});

            // Keep only delay ticks worth of history
            while (history.size() > maxTicks + 1) {
                history.removeFirst();
            }
        }
    }

    /**
     * Get the delayed position for an entity.
     * Returns null if no history available.
     * KillAura uses this to check if the delayed position is closer,
     * effectively extending the hit window.
     */
    public double[] getDelayedPosition(Entity entity) {
        LinkedList<double[]> history = positionHistory.get(entity.getEntityId());
        if (history == null || history.isEmpty()) return null;
        return history.getFirst(); // Oldest stored position
    }

    @Override
    public void onDisable() {
        super.onDisable();
        positionHistory.clear();
    }
}
