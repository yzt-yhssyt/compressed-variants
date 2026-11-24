package com.compressedvariants;

/**
 * Implemented on {@link net.minecraft.entity.LivingEntity} by {@code LivingEntityMixin}.
 * Tier 0 is a normal mob, 1 is tier one, 2 is tier two, 3 is tier three.
 */
public interface CompressedTierAccess {
    int compressed$getTier();

    void compressed$setTier(int tier);

    /** Overrides the vanilla 1024 max-health cap for compressed mobs. */
    float compressed$getMaxHealthOverride();

    void compressed$setMaxHealthOverride(float maxHealth);

    /** Fixed attack damage independent of the world difficulty. */
    float compressed$getAttackDamageOverride();

    void compressed$setAttackDamageOverride(float attackDamage);
}
