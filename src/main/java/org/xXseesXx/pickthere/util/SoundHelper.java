package org.xXseesXx.pickthere.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.xXseesXx.pickthere.config.PickThereConfig;

public class SoundHelper {
    
    public static void playInventorySelectSound(World world, EntityPlayer player, int x, int y, int z) {
        if (!PickThereConfig.enableSounds || world.isRemote) {
            return;
        }
        
        // Play a subtle click sound when selecting inventories
        world.playSoundAtEntity(player, "random.click", 0.3F, 1.2F);
    }
    
    public static void playInventoryDeselectSound(World world, EntityPlayer player, int x, int y, int z) {
        if (!PickThereConfig.enableSounds || world.isRemote) {
            return;
        }
        
        // Play a subtle click sound when deselecting inventories
        world.playSoundAtEntity(player, "random.click", 0.3F, 0.8F);
    }
    
    public static void playModeToggleSound(World world, EntityPlayer player) {
        if (!PickThereConfig.enableSounds || world.isRemote) {
            return;
        }
        
        // Play a note sound when toggling modes
        world.playSoundAtEntity(player, "note.pling", 0.5F, 1.0F);
    }
    
    public static void playPickupToggleSound(World world, EntityPlayer player, boolean enabled) {
        if (!PickThereConfig.enableSounds || world.isRemote) {
            return;
        }
        
        // Play different sounds for enable/disable
        if (enabled) {
            world.playSoundAtEntity(player, "note.pling", 0.5F, 1.5F);
        } else {
            world.playSoundAtEntity(player, "note.bass", 0.5F, 0.8F);
        }
    }
}