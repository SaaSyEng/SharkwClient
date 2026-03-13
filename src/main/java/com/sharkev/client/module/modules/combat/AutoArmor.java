package com.sharkev.client.module.modules.combat;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoArmor extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    private final Setting<Float> delay = addSlider("Delay", 8f, 5f, 20f);

    private int cooldown = 0;

    public AutoArmor() {
        super("AutoArmor", "Auto equip best armor from inventory", Category.COMBAT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;

        if (cooldown > 0) { cooldown--; return; }

        // Armor slots in player inventory:
        //   armorInventory[0] = boots   -> container slot 8
        //   armorInventory[1] = legs    -> container slot 7
        //   armorInventory[2] = chest   -> container slot 6
        //   armorInventory[3] = helmet  -> container slot 5
        //
        // Container slot mapping for inventoryContainer:
        //   0     = crafting output
        //   1-4   = crafting grid
        //   5-8   = armor (helmet=5, chest=6, legs=7, boots=8)
        //   9-35  = main inventory (top-left to bottom-right)
        //   36-44 = hotbar

        for (int armorSlot = 0; armorSlot < 4; armorSlot++) {
            ItemStack current = mc.thePlayer.inventory.armorInventory[armorSlot];
            int currentProtection = getProtection(current);

            // armorType mapping: 0=helmet, 1=chest, 2=legs, 3=boots
            // armorInventory mapping: 0=boots, 1=legs, 2=chest, 3=helmet
            // So armorType that goes in armorInventory[armorSlot] = 3 - armorSlot
            int expectedArmorType = 3 - armorSlot;

            // Container slot for this armor piece
            // helmet=5, chest=6, legs=7, boots=8 -> 5 + (3 - armorSlot)
            int containerArmorSlot = 5 + (3 - armorSlot);

            // Scan main inventory (slots 9-35) and hotbar (slots 36-44) for better armor
            for (int containerSlot = 9; containerSlot <= 44; containerSlot++) {
                ItemStack candidate = mc.thePlayer.inventoryContainer.getSlot(containerSlot).getStack();
                if (candidate == null) continue;
                if (!(candidate.getItem() instanceof ItemArmor)) continue;

                ItemArmor armor = (ItemArmor) candidate.getItem();
                if (armor.armorType != expectedArmorType) continue;

                int candidateProtection = getProtection(candidate);
                if (candidateProtection > currentProtection) {
                    // Shift-click the better armor piece to auto-equip it
                    mc.playerController.windowClick(
                        mc.thePlayer.inventoryContainer.windowId,
                        containerSlot,
                        0, 1, // type 1 = shift-click
                        mc.thePlayer
                    );
                    cooldown = (int) delay.getFloat();
                    return;
                }
            }
        }
    }

    private int getProtection(ItemStack stack) {
        if (stack == null) return 0;
        if (!(stack.getItem() instanceof ItemArmor)) return 0;
        return ((ItemArmor) stack.getItem()).damageReduceAmount;
    }
}
