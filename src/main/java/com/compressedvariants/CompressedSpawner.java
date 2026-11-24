package com.compressedvariants;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Rolls a tier when a whitelisted mob is loaded and applies the 9x / 81x
 * attribute modifiers. The command tag makes the roll happen once per mob.
 */
public final class CompressedSpawner {
    public static final int TIER_ONE = 1;
    public static final int TIER_TWO = 2;
    public static final int TIER_THREE = 3;

    // Of every natural whitelisted spawn: 50% tier 1, 25% tier 2, 25% tier 3.
    // Tier 2 and tier 3 are half as likely as tier 1, and no plain mob is left.
    public static final double TIER_ONE_CHANCE = 0.50D;
    public static final double TIER_TWO_CHANCE = 0.25D;
    public static final double TIER_THREE_CHANCE = 0.25D;

    private static final String[] LOW_MATERIALS = {
            "wood", "stone", "coal", "lapis", "redstone", "gold"
    };
    private static final String[] ARMOR_PIECES = {
            "helmet", "chestplate", "leggings", "boots"
    };
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final Item[] IRON_ARMOR = {
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
    };
    private static final Item[] DIAMOND_ARMOR = {
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
    };

    public static final String CHECKED_TAG = "compressed_variants_checked";

    private static final Identifier HEALTH_MODIFIER_ID =
            Identifier.of(CompressedVariants.MOD_ID, "tier_health");
    private static final Identifier ATTACK_MODIFIER_ID =
            Identifier.of(CompressedVariants.MOD_ID, "tier_attack");
    private static final Identifier ARMOR_MODIFIER_ID =
            Identifier.of(CompressedVariants.MOD_ID, "tier_armor");

    private CompressedSpawner() {
    }

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register(CompressedSpawner::onEntityLoad);
    }

    private static void onEntityLoad(net.minecraft.entity.Entity entity, ServerWorld world) {
        if (!(entity instanceof MobEntity mob)) {
            return;
        }

        VariantType type = VariantType.byType(mob.getType());
        if (type == null) {
            return;
        }
        CompressedTierAccess access = (CompressedTierAccess) mob;
        int tier = access.compressed$getTier();

        // Migrate compressed mobs saved by an older version: they already have
        // a tier, but no fixed attack override yet.
        if (tier > 0) {
            if (access.compressed$getAttackDamageOverride() <= 0.0F) {
                applyTier(mob, type, tier);
            }
            return;
        }

        if (mob.getCommandTags().contains(CHECKED_TAG)) {
            return;
        }

        if (tier <= 0) {
            double roll = mob.getRandom().nextDouble();
            if (roll < TIER_ONE_CHANCE) {
                tier = TIER_ONE;
            } else if (roll < TIER_ONE_CHANCE + TIER_TWO_CHANCE) {
                tier = TIER_TWO;
            } else {
                tier = TIER_THREE;
            }
        }

        if (tier > 0) {
            applyTier(mob, type, tier);
        }
        mob.addCommandTag(CHECKED_TAG);
    }

    /**
     * Applies tiers 1..3. Health is a per-mob random value plus the armor
     * overflow bonus; attack/armor keep the 9x or 81x scaling. Tier 3 keeps
     * tier 2's combat multipliers and rolls 9000-13500 base health.
     */
    public static void applyTier(MobEntity mob, VariantType type, int tier) {
        tier = Math.max(0, Math.min(TIER_THREE, tier));
        if (tier == 0) {
            clearTier(mob);
            return;
        }

        CompressedTierAccess access = (CompressedTierAccess) mob;
        int oldTier = access.compressed$getTier();
        float existingMaxHealth = access.compressed$getMaxHealthOverride();
        float existingAttackDamage = access.compressed$getAttackDamageOverride();

        removeModifiers(mob);
        access.compressed$setTier(tier);

        double combatMultiplier = tier >= TIER_TWO ? 80.0D : 8.0D;
        addModifier(mob, EntityAttributes.GENERIC_ARMOR, ARMOR_MODIFIER_ID, combatMultiplier);

        // Re-apply on chunk load must not reroll the health or damage of an existing mob.
        boolean preserveHealth = oldTier == tier && existingMaxHealth > 0.0F;
        if (!preserveHealth) {
            if (type.humanoid()) {
                equipArmorSet(mob, tier);
            }
            float baseHealth = rollBaseHealth(mob, tier);
            float armorBonus = armorOverflowBonus(mob);
            access.compressed$setMaxHealthOverride(baseHealth + armorBonus);
        }
        if (!(oldTier == tier && existingAttackDamage > 0.0F)) {
            access.compressed$setAttackDamageOverride(rollAttackDamage(mob, tier));
        }

        mob.setHealth(mob.getMaxHealth());
    }

    private static float rollAttackDamage(MobEntity mob, int tier) {
        float roll = mob.getRandom().nextFloat();
        return switch (tier) {
            case TIER_THREE -> 50.0F + roll * 25.0F;
            case TIER_TWO -> 25.0F + roll * 25.0F;
            default -> 15.0F + roll * 10.0F;
        };
    }

    private static float rollBaseHealth(MobEntity mob, int tier) {
        float roll = mob.getRandom().nextFloat();
        return switch (tier) {
            case TIER_THREE -> 9000.0F + roll * 4500.0F;
            case TIER_TWO -> 1000.0F + roll * 500.0F;
            default -> 150.0F + roll * 100.0F;
        };
    }

    /**
     * Same 4:1 overflow conversion used by the armor mod: armor above 20 adds
     * (armor - 20) / 2 to the mob's max health.
     */
    private static float armorOverflowBonus(MobEntity mob) {
        int armor = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (mob.getEquippedStack(slot).getItem() instanceof ArmorItem armorItem) {
                armor += armorItem.getProtection();
            }
        }
        return Math.max(0, armor - 20) / 2.0F;
    }

    /**
     * Tier 1 can wear iron or tier-1 compressed low-level gear; tier 2 and 3
     * wear diamond or tier-2 compressed low-level gear. The compressed pieces
     * come from ultimate_compressed_gear if that mod is installed; otherwise
     * they fall back to vanilla armor.
     */
    private static void equipArmorSet(MobEntity mob, int tier) {
        int gearTier = tier >= TIER_TWO ? 2 : 1;
        Item[] fallback = tier >= TIER_TWO ? DIAMOND_ARMOR : IRON_ARMOR;
        boolean useCompressed = mob.getRandom().nextBoolean();
        String material = LOW_MATERIALS[mob.getRandom().nextInt(LOW_MATERIALS.length)];

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            Item item = fallback[i];
            if (useCompressed) {
                Item candidate = Registries.ITEM.get(Identifier.of(
                        "ultimate_compressed_gear",
                        material + "_" + ARMOR_PIECES[i] + "_" + gearTier));
                if (candidate != Items.AIR) {
                    item = candidate;
                }
            }
            equipArmor(mob, item, ARMOR_SLOTS[i]);
        }
    }

    private static void clearTier(MobEntity mob) {
        CompressedTierAccess access = (CompressedTierAccess) mob;
        removeModifiers(mob);
        access.compressed$setTier(0);
        access.compressed$setMaxHealthOverride(0.0F);
        access.compressed$setAttackDamageOverride(0.0F);
        if (mob.getHealth() > mob.getMaxHealth()) {
            mob.setHealth(mob.getMaxHealth());
        }
    }

    private static void removeModifiers(MobEntity mob) {
        removeModifier(mob, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_MODIFIER_ID);
        removeModifier(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, ATTACK_MODIFIER_ID);
        removeModifier(mob, EntityAttributes.GENERIC_ARMOR, ARMOR_MODIFIER_ID);
    }

    private static void addModifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute,
                                    Identifier id, double value) {
        EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addPersistentModifier(new EntityAttributeModifier(
                id, value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeModifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute,
                                       Identifier id) {
        EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void equipArmor(MobEntity mob, Item item, EquipmentSlot slot) {
        if (mob.getEquippedStack(slot).isEmpty()) {
            mob.equipStack(slot, new ItemStack(item));
            mob.setEquipmentDropChance(slot, 0.05F);
        }
    }
}
