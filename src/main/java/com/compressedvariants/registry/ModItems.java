package com.compressedvariants.registry;

import com.compressedvariants.CompressedVariants;
import com.compressedvariants.VariantType;
import com.compressedvariants.item.CompressedSpawnEggItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawn eggs for every compressed variant. There is one egg per tier so the
 * exact variant can be placed without relying on the natural-spawn roll.
 */
public final class ModItems {
    public static final List<Item> SPAWN_EGGS = new ArrayList<>();

    private static final int TIER_ONE_PRIMARY = 0x17142B;
    private static final int TIER_ONE_SECONDARY = 0x54E6FF;
    private static final int TIER_TWO_PRIMARY = 0x7A1FA2;
    private static final int TIER_TWO_SECONDARY = 0xE0A0FF;
    private static final int TIER_THREE_PRIMARY = 0x0B3B12;
    private static final int TIER_THREE_SECONDARY = 0x3FE05A;

    private static ItemGroup spawnEggGroup;

    private ModItems() {
    }

    public static void init() {
        for (VariantType type : VariantType.values()) {
            registerEgg(type, 1, TIER_ONE_PRIMARY, TIER_ONE_SECONDARY);
            registerEgg(type, 2, TIER_TWO_PRIMARY, TIER_TWO_SECONDARY);
            registerEgg(type, 3, TIER_THREE_PRIMARY, TIER_THREE_SECONDARY);
        }

        spawnEggGroup = Registry.register(Registries.ITEM_GROUP,
                Identifier.of(CompressedVariants.MOD_ID, "spawn_eggs"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.compressed_variants.spawn_eggs"))
                        .icon(() -> new ItemStack(SPAWN_EGGS.get(SPAWN_EGGS.size() - 1)))
                        .entries((context, entries) -> SPAWN_EGGS.forEach(entries::add))
                        .build());
    }

    private static void registerEgg(VariantType type, int tier, int primary, int secondary) {
        String id = type.id() + "_spawn_egg_" + tier;
        Item item = new CompressedSpawnEggItem(type, tier, primary, secondary, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(CompressedVariants.MOD_ID, id), item);
        SPAWN_EGGS.add(item);
    }
}
