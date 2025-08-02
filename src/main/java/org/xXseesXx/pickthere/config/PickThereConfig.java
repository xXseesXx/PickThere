package org.xXseesXx.pickthere.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PickThereConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue MAX_PICKUP_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PICKUP_SOUND;
    public static final ForgeConfigSpec.BooleanValue SHOW_PICKUP_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue SHOW_SELECTED_BLOCK_OUTLINES;

    static {
        BUILDER.push("General Settings");
        
        MAX_PICKUP_DISTANCE = BUILDER
            .comment("Maximum pickup distance (-1 for unlimited distance)")
            .defineInRange("maxPickupDistance", -1.0, -1.0, 64.0);
            
        ENABLE_PICKUP_SOUND = BUILDER
            .comment("Enable pickup sound effects")
            .define("enablePickupSound", true);
            
        SHOW_PICKUP_PARTICLES = BUILDER
            .comment("Show pickup particle effects")
            .define("showPickupParticles", true);
            
        SHOW_SELECTED_BLOCK_OUTLINES = BUILDER
            .comment("Show outlines around selected blocks")
            .define("showSelectedBlockOutlines", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }
}