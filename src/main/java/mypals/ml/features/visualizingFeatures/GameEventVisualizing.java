package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GameEventVisualizing {
    public static ConcurrentHashMap<Vec3d, Map.Entry<GameEventObject, Integer>> visualizers = new ConcurrentHashMap<>();
    public static int SURVIVE_TIME = 100;

    public static class GameEventObject {

        public String type;
        public DisplayEntity.TextDisplayEntity tickMarker;
        public DisplayEntity.BlockDisplayEntity typeMarker;

        public GameEventObject(ServerWorld world, Vec3d pos, String emitter, String type) {
            setVisualizer(world, pos, emitter, type);
        }

        public void setVisualizer(ServerWorld world, Vec3d pos, String trigger, String type) {
            if (tickMarker != null && !tickMarker.isRemoved()) {
                NbtCompound nbt = tickMarker.writeNbt(new NbtCompound());
                String textJson = "{\"text\":\"\",\"extra\":[" +
                        "{\"text\":\"" + trigger.replace("\"", "\\\"") + "\",\"color\":\"blue\"}," +
                        "{\"text\":\"\\n" + type.replace("\"", "\\\"") + "\",\"color\":\"blue\"}" +
                        "]}";
                nbt.remove("text");
                nbt.putString("text", textJson);
                tickMarker.readNbt(nbt);
            } else {
                tickMarker = summon(world, pos, trigger, type);
            }

            if (typeMarker == null) {
                typeMarker = summonMarker(world, pos);
            }

        }

        public void removeVisualizer() {
            if (tickMarker != null) {
                tickMarker.discard();
            }
            if (typeMarker != null) {
                typeMarker.discard();
            }
        }

        public static DisplayEntity.TextDisplayEntity summon(ServerWorld world, Vec3d pos, String trigger, String type) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.putString("billboard", "center");
            String textJson = "{\"text\":\"\",\"extra\":[" +
                    "{\"text\":\"" + trigger.replace("\"", "\\\"") + "\",\"color\":\"blue\"}," +
                    "{\"text\":\"\\n" + type.replace("\"", "\\\"") + "\",\"color\":\"blue\"}" +
                    "]}";
            nbt.putInt("background", 0x00000000);

            nbt.putString("text", textJson);
            entity.readNbt(nbt);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag("gameEventVisualizer");
            world.spawnEntity(entity);
            return entity;
        }

        public static DisplayEntity.BlockDisplayEntity summonMarker(World world, Vec3d pos) {
            DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.put("block_state", NbtHelper.fromBlockState(Blocks.BLUE_STAINED_GLASS_PANE.getDefaultState()));
            nbt = EntityHelper.scaleEntity(nbt, 0.3f);
            nbt.putInt("glow_color_override", 0xAAAAFF);
            entity.readNbt(nbt);
            entity.noClip = true;
            entity.setGlowing(true);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag("gameEventVisualizer");
            if (world instanceof ServerWorld serverWorld) {
                addMarkerToTeam(serverWorld, "gameEventTeam", entity);
            }
            entity.setInvisible(true);
            world.spawnEntity(entity);
            return entity;
        }
    }

    public static void setVisualizer(World world, Vec3d pos, String emitter, String type) {
        if (visualizers.containsKey(pos)) {
            visualizers.put(pos, Map.entry(visualizers.get(pos).getKey(), SURVIVE_TIME));
        } else {
            if (world instanceof ServerWorld serverWorld) {
                visualizers.put(pos, Map.entry(new GameEventObject(serverWorld, pos, emitter, type), SURVIVE_TIME));
            }
        }

    }

    private static void addMarkerToTeam(ServerWorld world, String teamName, DisplayEntity.BlockDisplayEntity marker) {
        Scoreboard scoreboard = world.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);

            team.setColor(Formatting.AQUA);
        }
        String entityName = marker.getUuidAsString();
        scoreboard.addScoreHolderToTeam(entityName, team);
    }

    public static void updateVisualizer() {
        if (!YetAnotherCarpetAdditionRules.gameEventVisualize || !CarpetServer.minecraft_server.getTickManager().shouldTick())
            return;
        visualizers.forEach((pos, entry) -> {
            GameEventObject object = entry.getKey();
            int time = entry.getValue();
            if (time > 0) {
                visualizers.put(pos, Map.entry(object, time - 1));
            } else {
                object.removeVisualizer();
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
            Predicate<DisplayEntity.BlockDisplayEntity> predicate = slime -> slime.getCommandTags().contains("gameEventVisualizer");
            List<DisplayEntity.BlockDisplayEntity> entities = new ArrayList<>();
            world.collectEntitiesByType(EntityType.BLOCK_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
            Predicate<DisplayEntity.TextDisplayEntity> predicate2 = arm -> arm.getCommandTags().contains("gameEventVisualizer");
            List<DisplayEntity.TextDisplayEntity> entities2 = new ArrayList<>();
            world.collectEntitiesByType(EntityType.TEXT_DISPLAY,
                    predicate2,
                    entities2);
            entities2.forEach(Entity::discard);
        }
    }
}
