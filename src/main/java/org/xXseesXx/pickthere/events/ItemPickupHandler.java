package org.xXseesXx.pickthere.events;

import java.util.ArrayList;
import java.util.List;

import org.xXseesXx.pickthere.Pickthere;
import org.xXseesXx.pickthere.init.ModItems;
import org.xXseesXx.pickthere.items.PickThereItem;
import org.xXseesXx.pickthere.util.InventoryManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pickthere.MODID)
public class ItemPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedUpItem = event.getItem().getItem();
        
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player has the PickThere item in their inventory
        ItemStack pickThereItem = findPickThereItem(player);
        if (pickThereItem.isEmpty()) {
            return;
        }

        // Check if pickup redirection is enabled
        if (!PickThereItem.isPickupEnabled(pickThereItem)) {
            return;
        }

        // Get selected positions from the PickThere item
        List<BlockPos> regularPositions = InventoryManager.getSelectedPositions(pickThereItem);
        List<BlockPos> sameItemOnlyPositions = InventoryManager.getSameItemOnlyPositions(pickThereItem);
        
        if (regularPositions.isEmpty() && sameItemOnlyPositions.isEmpty()) {
            return;
        }

        // Filter positions by distance
        List<BlockPos> validRegularPositions = filterPositionsByDistance(regularPositions, player);
        List<BlockPos> validSameItemOnlyPositions = filterPositionsByDistance(sameItemOnlyPositions, player);
        
        if (validRegularPositions.isEmpty() && validSameItemOnlyPositions.isEmpty()) {
            return;
        }

        // Try to insert the picked up item into selected inventories
        ItemStack itemToInsert = pickedUpItem.copy();
        boolean inserted = InventoryManager.tryInsertItem(player.level(), validRegularPositions, validSameItemOnlyPositions, itemToInsert);
        
        if (inserted) {
            if (itemToInsert.isEmpty()) {
                // All items were inserted, cancel the pickup event
                event.setCanceled(true);
                event.getItem().discard();
            } else {
                // Some items were inserted, update the item entity
                event.getItem().getItem().setCount(itemToInsert.getCount());
            }
        }
    }

    private static ItemStack findPickThereItem(Player player) {
        // Check main inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.PICK_THERE_ITEM.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<BlockPos> filterPositionsByDistance(List<BlockPos> positions, Player player) {
        List<BlockPos> validPositions = new ArrayList<>();
        double maxDistance = org.xXseesXx.pickthere.config.PickThereConfig.MAX_PICKUP_DISTANCE.get();
        
        // If maxDistance is negative, no distance limit (unlimited range)
        if (maxDistance < 0) {
            return new ArrayList<>(positions);
        }
        
        for (BlockPos pos : positions) {
            double distance = player.blockPosition().distSqr(pos);
            if (distance <= maxDistance * maxDistance) {
                validPositions.add(pos);
            }
        }
        
        return validPositions;
    }


}