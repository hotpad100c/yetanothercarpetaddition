package mypals.ml.features.moreCommandOperations;

import mypals.ml.YetAnotherCarpetAdditionServer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.RideCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import static mypals.ml.features.moreCommandOperations.WorldEventMapper.WORLD_EVENT_MAP;

public class ExtraVaniallaCommandFeatureManager {
    public static void addBlockEvent(ServerCommandSource source, BlockPos pos, Block block, int type, int data) {
        source.getWorld().addSyncedBlockEvent(pos, block, type, data);
        source.sendFeedback(() -> Text.literal("BlockEvent for [" + Text.translatable(block.getTranslationKey()).getString() + "] was emitted at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "] with type [" + type + "] and data[" + data + "]."), true);

    }

    public static int addGameEvent(ServerCommandSource source, Vec3d pos, String reason, @Nullable Entity entity, @Nullable BlockState blockState) {
        RegistryEntry<GameEvent> event = Registries.GAME_EVENT.getEntry(Identifier.of("minecraft:" + reason)).orElse(null);
        if (event == null) {
            source.sendError(Text.literal("Unknown GameEvent: " + reason));
            return 0;
        }
        source.getWorld().emitGameEvent(event, pos, new GameEvent.Emitter(entity, blockState));
        source.sendFeedback(() -> Text.literal("GameEvent <" + reason + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]" +
                (entity == null ? " " : (" by entity <" + entity.getName() + ">")) + (blockState == null ? " " : (" with block [" +
                Text.translatable(blockState.getBlock().getTranslationKey()).getString() + "]"))), true);

        return 1;
    }

    public static Integer getEventId(String eventName) {
        return WORLD_EVENT_MAP.get(eventName);
    }

    public static void addRandomTick(ServerCommandSource source, BlockPos pos) {
        ServerWorld serverWorld = source.getWorld();
        serverWorld.getBlockState(pos).randomTick(serverWorld, pos, serverWorld.getRandom());
        YetAnotherCarpetAdditionServer.randomTickVisualizing.setVisualizer(serverWorld, pos, pos.toCenterPos(), "-");
        source.sendFeedback(() -> Text.literal("Simulated a RandomTick event at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "]."), true);


    }

    public static void addWorldEvent(ServerCommandSource source, BlockPos pos, String id, @Nullable PlayerEntity player, int data) {
        int eventId = 1000;
        try {
            eventId = getEventId(id);
        } catch (Exception e) {
            source.sendError(Text.literal("Unknown WorldEvent: " + id));
        }
        boolean global = eventId == 1023 || eventId == 1028 || eventId == 1038;
        if (global) {
            source.getWorld().syncGlobalEvent(eventId, pos, data);
            source.sendFeedback(() -> Text.literal("<GLOBAL>WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        } else {
            source.getWorld().syncWorldEvent(player, eventId, pos, data);
            source.sendFeedback(() -> Text.literal("WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        }
    }
}
