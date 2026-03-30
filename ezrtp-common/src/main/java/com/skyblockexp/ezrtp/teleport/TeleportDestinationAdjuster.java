package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.SafetySettings;
import com.skyblockexp.ezrtp.util.BlockCompat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Handles destination adjustments for safe teleportation, such as water surface placement.
 */
public final class TeleportDestinationAdjuster {

    /**
     * Adjusts the destination location for safety, such as placing blocks on water surfaces.
     */
    public static Location adjustForSafety(Location destination, RandomTeleportSettings teleportSettings) {
        SafetySettings safetySettings = teleportSettings.getSafetySettings();
        if (safetySettings == null || !safetySettings.isPlaceDirtOnWater()) {
            return destination;
        }

        World world = destination.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) {
            return destination;
        }

        Block baseBlock = destination.getBlock();
        Block blockBelow = baseBlock.getRelative(BlockFace.DOWN);

        if (baseBlock.isLiquid()) {
            Location raised = raiseOutOfLiquid(destination);
            if (raised == null) {
                return destination;
            }
            destination = raised;
            baseBlock = destination.getBlock();
            blockBelow = baseBlock.getRelative(BlockFace.DOWN);
        }

        if (blockBelow.getType() != Material.WATER) {
            return destination;
        }

        Block blockAbove = baseBlock.getRelative(BlockFace.UP);
        if (!BlockCompat.isPassable(baseBlock) || baseBlock.isLiquid() || !BlockCompat.isPassable(blockAbove)) {
            return destination;
        }

        baseBlock.setType(safetySettings.getPlaceBlockMaterial(), false);
        return destination.clone().add(0.0D, 1.0D, 0.0D);
    }

    private static Location raiseOutOfLiquid(Location destination) {
        World world = destination.getWorld();
        if (world == null) {
            return null;
        }
        Location cursor = destination.clone();
        int maxY = world.getMaxHeight() - 1;
        while (cursor.getBlock().isLiquid() && cursor.getBlockY() <= maxY) {
            cursor.add(0.0D, 1.0D, 0.0D);
        }
        if (cursor.getBlockY() > maxY) {
            return null;
        }
        return cursor;
    }
}