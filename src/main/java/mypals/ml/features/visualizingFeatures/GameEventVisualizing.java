package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameEventVisualizing extends AbstractVisualizingManager<Vec3d, GameEventVisualizing.GameEventObject> {
    private static final ConcurrentHashMap<Vec3d, Map.Entry<GameEventObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 50;

    public static class GameEventObject {
        public String type;
        public String tag;
        public DisplayEntity.TextDisplayEntity textMarker;
        public DisplayEntity.BlockDisplayEntity posMarker;

        public GameEventObject(ServerWorld world, Vec3d pos, String emitter, String type, String tag) {
            setVisualizer(world, pos, emitter, type);
            this.tag = tag;
        }

        public void setVisualizer(ServerWorld world, Vec3d pos, String trigger, String type) {
            if (textMarker != null && !textMarker.isRemoved()) {
                JsonObject textJson = new JsonObject();
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();

                JsonObject triggerPart = new JsonObject();
                triggerPart.addProperty("text", trigger);
                triggerPart.addProperty("color", "blue");
                extra.add(triggerPart);

                JsonObject typePart = new JsonObject();
                typePart.addProperty("text", "\n" + type);
                typePart.addProperty("color", "blue");
                extra.add(typePart);

                textJson.add("extra", extra);

                NbtCompound nbt = textMarker.writeNbt(new NbtCompound());
                nbt.putString("text", textJson.toString());
                textMarker.readNbt(nbt);
            } else {
                textMarker = summonText(world, pos, trigger, type);
            }

            if (posMarker == null) {
                posMarker = summonMarker(world, pos);
            }
        }

        public void removeVisualizer() {
            if (textMarker != null) {
                textMarker.discard();
            }
            if (posMarker != null) {
                posMarker.discard();
            }
        }

        private DisplayEntity.TextDisplayEntity summonText(ServerWorld world, Vec3d pos, String trigger, String type) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);

            JsonObject textJson = new JsonObject();
            textJson.addProperty("text", "");
            JsonArray extra = new JsonArray();

            JsonObject triggerPart = new JsonObject();
            triggerPart.addProperty("text", trigger);
            triggerPart.addProperty("color", "blue");
            extra.add(triggerPart);

            JsonObject typePart = new JsonObject();
            typePart.addProperty("text", "\n" + type);
            typePart.addProperty("color", "blue");
            extra.add(typePart);

            textJson.add("extra", extra);

            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.putString("billboard", "center");
            nbt.putString("text", textJson.toString());
            nbt.putByte("see_through", (byte) 1);
            nbt.putInt("background", 0x00000000);
            entity.readNbt(nbt);

            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            return entity;
        }

        private DisplayEntity.BlockDisplayEntity summonMarker(World world, Vec3d pos) {
            DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.3f;
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.put("block_state", NbtHelper.fromBlockState(Blocks.BLUE_STAINED_GLASS_PANE.getDefaultState()));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", 0xAAAAFF);
            entity.readNbt(nbt);
            entity.noClip = true;
            entity.setGlowing(true);
            entity.setPos(pos.getX() - (scale / 2), pos.getY() - (scale / 2) - 0.1f, pos.getZ() - (scale / 2));
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");
            if (world instanceof ServerWorld serverWorld) {
                addMarkerToTeam(serverWorld, "gameEventTeam", entity);
            }
            entity.setInvisible(true);
            world.spawnEntity(entity);
            return entity;
        }
    }


    @Override
    protected void clearAllVisualizers() {
        visualizers.clear();
    }

    @Override
    protected void updateVisualizerEntity(GameEventObject entity, Object data) {

    }

    @Override
    protected void storeVisualizer(Vec3d key, GameEventObject entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerWorld) entity.textMarker.getWorld())));
    }

    @Override
    protected GameEventObject createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof String[] eventData) {
            String emitter = eventData[0];
            String type = eventData[1];
            GameEventObject object = new GameEventObject(world, pos, emitter, type, getVisualizerTag());
            return object;
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(Vec3d key) {
        Map.Entry<GameEventObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected GameEventObject getVisualizer(Vec3d key) {
        Map.Entry<GameEventObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    protected String getVisualizerTag() {
        return "gameEventVisualizer";
    }


    @Override
    public void updateVisualizer() {
        if (!YetAnotherCarpetAdditionRules.gameEventVisualize || !CarpetServer.minecraft_server.getTickManager().shouldTick()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            GameEventObject object = entry.getKey();
            long time = entry.getValue();
            if (time < object.textMarker.getWorld().getTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }
        });
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
}
