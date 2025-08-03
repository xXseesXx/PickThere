package org.xXseesXx.pickthere.events;

import java.util.ArrayList;
import java.util.List;

import org.xXseesXx.pickthere.Pickthere;
import org.xXseesXx.pickthere.config.PickThereConfig;
import org.xXseesXx.pickthere.items.PickThereItem;
import org.xXseesXx.pickthere.util.InventoryManager;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemPickupHandler {

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack pickedUpItem = event.item.getEntityItem();
        
        if (player.worldObj.isRemote) {
            return;
        }

        // Check if player has the PickThere item in their inventory
        ItemStack pickThereItem = findPickThereItem(player);
        if (pickThereItem == null) {
            return;
        }

        // Check if pickup redirection is enabled
        if (!PickThereItem.isPickupEnabled(pickThereItem)) {
            return;
        }

        // Get selected positions from the PickThere item
        List<InventoryManager.Position> regularPositions = InventoryManager.getSelectedPositions(pickThereItem);
        List<InventoryManager.Position> sameItemOnlyPositions = InventoryManager.getSameItemOnlyPositions(pickThereItem);
        
        if (regularPositions.isEmpty() && sameItemOnlyPositions.isEmpty()) {
            return;
        }

        // Filter positions by distance using config
        List<InventoryManager.Position> validRegularPositions = filterPositionsByDistance(regularPositions, player, PickThereConfig.maxPickupDistance);
        List<InventoryManager.Position> validSameItemOnlyPositions = filterPositionsByDistance(sameItemOnlyPositions, player, PickThereConfig.maxPickupDistance);
        
        if (validRegularPositions.isEmpty() && validSameItemOnlyPositions.isEmpty()) {
            return;
        }

        // Try to insert the picked up item into selected inventories
        ItemStack itemToInsert = pickedUpItem.copy();
        boolean inserted = InventoryManager.tryInsertItem(player.worldObj, validRegularPositions, validSameItemOnlyPositions, itemToInsert);
        
        if (inserted) {
            if (itemToInsert.stackSize <= 0) {
                // All items were inserted, cancel the pickup event
                event.setCanceled(true);
                event.item.setDead();
            } else {
                // Some items were inserted, update the item entity
                event.item.getEntityItem().stackSize = itemToInsert.stackSize;
            }
        }
    }

    private ItemStack findPickThereItem(EntityPlayer player) {
        // Check main inventory
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Pickthere.pickThereItem) {
                return stack;
            }
        }
        return null;
    }

    private List<InventoryManager.Position> filterPositionsByDistance(List<InventoryManager.Position> positions, EntityPlayer player, double maxDistance) {
        List<InventoryManager.Position> validPositions = new ArrayList<InventoryManager.Position>();
        
        // If maxDistance is negative, no distance limit (unlimited range)
        if (maxDistance < 0) {
            return new ArrayList<InventoryManager.Position>(positions);
        }
        
        int playerX = (int) Math.floor(player.posX);
        int playerY = (int) Math.floor(player.posY);
        int playerZ = (int) Math.floor(player.posZ);
        
        for (InventoryManager.Position pos : positions) {
            double distanceSquared = (playerX - pos.x) * (playerX - pos.x) + 
                                   (playerY - pos.y) * (playerY - pos.y) + 
                                   (playerZ - pos.z) * (playerZ - pos.z);
            if (distanceSquared <= maxDistance * maxDistance) {
                validPositions.add(pos);
            }
        }
        
        return validPositions;
    }
    

}