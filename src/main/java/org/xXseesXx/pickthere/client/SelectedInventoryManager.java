package org.xXseesXx.pickthere.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.xXseesXx.pickthere.init.ModItems;
import org.xXseesXx.pickthere.items.PickThereItem;

import java.util.ArrayList;
import java.util.List;

public class SelectedInventoryManager {
    private static List<BlockPos> cachedRegularPositions = new ArrayList<>();
    private static List<BlockPos> cachedSameItemOnlyPositions = new ArrayList<>();
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100;

    public static List<BlockPos> getSelectedPositions() {
        updateCacheIfNeeded();
        return new ArrayList<>(cachedRegularPositions);
    }

    public static List<BlockPos> getSameItemOnlyPositions() {
        updateCacheIfNeeded();
        return new ArrayList<>(cachedSameItemOnlyPositions);
    }

    private static void updateCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        
        // Only update cache periodically to avoid performance issues
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            updateCache();
            lastUpdateTime = currentTime;
        }
    }

    private static void updateCache() {
        List<BlockPos> oldRegularPositions = new ArrayList<>(cachedRegularPositions);
        List<BlockPos> oldSameItemOnlyPositions = new ArrayList<>(cachedSameItemOnlyPositions);
        
        cachedRegularPositions.clear();
        cachedSameItemOnlyPositions.clear();
        
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // Only show rendering when PickThere item is in main hand
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == ModItems.PICK_THERE_ITEM.get()) {
            List<BlockPos> regularPositions = PickThereItem.getSelectedPositions(mainHandItem);
            List<BlockPos> sameItemOnlyPositions = PickThereItem.getSameItemOnlyPositions(mainHandItem);
            
            // Request render refresh if positions changed
            if (!regularPositions.equals(oldRegularPositions) || !sameItemOnlyPositions.equals(oldSameItemOnlyPositions)) {
                InventoryRenderer.requestRefresh();
            }
            
            cachedRegularPositions.addAll(regularPositions);
            cachedSameItemOnlyPositions.addAll(sameItemOnlyPositions);
        } else {
            // If positions were cleared, request refresh
            if (!oldRegularPositions.isEmpty() || !oldSameItemOnlyPositions.isEmpty()) {
                InventoryRenderer.requestRefresh();
            }
        }
    }

    public static void forceRefresh() {
        lastUpdateTime = 0;
        InventoryRenderer.requestRefresh();
    }
}