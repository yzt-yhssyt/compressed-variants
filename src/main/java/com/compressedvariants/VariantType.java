package com.compressedvariants;

import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * The vanilla mobs that can spawn as a compressed variant. The id is also the
 * texture basename under assets/compressed_variants/textures/entity/compressed/.
 */
public enum VariantType {
    ZOMBIE("zombie", EntityType.ZOMBIE, true),
    HUSK("husk", EntityType.HUSK, true),
    DROWNED("drowned", EntityType.DROWNED, true),
    ZOMBIE_VILLAGER("zombie_villager", EntityType.ZOMBIE_VILLAGER, true),
    ZOMBIFIED_PIGLIN("zombified_piglin", EntityType.ZOMBIFIED_PIGLIN, true),

    SKELETON("skeleton", EntityType.SKELETON, true),
    STRAY("stray", EntityType.STRAY, true),
    WITHER_SKELETON("wither_skeleton", EntityType.WITHER_SKELETON, true),
    BOGGED("bogged", EntityType.BOGGED, true),

    SPIDER("spider", EntityType.SPIDER, false),
    CAVE_SPIDER("cave_spider", EntityType.CAVE_SPIDER, false),

    ENDERMAN("enderman", EntityType.ENDERMAN, false),
    CREEPER("creeper", EntityType.CREEPER, false);

    private static final Map<EntityType<?>, VariantType> BY_TYPE = new HashMap<>();

    static {
        for (VariantType type : values()) {
            BY_TYPE.put(type.entityType, type);
        }
    }

    private final String id;
    private final EntityType<?> entityType;
    private final boolean humanoid;

    VariantType(String id, EntityType<?> entityType, boolean humanoid) {
        this.id = id;
        this.entityType = entityType;
        this.humanoid = humanoid;
    }

    public String id() {
        return id;
    }

    public EntityType<?> entityType() {
        return entityType;
    }

    public boolean humanoid() {
        return humanoid;
    }

    public static VariantType byType(EntityType<?> type) {
        return BY_TYPE.get(type);
    }
}
