package org.xXseesXx.pickthere.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xXseesXx.pickthere.Pickthere;
import org.xXseesXx.pickthere.items.PickThereItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Pickthere.MODID);

    public static final RegistryObject<Item> PICK_THERE_ITEM = ITEMS.register("pick_there_item",
            () -> new PickThereItem(new Item.Properties().stacksTo(1)));
}