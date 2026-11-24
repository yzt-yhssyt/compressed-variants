package com.compressedvariants.mixin;

import com.compressedvariants.CompressedTierAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CompressedTierAccess {
    @Unique
    private static final TrackedData<Integer> COMPRESSED_TIER =
            DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private static final String TIER_NBT_KEY = "CompressedVariantsTier";

    @Unique
    private static final String MAX_HEALTH_NBT_KEY = "CompressedVariantsMaxHealth";

    @Unique
    private static final String ATTACK_DAMAGE_NBT_KEY = "CompressedVariantsAttackDamage";

    @Unique
    private float compressed$maxHealthOverride;

    @Unique
    private float compressed$attackDamageOverride;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void compressedvariants$addTierTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(COMPRESSED_TIER, 0);
    }

    @Override
    public int compressed$getTier() {
        return ((LivingEntity) (Object) this).getDataTracker().get(COMPRESSED_TIER);
    }

    @Override
    public void compressed$setTier(int tier) {
        ((LivingEntity) (Object) this).getDataTracker().set(COMPRESSED_TIER, Math.max(0, Math.min(3, tier)));
    }

    @Override
    public float compressed$getMaxHealthOverride() {
        return compressed$maxHealthOverride;
    }

    @Override
    public void compressed$setMaxHealthOverride(float maxHealth) {
        compressed$maxHealthOverride = Math.max(0.0F, maxHealth);
    }

    @Override
    public float compressed$getAttackDamageOverride() {
        return compressed$attackDamageOverride;
    }

    @Override
    public void compressed$setAttackDamageOverride(float attackDamage) {
        compressed$attackDamageOverride = Math.max(0.0F, attackDamage);
    }

    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
    private void compressedvariants$useMaxHealthOverride(CallbackInfoReturnable<Float> cir) {
        if (compressed$maxHealthOverride > 0.0F) {
            cir.setReturnValue(compressed$maxHealthOverride);
        }
    }

    @Inject(method = "getAttributeValue", at = @At("RETURN"), cancellable = true)
    private void compressedvariants$useAttackDamageOverride(RegistryEntry<EntityAttribute> attribute,
                                                            CallbackInfoReturnable<Double> cir) {
        if (attribute == EntityAttributes.GENERIC_ATTACK_DAMAGE && compressed$attackDamageOverride > 0.0F) {
            cir.setReturnValue((double) compressed$attackDamageOverride);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void compressedvariants$writeTier(NbtCompound nbt, CallbackInfo ci) {
        int tier = compressed$getTier();
        if (tier > 0) {
            nbt.putInt(TIER_NBT_KEY, tier);
        }
        if (compressed$maxHealthOverride > 0.0F) {
            nbt.putFloat(MAX_HEALTH_NBT_KEY, compressed$maxHealthOverride);
        }
        if (compressed$attackDamageOverride > 0.0F) {
            nbt.putFloat(ATTACK_DAMAGE_NBT_KEY, compressed$attackDamageOverride);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void compressedvariants$readTier(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(TIER_NBT_KEY)) {
            compressed$setTier(nbt.getInt(TIER_NBT_KEY));
        }
        if (nbt.contains(MAX_HEALTH_NBT_KEY)) {
            compressed$setMaxHealthOverride(nbt.getFloat(MAX_HEALTH_NBT_KEY));
        }
        if (nbt.contains(ATTACK_DAMAGE_NBT_KEY)) {
            compressed$setAttackDamageOverride(nbt.getFloat(ATTACK_DAMAGE_NBT_KEY));
        }
    }
}
