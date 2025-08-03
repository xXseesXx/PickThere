package org.xXseesXx.pickthere.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraft.client.renderer.texture.IIconRegister;
import org.xXseesXx.pickthere.config.PickThereConfig;

import org.xXseesXx.pickthere.util.InventoryManager;
import org.xXseesXx.pickthere.util.LocalizationHelper;
import org.xXseesXx.pickthere.util.SoundHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PickThereItem extends Item {
    private static final String SELECTED_POSITIONS_TAG = "SelectedPositions";
    private static final String SAME_ITEM_ONLY_POSITIONS_TAG = "SameItemOnlyPositions";
    private static final String MODE_TAG = "SelectionMode";
    private static final String PICKUP_ENABLED_TAG = "PickupEnabled";
    
    // Selection modes
    public static final int MODE_REGULAR = 0;
    public static final int MODE_SAME_ITEM_ONLY = 1;
    
    // Constants
    private static final int MAX_SELECTION_DISTANCE = 64; // Maximum distance for inventory selection
    
    // Icons for different states
    @SideOnly(Side.CLIENT)
    private IIcon iconEnabled;
    @SideOnly(Side.CLIENT)
    private IIcon iconDisabled;

    public PickThereItem() {
        super();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null && InventoryManager.isValidInventory(tileEntity)) {
            if (player.isSneaking()) {
                int currentMode = getCurrentMode(stack);
                
                String positionStr = x + ", " + y + ", " + z;
                
                if (currentMode == MODE_REGULAR) {
                    if (isPositionSelected(stack, x, y, z)) {
                        removeSelectedPosition(stack, x, y, z);
                        player.addChatMessage(new ChatComponentText(LocalizationHelper.getRemovedRegularMessage(positionStr)));
                        SoundHelper.playInventoryDeselectSound(world, player, x, y, z);

                    } else {
                        removeSameItemOnlyPosition(stack, x, y, z);
                        addSelectedPosition(stack, x, y, z);
                        player.addChatMessage(new ChatComponentText(LocalizationHelper.getAddedRegularMessage(positionStr)));
                        SoundHelper.playInventorySelectSound(world, player, x, y, z);

                    }
                } else {
                    if (isSameItemOnlyPosition(stack, x, y, z)) {
                        removeSameItemOnlyPosition(stack, x, y, z);
                        player.addChatMessage(new ChatComponentText(LocalizationHelper.getRemovedSameItemMessage(positionStr)));
                        SoundHelper.playInventoryDeselectSound(world, player, x, y, z);
                    } else {
                        removeSelectedPosition(stack, x, y, z);
                        addSameItemOnlyPosition(stack, x, y, z);
                        player.addChatMessage(new ChatComponentText(LocalizationHelper.getAddedSameItemMessage(positionStr)));
                        SoundHelper.playInventorySelectSound(world, player, x, y, z);
                    }
                }
                
                // Trigger visual refresh on client side
                if (world.isRemote) {
                    refreshClientVisuals();
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            // Toggle pickup redirection when shift-right-clicking air
            if (!world.isRemote) {
                boolean currentEnabled = isPickupEnabled(stack);
                setPickupEnabled(stack, !currentEnabled);
                
                String message = !currentEnabled ? LocalizationHelper.getPickupEnabledMessage() : LocalizationHelper.getPickupDisabledMessage();
                player.addChatMessage(new ChatComponentText(message));
                SoundHelper.playPickupToggleSound(world, player, !currentEnabled);
            } else if (world.isRemote) {
                refreshClientVisuals();
            }
        } else {
            // Toggle mode when right-clicking air
            if (!world.isRemote) {
                int currentMode = getCurrentMode(stack);
                int newMode = (currentMode == MODE_REGULAR) ? MODE_SAME_ITEM_ONLY : MODE_REGULAR;
                String oldModeStr = (currentMode == MODE_REGULAR) ? "Regular" : "Same-Item-Only";
                String newModeStr = (newMode == MODE_REGULAR) ? "Regular" : "Same-Item-Only";
                setMode(stack, newMode);
                
                String message = (newMode == MODE_REGULAR) ? LocalizationHelper.getModeRegularMessage() : LocalizationHelper.getModeSameItemMessage();
                player.addChatMessage(new ChatComponentText(message));
                SoundHelper.playModeToggleSound(world, player);
            } else if (world.isRemote) {
                refreshClientVisuals();
            }
        }
        return stack;
    }

    private void addSelectedPosition(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        
        // Check if position already exists
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                return; // Already exists
            }
        }
        
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger("x", x);
        posTag.setInteger("y", y);
        posTag.setInteger("z", z);
        positionsList.appendTag(posTag);
        
        tag.setTag(SELECTED_POSITIONS_TAG, positionsList);
    }

    private boolean isPositionSelected(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(SELECTED_POSITIONS_TAG)) {
            return false;
        }
        
        NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                return true;
            }
        }
        return false;
    }

    private void removeSelectedPosition(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                positionsList.removeTag(i);
                break;
            }
        }
        
        tag.setTag(SELECTED_POSITIONS_TAG, positionsList);
    }

    private void addSameItemOnlyPosition(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        NBTTagList positionsList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        
        // Check if position already exists
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                return; // Already exists
            }
        }
        
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger("x", x);
        posTag.setInteger("y", y);
        posTag.setInteger("z", z);
        positionsList.appendTag(posTag);
        
        tag.setTag(SAME_ITEM_ONLY_POSITIONS_TAG, positionsList);
    }

    private boolean isSameItemOnlyPosition(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(SAME_ITEM_ONLY_POSITIONS_TAG)) {
            return false;
        }
        
        NBTTagList positionsList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                return true;
            }
        }
        return false;
    }

    private void removeSameItemOnlyPosition(ItemStack stack, int x, int y, int z) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        NBTTagList positionsList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
        
        for (int i = 0; i < positionsList.tagCount(); i++) {
            NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
            if (posTag.getInteger("x") == x && posTag.getInteger("y") == y && posTag.getInteger("z") == z) {
                positionsList.removeTag(i);
                break;
            }
        }
        
        tag.setTag(SAME_ITEM_ONLY_POSITIONS_TAG, positionsList);
    }

    private int getCurrentMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(MODE_TAG)) {
            return MODE_REGULAR;
        }
        return tag.getInteger(MODE_TAG);
    }

    private void setMode(ItemStack stack, int mode) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(MODE_TAG, mode);
    }

    public static String getCurrentModeString(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(MODE_TAG)) {
            return LocalizationHelper.getRegularMode();
        }
        int mode = tag.getInteger(MODE_TAG);
        return (mode == MODE_REGULAR) ? LocalizationHelper.getRegularMode() : LocalizationHelper.getSameItemOnlyMode();
    }

    public static boolean isPickupEnabled(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(PICKUP_ENABLED_TAG)) {
            return true;
        }
        return tag.getBoolean(PICKUP_ENABLED_TAG);
    }

    private void setPickupEnabled(ItemStack stack, boolean enabled) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(PICKUP_ENABLED_TAG, enabled);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        NBTTagCompound tag = stack.getTagCompound();
        int regularCount = 0;
        int sameItemOnlyCount = 0;
        
        if (tag != null) {
            if (tag.hasKey(SELECTED_POSITIONS_TAG)) {
                NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
                regularCount = positionsList.tagCount();
            }
            if (tag.hasKey(SAME_ITEM_ONLY_POSITIONS_TAG)) {
                NBTTagList sameItemOnlyList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
                sameItemOnlyCount = sameItemOnlyList.tagCount();
            }
        }
        
        int totalCount = regularCount + sameItemOnlyCount;
        tooltip.add(LocalizationHelper.getSelectedInventoriesTooltip(totalCount));
        tooltip.add(LocalizationHelper.getRegularCountTooltip(regularCount, sameItemOnlyCount));
        
        // Show distance limit
        String distanceStr = PickThereConfig.maxPickupDistance < 0 ? 
            LocalizationHelper.getUnlimitedDistance() : 
            String.valueOf((int)PickThereConfig.maxPickupDistance);
        tooltip.add(LocalizationHelper.getDistanceLimitTooltip(distanceStr));
        
        // Show visual effects status
        String visualStatus = PickThereConfig.enableVisualEffects ? 
            LocalizationHelper.getVisualEnabledStatus() : 
            LocalizationHelper.getVisualDisabledStatus();
        tooltip.add(LocalizationHelper.getVisualEffectsTooltip(visualStatus));
        
        // Show advanced position details if enabled and in advanced mode
        if (PickThereConfig.showAdvancedTooltips && advanced && totalCount > 0) {
            tooltip.add("");
            
            if (regularCount > 0) {
                tooltip.add(LocalizationHelper.getRegularPositionsHeader());
                NBTTagList positionsList = tag.getTagList(SELECTED_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
                int maxShow = Math.min(regularCount, PickThereConfig.maxTooltipPositions);
                for (int i = 0; i < maxShow; i++) {
                    NBTTagCompound posTag = positionsList.getCompoundTagAt(i);
                    String posStr = "  " + posTag.getInteger("x") + ", " + posTag.getInteger("y") + ", " + posTag.getInteger("z");
                    tooltip.add(posStr);
                }
                if (regularCount > PickThereConfig.maxTooltipPositions) {
                    tooltip.add("  " + LocalizationHelper.getAndMoreTooltip(regularCount - PickThereConfig.maxTooltipPositions));
                }
            }
            
            if (sameItemOnlyCount > 0) {
                tooltip.add(LocalizationHelper.getSameItemPositionsHeader());
                NBTTagList sameItemOnlyList = tag.getTagList(SAME_ITEM_ONLY_POSITIONS_TAG, Constants.NBT.TAG_COMPOUND);
                int maxShow = Math.min(sameItemOnlyCount, PickThereConfig.maxTooltipPositions);
                for (int i = 0; i < maxShow; i++) {
                    NBTTagCompound posTag = sameItemOnlyList.getCompoundTagAt(i);
                    String posStr = "  " + posTag.getInteger("x") + ", " + posTag.getInteger("y") + ", " + posTag.getInteger("z");
                    tooltip.add(posStr);
                }
                if (sameItemOnlyCount > PickThereConfig.maxTooltipPositions) {
                    tooltip.add("  " + LocalizationHelper.getAndMoreTooltip(sameItemOnlyCount - PickThereConfig.maxTooltipPositions));
                }
            }
        }
        
        tooltip.add("");
        String currentMode = getCurrentModeString(stack);
        String colorCode = currentMode.equals(LocalizationHelper.getRegularMode()) ? "\u00a76" : "\u00a7c";
        tooltip.add(LocalizationHelper.getCurrentModeTooltip(colorCode + currentMode));
        
        boolean pickupEnabled = isPickupEnabled(stack);
        String pickupStatus = pickupEnabled ? LocalizationHelper.getEnabledStatus() : LocalizationHelper.getDisabledStatus();
        tooltip.add(LocalizationHelper.getPickupStatusTooltip(pickupStatus));
        
        tooltip.add("");
        tooltip.add(LocalizationHelper.getUsage1());
        tooltip.add(LocalizationHelper.getUsage2());
        tooltip.add(LocalizationHelper.getUsage3());
        
        // Show reset recipe information if item has data
        if (totalCount > 0 || !pickupEnabled || !currentMode.equals(LocalizationHelper.getRegularMode())) {
            tooltip.add("");
            tooltip.add(LocalizationHelper.getResetRecipeTooltip());
        }
    }
    
    /**
     * Creates a clean PickThere item with default settings
     * @return A new ItemStack with no stored data
     */
    public static ItemStack createCleanItem() {
        ItemStack cleanItem = new ItemStack(org.xXseesXx.pickthere.Pickthere.pickThereItem, 1);
        // No need to set NBT data - defaults will be used:
        // - No selected positions
        // - Pickup enabled = true
        // - Mode = MODE_REGULAR
        return cleanItem;
    }
    
    @SideOnly(Side.CLIENT)
    private void refreshClientVisuals() {
        try {
            // Use reflection to avoid compile-time dependency on client classes
            Class<?> managerClass = Class.forName("org.xXseesXx.pickthere.client.SelectedInventoryManager");
            java.lang.reflect.Method forceRefreshMethod = managerClass.getMethod("forceRefresh");
            forceRefreshMethod.invoke(null);
        } catch (Exception e) {
            // Silently ignore if client classes aren't available
        }
    }
    
    /**
     * Register icons for different states
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        // Register both textures
        this.iconEnabled = iconRegister.registerIcon("pickthere:pick_there_item");
        this.iconDisabled = iconRegister.registerIcon("pickthere:pick_there_item_off");
    }
    
    /**
     * Get the appropriate icon based on the pickup state
     * This method is called when we don't have access to the ItemStack
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        // Default to enabled icon if no specific damage value
        return this.iconEnabled;
    }
    
    /**
     * Get icon from ItemStack - this is where we check the pickup state
     * This method is used for both inventory and world rendering
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass) {
        if (stack != null) {
            boolean pickupEnabled = isPickupEnabled(stack);
            return pickupEnabled ? this.iconEnabled : this.iconDisabled;
        }
        return this.iconEnabled;
    }
    
    /**
     * Get icon for inventory rendering - this is the key method for inventory display
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        if (stack != null) {
            boolean pickupEnabled = isPickupEnabled(stack);
            return pickupEnabled ? this.iconEnabled : this.iconDisabled;
        }
        return this.iconEnabled;
    }
    

}