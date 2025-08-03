package org.xXseesXx.pickthere.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.File;

public class PickThereConfig {
    private static Configuration config;
    
    // Config categories
    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_VISUAL = "visual";
    
    // Config values
    public static double maxPickupDistance = -1.0; // Unlimited by default
    public static boolean enableVisualEffects = true;
    public static boolean showAdvancedTooltips = true;
    public static int maxTooltipPositions = 5;
    public static boolean enableSounds = true;

    public static float outlineOpacity = 0.8f;
    public static double maxRenderDistance = -1.0; // Unlimited by default
    
    public static void init(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }
    
    public static void loadConfig() {
        try {
            config.load();
            
            // General settings
            Property maxDistanceProp = config.get(CATEGORY_GENERAL, "maxPickupDistance", -1.0);
            maxDistanceProp.comment = "Maximum distance for pickup redirection. Set to -1 for unlimited range. Default: -1.0 (unlimited)";
            maxPickupDistance = maxDistanceProp.getDouble();
            
            Property enableSoundsProp = config.get(CATEGORY_GENERAL, "enableSounds", true);
            enableSoundsProp.comment = "Enable sound effects when selecting inventories. Default: true";
            enableSounds = enableSoundsProp.getBoolean();
            

            
            // Visual settings
            Property enableVisualProp = config.get(CATEGORY_VISUAL, "enableVisualEffects", true);
            enableVisualProp.comment = "Enable visual inventory highlighting when holding the PickThere item. Default: true";
            enableVisualEffects = enableVisualProp.getBoolean();
            
            Property showAdvancedTooltipsProp = config.get(CATEGORY_VISUAL, "showAdvancedTooltips", true);
            showAdvancedTooltipsProp.comment = "Show detailed position information in tooltips. Default: true";
            showAdvancedTooltips = showAdvancedTooltipsProp.getBoolean();
            
            Property maxTooltipPositionsProp = config.get(CATEGORY_VISUAL, "maxTooltipPositions", 5);
            maxTooltipPositionsProp.comment = "Maximum number of positions to show in tooltips. Default: 5";
            maxTooltipPositions = maxTooltipPositionsProp.getInt();
            
            Property outlineOpacityProp = config.get(CATEGORY_VISUAL, "outlineOpacity", 0.8);
            outlineOpacityProp.comment = "Opacity of inventory outlines (0.0 to 1.0). Default: 0.8";
            outlineOpacity = (float) outlineOpacityProp.getDouble();
            
            Property maxRenderDistanceProp = config.get(CATEGORY_VISUAL, "maxRenderDistance", -1.0);
            maxRenderDistanceProp.comment = "Maximum distance to render inventory outlines. Set to -1 for unlimited range. Default: -1.0 (unlimited)";
            maxRenderDistance = maxRenderDistanceProp.getDouble();
            
        } catch (Exception e) {
            // Config loading failed, use defaults
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals("pickthere")) {
            loadConfig();
        }
    }
    
    public static Configuration getConfig() {
        return config;
    }
}