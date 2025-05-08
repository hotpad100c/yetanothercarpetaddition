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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class RandomTickVisualizing {
    public static ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Integer>> visualizers = new ConcurrentHashMap<>();
    public static int SURVIVE_TIME = 20;
    public static int RANGE = 20;

    public static void setVisualizer(World world, BlockPos pos) {
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
            nbt.put("block_state", NbtHelper.fromBlockState(Blocks.RED_STAINED_GLASS.getDefaultState()));
            float scale = 0.9f;
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", 0xFFAAAA);
            entity.readNbt(nbt);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.noClip = true;
            entity.setYaw(0);
            entity.setPos(pos.getX() + 0.5 - scale, pos.getY() + 0.5 -scale, pos.getZ() + 0.5 -scale);
            entity.addCommandTag("randomTickVisualizer");
            if (world instanceof ServerWorld serverWorld) {
                addMarkerToTeam(serverWorld, "randomTickVisualizerTeam", entity);
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
        if (!YetAnotherCarpetAdditionRules.randomTickVisualize || !CarpetServer.minecraft_server.getTickManager().shouldTick())
            return;
        visualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity entity = entry.getKey();
            int time = entry.getValue();
            if (time > 0) {
                visualizers.put(pos, Map.entry(entity, time - 1));
            } else {
                entity.discard();
                visualizers.remove(pos);
            }
        });
    }

    public static void clearVisualizers(MinecraftServer server) {
        visualizers.clear();
        clearWorldVisualizers(server.getWorld(World.OVERWORLD));
        clearWorldVisualizers(server.getWorld(World.NETHER));
        clearWorldVisualizers(server.getWorld(World.END));
    }

    public static void clearWorldVisualizers(ServerWorld world) {
        if (world!= null) {
            List<DisplayEntity.BlockDisplayEntity> entities = new ArrayList<>();
            Predicate<DisplayEntity.BlockDisplayEntity> predicate = bd -> bd.getCommandTags().contains("randomTickVisualizer");
            world.collectEntitiesByType(EntityType.BLOCK_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
        }
    }
}
