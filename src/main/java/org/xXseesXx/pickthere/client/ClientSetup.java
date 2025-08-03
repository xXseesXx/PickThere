package org.xXseesXx.pickthere.client;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;

public class ClientSetup {
    
    public static void init() {
        // Create and register the renderer for visual effects
        DebugRenderer renderer = new DebugRenderer();
        
        // Register with MinecraftForge EVENT_BUS for world rendering events
        MinecraftForge.EVENT_BUS.register(renderer);
        
        // Also register with FML bus for tick events if needed
        FMLCommonHandler.instance().bus().register(renderer);
    }
}