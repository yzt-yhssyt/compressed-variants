package com.compressedvariants.item;

import com.compressedvariants.CompressedSpawner;
import com.compressedvariants.VariantType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A spawn egg that creates the matching compressed variant directly, instead of
 * waiting for the natural-spawn roll.
 */
public class CompressedSpawnEggItem extends SpawnEggItem {
    private final VariantType variant;
    private final int tier;

    @SuppressWarnings("unchecked")
    public CompressedSpawnEggItem(VariantType variant, int tier, int primaryColor, int secondaryColor,
                                  Settings settings) {
        super((net.minecraft.entity.EntityType<? extends MobEntity>) variant.entityType(),
                primaryColor, secondaryColor, settings);
        this.variant = variant;
        this.tier = tier;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld server)) {
            return ActionResult.SUCCESS;
        }

        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.PASS;
        }

        BlockPos pos = context.getBlockPos().offset(context.getSide());
        MobEntity mob = (MobEntity) variant.entityType().spawnFromItemStack(
                server, context.getStack(), player, pos, SpawnReason.SPAWN_EGG, false, false);
        if (mob != null) {
            CompressedSpawner.applyTier(mob, variant, tier);
            mob.addCommandTag(CompressedSpawner.CHECKED_TAG);
        }

        if (!player.getAbilities().creativeMode) {
            context.getStack().decrement(1);
        }
        return ActionResult.SUCCESS;
    }
}
