package org.xXseesXx.pickthere;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.xXseesXx.pickthere.client.ClientSetup;
import org.xXseesXx.pickthere.config.PickThereConfig;
import org.xXseesXx.pickthere.init.ModCreativeTabs;
import org.xXseesXx.pickthere.init.ModItems;

@Mod(Pickthere.MODID)
public class Pickthere {
    public static final String MODID = "pickthere";

    public Pickthere() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register config
        PickThereConfig.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        
    }
}
