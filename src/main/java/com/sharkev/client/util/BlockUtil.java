package com.sharkev.client.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public class BlockUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Check if a block position is air/replaceable
    public static boolean isReplaceable(BlockPos pos) {
        Block b = mc.theWorld.getBlockState(pos).getBlock();
        return b instanceof BlockAir || b instanceof BlockLiquid;
    }

    // Check if a block position is solid (can be placed on)
    public static boolean isSolid(BlockPos pos) {
        Block b = mc.theWorld.getBlockState(pos).getBlock();
        return b.isFullBlock();
    }

    // Place a block on a given face of blockPos via packet
    // Returns true if packet was sent
    public static boolean placeBlockOnFace(BlockPos pos, EnumFacing face) {
        if (mc.thePlayer.getHeldItem() == null) return false;

        Vec3 hitVec = new Vec3(
            pos.getX() + 0.5 + face.getFrontOffsetX() * 0.5,
            pos.getY() + 0.5 + face.getFrontOffsetY() * 0.5,
            pos.getZ() + 0.5 + face.getFrontOffsetZ() * 0.5
        );

        mc.thePlayer.sendQueue.addToSendQueue(
            new C08PacketPlayerBlockPlacement(pos, face.getIndex(),
                mc.thePlayer.getHeldItem(), (float) hitVec.xCoord,
                (float) hitVec.yCoord, (float) hitVec.zCoord)
        );
        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        return true;
    }

    // Find a solid neighbor of an air block to place against
    // Used by Scaffold to find which face to click
    public static BlockPos[] findPlaceTarget(BlockPos airPos) {
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos neighbor = airPos.offset(face);
            if (isSolid(neighbor)) {
                return new BlockPos[]{neighbor, airPos};
            }
        }
        return null;
    }
}
