package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class BlockUpdateVisualizing {
    public static ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> NCvisualizers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> PPvisualizers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> CPvisualizers = new ConcurrentHashMap<>();

    public static int SURVIVE_TIME = 20;
    public static int RANGE = 40;

    public enum UpdateType {
        NC("NCVisualizer", 0xff4f00),
        PP("PPVisualizer", 0x00ffff),
        CP("CPVisualizer", 0xffffed);
        public String tagName;
        public int color;

        UpdateType(String s, int i) {
            tagName = s;
            color = i;
        }
    }

    public static void setVisualizer(World world, BlockPos pos, UpdateType updateType) {
        switch (updateType) {
            case NC -> setVisualizer(world, pos, updateType, NCvisualizers);
            case PP -> setVisualizer(world, pos, updateType, PPvisualizers);
            case CP -> setVisualizer(world, pos, updateType, CPvisualizers);
        }
    }

    public static void setVisualizer(World world, BlockPos pos, UpdateType updateType, ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> visualizers) {
        boolean playersNearBy = false;
        for (PlayerEntity player : CarpetServer.minecraft_server.getPlayerManager().players) {
            if (player.getPos().distanceTo(pos.toCenterPos()) < RANGE) {
                playersNearBy = true;
                break;
            }
        }
        if (!playersNearBy) return;
        if (visualizers.containsKey(pos)) {
            visualizers.put(pos, Map.entry(visualizers.get(pos).getKey(), SURVIVE_TIME));
        } else {
            DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            entity.setNoGravity(true);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.put("block_state", NbtHelper.fromBlockState(Blocks.GLASS.getDefaultState()));
            float scale = 1.002f;
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", updateType.color);
            entity.readNbt(nbt);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.noClip = true;
            entity.setYaw(0);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(updateType.tagName);
            entity.addCommandTag("DoNotTick");
            if (world instanceof ServerWorld serverWorld) {
                addMarkerToTeam(serverWorld, updateType.tagName, entity);
            }
            world.spawnEntity(entity);

            visualizers.put(pos, Map.entry(entity, SURVIVE_TIME));
        }
    }

    private static void addMarkerToTeam(ServerWorld world, String teamName, DisplayEntity.BlockDisplayEntity marker) {
        Scoreboard scoreboard = world.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);

            team.setColor(Formatting.RED);
        }
        String entityName = marker.getUuidAsString();
        scoreboard.addScoreHolderToTeam(entityName, team);
    }

    public static void updateVisualizer() {
        if (!CarpetServer.minecraft_server.getTickManager().shouldTick())
            return;
        NCvisualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity entity = entry.getKey();
            int time = entry.getValue();
            if (time > 0) {
                NCvisualizers.put(pos, Map.entry(entity, time - 1));
            } else {
                entity.discard();
                NCvisualizers.remove(pos);
            }
        });
        PPvisualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity entity = entry.getKey();
            int time = entry.getValue();
            if (time > 0) {
                PPvisualizers.put(pos, Map.entry(entity, time - 1));
            } else {
                entity.discard();
                PPvisualizers.remove(pos);
            }
        });
        CPvisualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity entity = entry.getKey();
            int time = entry.getValue();
            if (time > 0) {
                CPvisualizers.put(pos, Map.entry(entity, time - 1));
            } else {
                entity.discard();
                CPvisualizers.remove(pos);
            }
        });
    }

    public static void clearVisualizers(ServerCommandSource source, UpdateType updateType) {
        ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> visualizers;
        switch (updateType) {
            case NC -> {
                visualizers = NCvisualizers;
                NCvisualizers.clear();
            }
            case PP -> {
                visualizers = PPvisualizers;
                PPvisualizers.clear();
            }
            case CP -> {
                visualizers = CPvisualizers;
                CPvisualizers.clear();
            }
            default -> throw new IllegalArgumentException("Unknown UpdateType: " + updateType);
        }
        visualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity entity = entry.getKey();
            if (!entity.isRemoved()) {
                entity.discard();
            }
        });
        Predicate<DisplayEntity.BlockDisplayEntity> predicate = bd -> bd.getCommandTags().contains(updateType.tagName);
        List<DisplayEntity.BlockDisplayEntity> entities = new ArrayList<>();
        source.getWorld().collectEntitiesByType(EntityType.BLOCK_DISPLAY,
                predicate,
                entities);
        entities.forEach(Entity::discard);
    }
}
