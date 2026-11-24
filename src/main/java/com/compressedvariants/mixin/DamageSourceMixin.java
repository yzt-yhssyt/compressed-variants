package com.compressedvariants.mixin;

import com.compressedvariants.CompressedTierAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Compressed mob damage is a fixed per-tier roll, so it must not be scaled by
 * the world difficulty (0.5x easy, 1.5x hard).
 */
@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
    @Inject(method = "isScaledWithDifficulty", at = @At("HEAD"), cancellable = true)
    private void compressedvariants$disableDifficultyScaling(CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = ((DamageSource) (Object) this).getAttacker();
        if (attacker instanceof CompressedTierAccess access && access.compressed$getTier() > 0) {
            cir.setReturnValue(false);
        }
    }
}
