package org.xXseesXx.pickthere.util;

import net.minecraft.util.StatCollector;

public class LocalizationHelper {
    
    public static String translate(String key) {
        return StatCollector.translateToLocal(key);
    }
    
    public static String translate(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }
    
    // Tooltip translations
    public static String getSelectedInventoriesTooltip(int count) {
        return translate("pickthere.tooltip.selected_inventories", count);
    }
    
    public static String getRegularCountTooltip(int regular, int sameItem) {
        return translate("pickthere.tooltip.regular_count", regular, sameItem);
    }
    
    public static String getCurrentModeTooltip(String mode) {
        return translate("pickthere.tooltip.current_mode", mode);
    }
    
    public static String getPickupStatusTooltip(String status) {
        return translate("pickthere.tooltip.pickup_status", status);
    }
    
    public static String getRegularPositionsHeader() {
        return translate("pickthere.tooltip.regular_positions");
    }
    
    public static String getSameItemPositionsHeader() {
        return translate("pickthere.tooltip.same_item_positions");
    }
    
    public static String getAndMoreTooltip(int count) {
        return translate("pickthere.tooltip.and_more", count);
    }
    
    public static String getDistanceLimitTooltip(String distance) {
        return translate("pickthere.tooltip.distance_limit", distance);
    }
    
    public static String getVisualEffectsTooltip(String status) {
        return translate("pickthere.tooltip.visual_effects", status);
    }
    
    // Chat message translations
    public static String getRemovedRegularMessage(String position) {
        return translate("pickthere.chat.removed_regular", position);
    }
    
    public static String getAddedRegularMessage(String position) {
        return translate("pickthere.chat.added_regular", position);
    }
    
    public static String getRemovedSameItemMessage(String position) {
        return translate("pickthere.chat.removed_same_item", position);
    }
    
    public static String getAddedSameItemMessage(String position) {
        return translate("pickthere.chat.added_same_item", position);
    }
    
    public static String getPickupEnabledMessage() {
        return translate("pickthere.chat.pickup_enabled");
    }
    
    public static String getPickupDisabledMessage() {
        return translate("pickthere.chat.pickup_disabled");
    }
    
    public static String getModeRegularMessage() {
        return translate("pickthere.chat.mode_regular");
    }
    
    public static String getModeSameItemMessage() {
        return translate("pickthere.chat.mode_same_item");
    }
    
    public static String getItemResetMessage() {
        return translate("pickthere.chat.item_reset");
    }
    
    // Mode and status translations
    public static String getRegularMode() {
        return translate("pickthere.mode.regular");
    }
    
    public static String getSameItemOnlyMode() {
        return translate("pickthere.mode.same_item_only");
    }
    
    public static String getEnabledStatus() {
        return translate("pickthere.status.enabled");
    }
    
    public static String getDisabledStatus() {
        return translate("pickthere.status.disabled");
    }
    
    public static String getUnlimitedDistance() {
        return translate("pickthere.distance.unlimited");
    }
    
    public static String getVisualEnabledStatus() {
        return translate("pickthere.visual.enabled");
    }
    
    public static String getVisualDisabledStatus() {
        return translate("pickthere.visual.disabled");
    }
    
    // Usage instructions
    public static String getUsage1() {
        return translate("pickthere.tooltip.usage1");
    }
    
    public static String getUsage2() {
        return translate("pickthere.tooltip.usage2");
    }
    
    public static String getUsage3() {
        return translate("pickthere.tooltip.usage3");
    }
    
    public static String getResetRecipeTooltip() {
        return translate("pickthere.tooltip.reset_recipe");
    }
}