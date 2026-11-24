package com.compressedvariants;

import com.compressedvariants.registry.ModItems;
import net.fabricmc.api.ModInitializer;

public class CompressedVariants implements ModInitializer {
    public static final String MOD_ID = "compressed_variants";

    @Override
    public void onInitialize() {
        ModItems.init();
        CompressedSpawner.init();
        CompressedCommand.init();
    }
}
