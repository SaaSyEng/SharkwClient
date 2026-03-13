package com.sharkev.client.module.modules.movement;

import com.sharkev.client.module.Category;
import com.sharkev.client.module.Module;
import com.sharkev.client.util.BlockUtil;
import com.sharkev.client.util.PacketUtil;
import com.sharkev.client.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Scaffold extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Setting<Boolean> tower = addBool("Tower", false);
    private final ModeSetting rotations = addMode("Rotations", "Silent", "Silent", "Visual");
    private final Setting<Boolean> safeWalk = addBool("Safe Walk", true);
    private final Setting<Float> delay = addSlider("Delay", 0f, 0f, 4f);

    private int prevSlot = -1;
    private int ticksSincePlace = 0;

    public Scaffold() {
        super("Scaffold", "Auto place blocks below you (bridge/tower)", Category.MOVEMENT, 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ticksSincePlace++;

        // Find a block slot
        int blockSlot = getBlockSlot();
        if (blockSlot == -1) return;

        BlockPos below = new BlockPos(mc.thePlayer).down();

        // Tower mode: hold jump to go straight up
        if (tower.getBool() && mc.gameSettings.keyBindJump.isKeyDown()) {
            handleTower(blockSlot, below);
            return;
        }

        // Flat bridge: place block below while walking
        if (BlockUtil.isReplaceable(below)) {
            // Respect placement delay
            int delayTicks = (int) delay.getFloat();
            if (ticksSincePlace >= delayTicks) {
                placeBelow(blockSlot, below);
                ticksSincePlace = 0;
            }
        }

        // Safe walk: stop the player from walking off edges
        if (safeWalk.getBool()) {
            mc.thePlayer.setSneaking(true);
        }
    }

    private void handleTower(int blockSlot, BlockPos below) {
        if (BlockUtil.isReplaceable(below)) {
            placeBelow(blockSlot, below);
        }
        // Boost upward
        mc.thePlayer.motionY = 0.42;
        mc.thePlayer.fallDistance = 0;
    }

    private void placeBelow(int blockSlot, BlockPos below) {
        // Switch to block slot silently via packet
        if (mc.thePlayer.inventory.currentItem != blockSlot) {
            prevSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = blockSlot;
            mc.thePlayer.sendQueue.addToSendQueue(
                new C09PacketHeldItemChange(blockSlot)
            );
        }

        // Calculate look-down rotation
        float[] rot = RotationUtil.addNoise(mc.thePlayer.rotationYaw, 80f, 2f);

        if (rotations.getMode().equals("Silent")) {
            // Silent: send rotation via packet without changing visual look
            PacketUtil.sendRotation(rot[0], rot[1], mc.thePlayer.onGround);
        } else {
            // Visual: actually rotate the player camera
            mc.thePlayer.rotationYaw = rot[0];
            mc.thePlayer.rotationPitch = rot[1];
        }

        // Find a solid neighbor of the block below to place against
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos neighbor = below.offset(face);
            if (!BlockUtil.isReplaceable(neighbor)) {
                BlockUtil.placeBlockOnFace(neighbor, face.getOpposite());
                break;
            }
        }
    }

    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                Block b = ((ItemBlock) stack.getItem()).getBlock();
                if (!(b instanceof BlockAir)) return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ticksSincePlace = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null) return;
        // Restore slot and sneak
        if (prevSlot != -1) {
            mc.thePlayer.inventory.currentItem = prevSlot;
            mc.thePlayer.sendQueue.addToSendQueue(
                new C09PacketHeldItemChange(prevSlot)
            );
            prevSlot = -1;
        }
        mc.thePlayer.setSneaking(false);
    }
}
