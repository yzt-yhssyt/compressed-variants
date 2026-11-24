package com.compressedvariants;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/** Testing helper: /compressedvariants set <tier> affects nearby whitelisted mobs. */
public final class CompressedCommand {
    private CompressedCommand() {
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("compressedvariants")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("tier", IntegerArgumentType.integer(0, 3))
                                .executes(context -> setTier(context,
                                        IntegerArgumentType.getInteger(context, "tier"))))));
    }

    private static int setTier(CommandContext<ServerCommandSource> context, int tier)
            throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        List<MobEntity> mobs = player.getServerWorld().getEntitiesByClass(
                MobEntity.class,
                player.getBoundingBox().expand(16.0D),
                mob -> VariantType.byType(mob.getType()) != null);

        for (MobEntity mob : mobs) {
            CompressedSpawner.applyTier(mob, VariantType.byType(mob.getType()), tier);
        }

        context.getSource().sendFeedback(
                () -> Text.translatable("command.compressed_variants.set", mobs.size(), tier), false);
        return mobs.size();
    }
}
