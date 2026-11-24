package com.compressedvariants.client;

import net.fabricmc.api.ClientModInitializer;

public class CompressedVariantsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Rendering is handled by LivingEntityRendererMixin, which is loaded client-side.
    }
}
