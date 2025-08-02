package org.xXseesXx.pickthere.init;

import org.xXseesXx.pickthere.Pickthere;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Pickthere.MODID);

    public static final RegistryObject<CreativeModeTab> PICK_THERE_TAB = CREATIVE_MODE_TABS.register("pick_there_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pickthere"))
                    .icon(() -> new ItemStack(ModItems.PICK_THERE_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PICK_THERE_ITEM.get());
                    })
                    .build());
}