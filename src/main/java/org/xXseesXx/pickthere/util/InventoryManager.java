package org.xXseesXx.pickthere.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class InventoryManager {
    private static final String SELECTED_POSITIONS_TAG = "SelectedPositions";
    private static final String SAME_ITEM_ONLY_POSITIONS_TAG = "SameItemOnlyPositions";

    public static class Position {
        public final int x, y, z;
        
        public Position(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Position)) return false;
            Position other = (Position) obj;
            return x == other.x && y == other.y && z == other.z;
        }
        
        @Override
        public int hashCode() {
            return x * 31 * 31 + y * 31 + z;
        }
    }

    public static List<Position> getSelectedPositions(ItemStack stack) {
        List<Position> positions = new ArrayList<Position>();
        NBTTagCompound tag = stack.getTagCompound();
        
        if (tag != null && tag.hasKey(SELECTED_POSITIONS_TAG)) {
            NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < positionsList.tagCount(); i++) {
                NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
                Position pos = new Position(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z"));
                positions.add(pos);
            }
        }
        
        return positions;
    }

    public static List<Position> getSameItemOnlyPositions(ItemStack stack) {
        List<Position> positions = new ArrayList<Position>();
        NBTTagCompound tag = stack.getTagCompound();
        
        if (tag != null && tag.hasKey(SAME_ITEM_ONLY_POSITIONS_TAG)) {
            NBTTagList positionsList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < positionsList.tagCount(); i++) {
                NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
                Position pos = new Position(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z"));
                positions.add(pos);
            }
        }
        
        return positions;
    }

    public static boolean isValidInventory(TileEntity tileEntity) {
        return tileEntity instanceof IInventory;
    }

    public static boolean tryInsertItem(World world, List<Position> regularPositions, List<Position> sameItemOnlyPositions, ItemStack itemToInsert) {
        // Input validation
        if (world == null || itemToInsert == null || itemToInsert.stackSize <= 0) {
            return false;
        }
        
        if ((regularPositions == null || regularPositions.isEmpty()) && 
            (sameItemOnlyPositions == null || sameItemOnlyPositions.isEmpty())) {
            return false;
        }

        ItemStack remainingStack = itemToInsert.copy();

        // Phase 1: Try to stack with existing same items in same-item-only inventories first (PRIORITY)
        if (sameItemOnlyPositions != null) {
            for (Position pos : sameItemOnlyPositions) {
                if (remainingStack.stackSize <= 0) break;
                remainingStack = tryStackAtPosition(world, pos, remainingStack);
            }
        }
        
        // Phase 2: Try to stack with existing same items in regular inventories
        if (regularPositions != null) {
            for (Position pos : regularPositions) {
                if (remainingStack.stackSize <= 0) break;
                remainingStack = tryStackAtPosition(world, pos, remainingStack);
            }
        }

        // Phase 3: Try to insert into same-item-only inventories that already contain the same item type (PRIORITY)
        for (Position pos : sameItemOnlyPositions) {
            if (remainingStack.stackSize <= 0) break;
            
            TileEntity tileEntity = world.getTileEntity(pos.x, pos.y, pos.z);
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                if (containsItemType(inventory, remainingStack)) {
                    remainingStack = insertItemIntoInventory(inventory, remainingStack);
                }
            }
        }

        // Phase 4: Try to insert into regular inventories that already contain the same item type
        for (Position pos : regularPositions) {
            if (remainingStack.stackSize <= 0) break;
            
            TileEntity tileEntity = world.getTileEntity(pos.x, pos.y, pos.z);
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                if (containsItemType(inventory, remainingStack)) {
                    remainingStack = insertItemIntoInventory(inventory, remainingStack);
                }
            }
        }

        // Phase 5: Try to insert into any available regular inventory (same-item-only inventories are skipped)
        for (Position pos : regularPositions) {
            if (remainingStack.stackSize <= 0) break;
            
            TileEntity tileEntity = world.getTileEntity(pos.x, pos.y, pos.z);
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                remainingStack = insertItemIntoInventory(inventory, remainingStack);
            }
        }

        // Update the original stack
        if (remainingStack.stackSize != itemToInsert.stackSize) {
            itemToInsert.stackSize = remainingStack.stackSize;
            return true; // At least some items were inserted
        }

        return false; // No items were inserted
    }

    private static ItemStack tryStackAtPosition(World world, Position pos, ItemStack stackToInsert) {
        TileEntity tileEntity = world.getTileEntity(pos.x, pos.y, pos.z);
        if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            return tryStackWithExisting(inventory, stackToInsert);
        }
        return stackToInsert;
    }

    private static ItemStack tryStackWithExisting(IInventory inventory, ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();
        
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (remaining.stackSize <= 0) break;
            
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack != null && areItemStacksEqual(slotStack, remaining)) {
                int maxStackSize = Math.min(slotStack.getMaxStackSize(), inventory.getInventoryStackLimit());
                int canInsert = maxStackSize - slotStack.stackSize;
                
                if (canInsert > 0) {
                    int toInsert = Math.min(canInsert, remaining.stackSize);
                    slotStack.stackSize += toInsert;
                    remaining.stackSize -= toInsert;
                    inventory.markDirty();
                }
            }
        }
        
        return remaining;
    }

    private static ItemStack insertItemIntoInventory(IInventory inventory, ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();
        
        // First try to stack with existing items
        remaining = tryStackWithExisting(inventory, remaining);
        
        // Then try to insert into empty slots
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (remaining.stackSize <= 0) break;
            
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack == null && inventory.isItemValidForSlot(slot, remaining)) {
                int maxStackSize = Math.min(remaining.getMaxStackSize(), inventory.getInventoryStackLimit());
                int toInsert = Math.min(maxStackSize, remaining.stackSize);
                
                ItemStack insertStack = remaining.copy();
                insertStack.stackSize = toInsert;
                inventory.setInventorySlotContents(slot, insertStack);
                remaining.stackSize -= toInsert;
                inventory.markDirty();
            }
        }
        
        return remaining;
    }

    private static boolean containsItemType(IInventory inventory, ItemStack itemStack) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack != null && areItemStacksEqual(slotStack, itemStack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }
        
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }
        
        if (stack1.getItemDamage() != stack2.getItemDamage()) {
            return false;
        }
        
        // Check NBT tags
        NBTTagCompound nbt1 = stack1.getTagCompound();
        NBTTagCompound nbt2 = stack2.getTagCompound();
        
        if (nbt1 == null && nbt2 == null) {
            return true;
        }
        
        if (nbt1 == null || nbt2 == null) {
            return false;
        }
        
        return nbt1.equals(nbt2);
    }
    
    /**
     * Check if a position is within pickup distance from a player
     */
    public static boolean isWithinPickupDistance(Position pos, double playerX, double playerY, double playerZ) {
        double maxDistance = org.xXseesXx.pickthere.config.PickThereConfig.maxPickupDistance;
        if (maxDistance < 0) {
            return true; // Unlimited distance
        }
        
        double dx = pos.x - playerX;
        double dy = pos.y - playerY;
        double dz = pos.z - playerZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= maxDistance * maxDistance;
    }
}