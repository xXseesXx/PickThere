package org.xXseesXx.pickthere.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class InventoryManager {
    private static final String SELECTED_POSITIONS_TAG = "SelectedPositions";
    private static final String SAME_ITEM_ONLY_POSITIONS_TAG = "SameItemOnlyPositions";

    public static List<BlockPos> getSelectedPositions(ItemStack stack) {
        List<BlockPos> positions = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        
        if (tag != null && tag.contains(SELECTED_POSITIONS_TAG)) {
            ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < positionsList.size(); i++) {
                CompoundTag posTag = positionsList.getCompound(i);
                BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
                positions.add(pos);
            }
        }
        
        return positions;
    }

    public static List<BlockPos> getSameItemOnlyPositions(ItemStack stack) {
        List<BlockPos> positions = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        
        if (tag != null && tag.contains(SAME_ITEM_ONLY_POSITIONS_TAG)) {
            ListTag positionsList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < positionsList.size(); i++) {
                CompoundTag posTag = positionsList.getCompound(i);
                BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
                positions.add(pos);
            }
        }
        
        return positions;
    }

    public static boolean tryInsertItem(Level level, ItemStack pickThereItem, ItemStack itemToInsert) {
        List<BlockPos> regularPositions = getSelectedPositions(pickThereItem);
        List<BlockPos> sameItemOnlyPositions = getSameItemOnlyPositions(pickThereItem);
        return tryInsertItem(level, regularPositions, sameItemOnlyPositions, itemToInsert);
    }

    public static boolean tryInsertItem(Level level, List<BlockPos> regularPositions, List<BlockPos> sameItemOnlyPositions, ItemStack itemToInsert) {
        if (regularPositions.isEmpty() && sameItemOnlyPositions.isEmpty() || itemToInsert.isEmpty()) {
            return false;
        }

        ItemStack remainingStack = itemToInsert.copy();

        // Phase 1: Try to stack with existing same items in same-item-only inventories first (PRIORITY)
        for (BlockPos pos : sameItemOnlyPositions) {
            if (remainingStack.isEmpty()) break;
            remainingStack = tryStackAtPosition(level, pos, remainingStack);
        }
        
        // Phase 2: Try to stack with existing same items in regular inventories
        for (BlockPos pos : regularPositions) {
            if (remainingStack.isEmpty()) break;
            remainingStack = tryStackAtPosition(level, pos, remainingStack);
        }

        // Phase 3: Try to insert into same-item-only inventories that already contain the same item type (PRIORITY)
        for (BlockPos pos : sameItemOnlyPositions) {
            if (remainingStack.isEmpty()) break;
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (itemHandler != null && containsItemType(itemHandler, remainingStack)) {
                    remainingStack = ItemHandlerHelper.insertItemStacked(itemHandler, remainingStack, false);
                }
            }
        }

        // Phase 4: Try to insert into regular inventories that already contain the same item type
        for (BlockPos pos : regularPositions) {
            if (remainingStack.isEmpty()) break;
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (itemHandler != null && containsItemType(itemHandler, remainingStack)) {
                    remainingStack = ItemHandlerHelper.insertItemStacked(itemHandler, remainingStack, false);
                }
            }
        }

        // Phase 5: Try to insert into any available regular inventory (same-item-only inventories are skipped)
        for (BlockPos pos : regularPositions) {
            if (remainingStack.isEmpty()) break;
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (itemHandler != null) {
                    remainingStack = ItemHandlerHelper.insertItemStacked(itemHandler, remainingStack, false);
                }
            }
        }

        // Update the original stack
        if (remainingStack.getCount() != itemToInsert.getCount()) {
            itemToInsert.setCount(remainingStack.getCount());
            return true; // At least some items were inserted
        }

        return false; // No items were inserted
    }

    private static ItemStack tryStackAtPosition(Level level, BlockPos pos, ItemStack stackToInsert) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (itemHandler != null) {
                return tryStackWithExisting(itemHandler, stackToInsert);
            }
        }
        return stackToInsert;
    }

    private static ItemStack tryStackWithExisting(IItemHandler itemHandler, ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();
        
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            if (remaining.isEmpty()) break;
            
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, remaining)) {
                int maxStackSize = Math.min(slotStack.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                int canInsert = maxStackSize - slotStack.getCount();
                
                if (canInsert > 0) {
                    int toInsert = Math.min(canInsert, remaining.getCount());
                    ItemStack insertStack = remaining.copy();
                    insertStack.setCount(toInsert);
                    
                    ItemStack leftover = itemHandler.insertItem(slot, insertStack, false);
                    remaining.shrink(toInsert - leftover.getCount());
                }
            }
        }
        
        return remaining;
    }

    private static boolean containsItemType(IItemHandler itemHandler, ItemStack itemStack) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, itemStack)) {
                return true;
            }
        }
        return false;
    }
}