package org.xXseesXx.pickthere.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.xXseesXx.pickthere.Pickthere;

public class ModRecipes {
    
    public static void registerRecipes() {
        // Main crafting recipe - chest (left bottom), hopper (center), ender pearl (top right)
        GameRegistry.addRecipe(new ItemStack(Pickthere.pickThereItem, 1),
            "  E",
            " H ", 
            "C  ",
            'C', Blocks.chest,
            'H', Blocks.hopper,
            'E', Items.ender_pearl
        );
        
        // Reset recipe - simple shapeless recipe that clears NBT data
        GameRegistry.addRecipe(new PickThereResetRecipe());
        
        // Register the custom recipe with RecipeSorter to avoid warnings
        net.minecraftforge.oredict.RecipeSorter.register("pickthere:reset", PickThereResetRecipe.class, 
            net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
    }
}