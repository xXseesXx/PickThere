package org.xXseesXx.pickthere.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.xXseesXx.pickthere.Pickthere;

/**
 * Custom recipe for resetting PickThere item data
 * Takes a PickThere item and returns a clean one without NBT data
 * 
 * Recipe: PickThere Item = Clean PickThere Item
 * This removes all stored inventory positions and resets the item to default settings:
 * - No selected positions
 * - Pickup enabled = true  
 * - Mode = Regular
 */
public class PickThereResetRecipe implements IRecipe {
    
    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        ItemStack pickThereItem = null;
        int itemCount = 0;
        
        // Check all slots in the crafting grid
        for (int i = 0; i < crafting.getSizeInventory(); i++) {
            ItemStack stack = crafting.getStackInSlot(i);
            if (stack != null) {
                itemCount++;
                
                if (stack.getItem() == Pickthere.pickThereItem) {
                    if (pickThereItem != null) {
                        return false; // Multiple PickThere items
                    }
                    pickThereItem = stack;
                    // Only allow reset if the item actually has data to reset
                    if (!hasDataToReset(stack)) {
                        return false; // Item is already clean
                    }
                } else {
                    return false; // Invalid item - only PickThere items allowed
                }
            }
        }
        
        // Must have exactly one PickThere item with data to reset
        return itemCount == 1 && pickThereItem != null;
    }
    
    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
        if (!matches(crafting, null)) {
            return null;
        }
        
        // Create a new clean PickThere item using the utility method
        return org.xXseesXx.pickthere.items.PickThereItem.createCleanItem();
    }
    
    @Override
    public int getRecipeSize() {
        return 1; // Requires 1 item
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return org.xXseesXx.pickthere.items.PickThereItem.createCleanItem();
    }
    
    public ItemStack[] getRemainingItems(InventoryCrafting crafting) {
        // No remaining items for this simple recipe
        return new ItemStack[crafting.getSizeInventory()];
    }
    
    /**
     * Check if the PickThere item has any data that needs to be reset
     */
    private boolean hasDataToReset(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) {
            return false; // No NBT data means it's already clean
        }
        
        // Check if it has any selected positions
        if (stack.getTagCompound().hasKey("SelectedPositions") || 
            stack.getTagCompound().hasKey("SameItemOnlyPositions")) {
            return true;
        }
        
        // Check if pickup is disabled
        if (stack.getTagCompound().hasKey("PickupEnabled") && 
            !stack.getTagCompound().getBoolean("PickupEnabled")) {
            return true;
        }
        
        // Check if mode is not regular (default)
        if (stack.getTagCompound().hasKey("SelectionMode") && 
            stack.getTagCompound().getInteger("SelectionMode") != 0) {
            return true;
        }
        
        return false; // Item is already in default state
    }
}