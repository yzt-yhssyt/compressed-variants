package com.compressedvariants.mixin;

import com.compressedvariants.CompressedTierAccess;
import com.compressedvariants.CompressedVariants;
import com.compressedvariants.VariantType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void compressedvariants$replaceTexture(LivingEntity entity, boolean showBody,
                                                   boolean translucent, boolean showOutline,
                                                   CallbackInfoReturnable<RenderLayer> cir) {
        if (!showBody) {
            return;
        }

        int tier = ((CompressedTierAccess) entity).compressed$getTier();
        if (tier <= 0) {
            return;
        }

        VariantType type = VariantType.byType(entity.getType());
        if (type == null) {
            return;
        }

        Identifier texture = Identifier.of(CompressedVariants.MOD_ID,
                "textures/entity/compressed/" + type.id() + "_t" + tier + ".png");
        cir.setReturnValue(RenderLayer.getEntityCutoutNoCull(texture));
    }
}
