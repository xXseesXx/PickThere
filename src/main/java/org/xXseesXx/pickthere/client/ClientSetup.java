package org.xXseesXx.pickthere.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.xXseesXx.pickthere.Pickthere;

@Mod.EventBusSubscriber(modid = Pickthere.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::clientSetup);
    }
    
    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            org.xXseesXx.pickthere.items.PickThereItem.registerItemProperties();
        });
    }
}