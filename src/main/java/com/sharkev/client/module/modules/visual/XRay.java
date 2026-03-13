package com.sharkev.client.module.modules.visual;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import java.util.HashSet;
import java.util.Set;

public class XRay extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Set<Block> XRAY_BLOCKS = new HashSet<>();
    private static boolean active = false;

    private final Setting<Boolean> fullbright = addBool("Fullbright", true);
    private final Setting<Boolean> showChests = addBool("Show Chests", true);
    private final Setting<Boolean> showSpawners = addBool("Show Spawners", true);

    private float prevGamma;

    static {
        // Ores
        XRAY_BLOCKS.add(Blocks.diamond_ore);
        XRAY_BLOCKS.add(Blocks.iron_ore);
        XRAY_BLOCKS.add(Blocks.gold_ore);
        XRAY_BLOCKS.add(Blocks.emerald_ore);
        XRAY_BLOCKS.add(Blocks.lapis_ore);
        XRAY_BLOCKS.add(Blocks.redstone_ore);
        XRAY_BLOCKS.add(Blocks.lit_redstone_ore);
        XRAY_BLOCKS.add(Blocks.coal_ore);
        XRAY_BLOCKS.add(Blocks.quartz_ore);
        // Valuable
        XRAY_BLOCKS.add(Blocks.obsidian);
        XRAY_BLOCKS.add(Blocks.tnt);
        XRAY_BLOCKS.add(Blocks.enchanting_table);
        XRAY_BLOCKS.add(Blocks.anvil);
        XRAY_BLOCKS.add(Blocks.beacon);
        XRAY_BLOCKS.add(Blocks.hopper);
        // Liquids
        XRAY_BLOCKS.add(Blocks.water);
        XRAY_BLOCKS.add(Blocks.flowing_water);
        XRAY_BLOCKS.add(Blocks.lava);
        XRAY_BLOCKS.add(Blocks.flowing_lava);
        // Portal
        XRAY_BLOCKS.add(Blocks.portal);
        XRAY_BLOCKS.add(Blocks.end_portal);
    }

    public XRay() {
        super("XRay", "See ores through blocks", Category.VISUAL, 0);
    }

    public static boolean isXRayBlock(Block block) {
        if (!active) return false;
        return XRAY_BLOCKS.contains(block)
            || block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.ender_chest
            || block == Blocks.mob_spawner;
    }

    public static boolean isActive() { return active; }

    @Override
    public void onEnable() {
        super.onEnable();
        active = true;
        prevGamma = mc.gameSettings.gammaSetting;
        if (fullbright.getBool()) {
            mc.gameSettings.gammaSetting = 1000f;
        }
        // Force chunk re-render
        if (mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        active = false;
        mc.gameSettings.gammaSetting = prevGamma;
        if (mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
