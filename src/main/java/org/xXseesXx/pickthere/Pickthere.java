package org.xXseesXx.pickthere;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import org.xXseesXx.pickthere.client.ClientSetup;
import org.xXseesXx.pickthere.config.PickThereConfig;
import org.xXseesXx.pickthere.events.ItemPickupHandler;
import org.xXseesXx.pickthere.items.PickThereItem;
import org.xXseesXx.pickthere.recipes.ModRecipes;

@Mod(modid = Pickthere.MODID, name = "Pick There", version = "1.0.0")
public class Pickthere {
    public static final String MODID = "pickthere";
    
    // Items
    public static Item pickThereItem;
    
    // Creative tab
    public static CreativeTabs pickThereTab = new CreativeTabs("pickthere") {
        @Override
        public Item getTabIconItem() {
            return pickThereItem;
        }
    };
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize configuration
        PickThereConfig.init(event.getSuggestedConfigurationFile());
        
        // Register items
        pickThereItem = new PickThereItem()
            .setUnlocalizedName("pickThereItem")
            .setCreativeTab(pickThereTab)
            .setMaxStackSize(1);
        
        GameRegistry.registerItem(pickThereItem, "pick_there_item");
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new ItemPickupHandler());
        MinecraftForge.EVENT_BUS.register(new PickThereConfig());
        
        // Register recipes
        ModRecipes.registerRecipes();
        
        // Initialize client-side features
        if (event.getSide().isClient()) {
            initClient();
        }
    }
    
    @SideOnly(Side.CLIENT)
    private void initClient() {
        ClientSetup.init();
    }
}