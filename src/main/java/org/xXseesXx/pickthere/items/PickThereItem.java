package org.xXseesXx.pickthere.items;

import java.util.List;

import javax.annotation.Nullable;

import org.xXseesXx.pickthere.util.InventoryManager;
import org.xXseesXx.pickthere.client.SelectedInventoryManager;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class PickThereItem extends Item {
    private static final String SELECTED_POSITIONS_TAG = "SelectedPositions";
    private static final String SAME_ITEM_ONLY_POSITIONS_TAG = "SameItemOnlyPositions";
    private static final String MODE_TAG = "SelectionMode";
    private static final String PICKUP_ENABLED_TAG = "PickupEnabled";
    
    // Selection modes
    public static final int MODE_REGULAR = 0;
    public static final int MODE_SAME_ITEM_ONLY = 1;

    public PickThereItem(Properties properties) {
        super(properties);
    }

    public static void registerItemProperties() {
        ItemProperties.register(org.xXseesXx.pickthere.init.ModItems.PICK_THERE_ITEM.get(), 
            ResourceLocation.fromNamespaceAndPath("pickthere", "active"), 
            (stack, level, entity, seed) -> {
                return isPickupEnabled(stack) ? 1.0F : 0.0F;
            });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null || level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (itemHandler != null) {
                if (player.isShiftKeyDown()) {
                    int currentMode = getCurrentMode(stack);
                    
                    if (currentMode == MODE_REGULAR) {

                        if (isPositionSelected(stack, pos)) {
                            removeSelectedPosition(stack, pos);
                            player.sendSystemMessage(Component.literal("Removed regular inventory at " + pos.toShortString()));
                        } else {

                            removeSameItemOnlyPosition(stack, pos);
                            addSelectedPosition(stack, pos);
                            player.sendSystemMessage(Component.literal("Added regular inventory at " + pos.toShortString()));
                        }
                    } else {
                        if (isSameItemOnlyPosition(stack, pos)) {
                            removeSameItemOnlyPosition(stack, pos);
                            player.sendSystemMessage(Component.literal("Removed same-item-only inventory at " + pos.toShortString()));
                        } else {

                            removeSelectedPosition(stack, pos);
                            addSameItemOnlyPosition(stack, pos);
                            player.sendSystemMessage(Component.literal("Added same-item-only inventory at " + pos.toShortString()));
                        }
                    }
                }
                

                if (level.isClientSide()) {
                    SelectedInventoryManager.forceRefresh();
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            // Toggle pickup redirection when shift-right-clicking air
            if (!level.isClientSide()) {
                boolean currentEnabled = isPickupEnabled(stack);
                setPickupEnabled(stack, !currentEnabled);
                
                String status = !currentEnabled ? "§aEnabled" : "§cDisabled";
                player.sendSystemMessage(Component.literal("Pickup Redirection: " + status));
            } else {
                SelectedInventoryManager.forceRefresh();
            }
            return InteractionResultHolder.success(stack);
        } else {
            // Toggle mode when left-clicking air
            if (!level.isClientSide()) {
                int currentMode = getCurrentMode(stack);
                int newMode = (currentMode == MODE_REGULAR) ? MODE_SAME_ITEM_ONLY : MODE_REGULAR;
                setMode(stack, newMode);
                
                String modeName = (newMode == MODE_REGULAR) ? "Regular Selection" : "Same-Item-Only Selection";
                String colorCode = (newMode == MODE_REGULAR) ? "§6" : "§c";
                player.sendSystemMessage(Component.literal("Mode: " + colorCode + modeName));
            } else {
                SelectedInventoryManager.forceRefresh();
            }
            return InteractionResultHolder.success(stack);
        }
    }

    private void addSelectedPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
        
        // Check if position already exists
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                return; // Already exists
            }
        }
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        positionsList.add(posTag);
        
        tag.put(SELECTED_POSITIONS_TAG, positionsList);
    }

    private boolean isPositionSelected(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SELECTED_POSITIONS_TAG)) {
            return false;
        }
        
        ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private void removeSelectedPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
        
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                positionsList.remove(i);
                break;
            }
        }
        
        tag.put(SELECTED_POSITIONS_TAG, positionsList);
    }

    private void clearSelectedPositions(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(SELECTED_POSITIONS_TAG, new ListTag());
        tag.put(SAME_ITEM_ONLY_POSITIONS_TAG, new ListTag());
    }

    private void addSameItemOnlyPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag positionsList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
        
        // Check if position already exists in same-item-only list
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                return; // Already exists
            }
        }
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        positionsList.add(posTag);
        
        tag.put(SAME_ITEM_ONLY_POSITIONS_TAG, positionsList);
    }

    private boolean isSameItemOnlyPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SAME_ITEM_ONLY_POSITIONS_TAG)) {
            return false;
        }
        
        ListTag positionsList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private void removeSameItemOnlyPosition(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag positionsList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
        
        for (int i = 0; i < positionsList.size(); i++) {
            CompoundTag posTag = positionsList.getCompound(i);
            BlockPos existingPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            if (existingPos.equals(pos)) {
                positionsList.remove(i);
                break;
            }
        }
        
        tag.put(SAME_ITEM_ONLY_POSITIONS_TAG, positionsList);
    }

    public static List<BlockPos> getSelectedPositions(ItemStack stack) {
        return InventoryManager.getSelectedPositions(stack);
    }

    public static List<BlockPos> getSameItemOnlyPositions(ItemStack stack) {
        return InventoryManager.getSameItemOnlyPositions(stack);
    }

    private int getCurrentMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MODE_TAG)) {
            return MODE_REGULAR;
        }
        return tag.getInt(MODE_TAG);
    }

    private void setMode(ItemStack stack, int mode) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(MODE_TAG, mode);
    }

    public static String getCurrentModeString(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MODE_TAG)) {
            return "Regular";
        }
        int mode = tag.getInt(MODE_TAG);
        return (mode == MODE_REGULAR) ? "Regular" : "Same-Item-Only";
    }

    public static boolean isPickupEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PICKUP_ENABLED_TAG)) {
            return true;
        }
        return tag.getBoolean(PICKUP_ENABLED_TAG);
    }

    private void setPickupEnabled(ItemStack stack, boolean enabled) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(PICKUP_ENABLED_TAG, enabled);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        int regularCount = 0;
        int sameItemOnlyCount = 0;
        
        if (tag != null) {
            if (tag.contains(SELECTED_POSITIONS_TAG)) {
                ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
                regularCount = positionsList.size();
            }
            if (tag.contains(SAME_ITEM_ONLY_POSITIONS_TAG)) {
                ListTag sameItemOnlyList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
                sameItemOnlyCount = sameItemOnlyList.size();
            }
        }
        
        int totalCount = regularCount + sameItemOnlyCount;
        tooltip.add(Component.literal("Selected inventories: " + totalCount));
        tooltip.add(Component.literal("Regular: " + regularCount + ", Same-item-only: " + sameItemOnlyCount));
        
        if (flag.isAdvanced() && totalCount > 0) {
            if (regularCount > 0) {
                tooltip.add(Component.literal("Regular positions:"));
                ListTag positionsList = tag.getList(SELECTED_POSITIONS_TAG, Tag.TAG_COMPOUND);
                for (int i = 0; i < Math.min(regularCount, 3); i++) {
                    CompoundTag posTag = positionsList.getCompound(i);
                    BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
                    tooltip.add(Component.literal("  " + pos.toShortString()));
                }
                if (regularCount > 3) {
                    tooltip.add(Component.literal("  ... and " + (regularCount - 3) + " more"));
                }
            }
            
            if (sameItemOnlyCount > 0) {
                tooltip.add(Component.literal("Same-item-only positions:"));
                ListTag sameItemOnlyList = tag.getList(SAME_ITEM_ONLY_POSITIONS_TAG, Tag.TAG_COMPOUND);
                for (int i = 0; i < Math.min(sameItemOnlyCount, 3); i++) {
                    CompoundTag posTag = sameItemOnlyList.getCompound(i);
                    BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
                    tooltip.add(Component.literal("  " + pos.toShortString()));
                }
                if (sameItemOnlyCount > 3) {
                    tooltip.add(Component.literal("  ... and " + (sameItemOnlyCount - 3) + " more"));
                }
            }
        }
        
        tooltip.add(Component.literal(""));
        String currentMode = getCurrentModeString(stack);
        String colorCode = currentMode.equals("Regular") ? "§6" : "§c";
        tooltip.add(Component.literal("Current Mode: " + colorCode + currentMode));
        
        boolean pickupEnabled = isPickupEnabled(stack);
        String pickupStatus = pickupEnabled ? "§aEnabled" : "§cDisabled";
        tooltip.add(Component.literal("Pickup Redirection: " + pickupStatus));
        
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Right-click air: Toggle mode"));
        tooltip.add(Component.literal("Shift+Right-click: Select inventory"));
        tooltip.add(Component.literal("Shift+Right-click air: Toggle pickup"));
    }
}