package org.xXseesXx.pickthere.client;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xXseesXx.pickthere.Pickthere;

@Mod.EventBusSubscriber(modid = Pickthere.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            InventoryRenderer.renderSelectedInventories(event);
        }
    }
}