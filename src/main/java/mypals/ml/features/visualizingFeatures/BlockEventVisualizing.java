package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static mypals.ml.features.visualizingFeatures.EntityHelper.mapSize;

public class BlockEventVisualizing extends AbstractVisualizingManager<BlockPos, BlockEventVisualizing.BlockEventObject> {
    private static final ConcurrentHashMap<BlockPos, Map.Entry<BlockEventObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 50;

    public static class BlockEventObject {
        public String tag;
        public DisplayEntity.TextDisplayEntity tickMarker;
        public DisplayEntity.BlockDisplayEntity typeMarker;

        public BlockEventObject(ServerWorld world, BlockPos pos, int order, String tag) {
            this.tag = tag;
            setVisualizer(world, pos, order);
        }

        public void setVisualizer(ServerWorld world, BlockPos pos, int order) {
            if (tickMarker != null && !tickMarker.isRemoved()) {
                JsonObject textJson = new JsonObject();
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();
                JsonObject orderPart = new JsonObject();
                orderPart.addProperty("text", String.valueOf(order));
                orderPart.addProperty("color", "green");
                extra.add(orderPart);
                textJson.add("extra", extra);

                NbtCompound nbt = tickMarker.writeNbt(new NbtCompound());
                nbt.putString("text", textJson.toString());
                tickMarker.readNbt(nbt);
            } else {
                tickMarker = summonText(world, pos.toCenterPos().add(0, -0.4, 0), String.valueOf(order));
            }

            if (typeMarker == null || typeMarker.isRemoved()) {
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

        private DisplayEntity.TextDisplayEntity summonText(ServerWorld world, Vec3d pos, String order) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);

            JsonObject textJson = new JsonObject();
            textJson.addProperty("text", "");
            JsonArray extra = new JsonArray();
            JsonObject orderPart = new JsonObject();
            orderPart.addProperty("text", "[" + String.valueOf(order) + "]");
            orderPart.addProperty("color", "green");
            extra.add(orderPart);
            textJson.add("extra", extra);

            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.putString("billboard", "center");
            nbt.putString("text", textJson.toString());
            nbt.putByte("see_through", (byte) 1);
            //nbt.putInt("background", 0x00000000);
            entity.readNbt(nbt);

            entity.setPos(pos.getX(), pos.getY() + 0.2, pos.getZ());
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            return entity;
        }

        private DisplayEntity.BlockDisplayEntity summonMarker(World world, BlockPos pos) {
            DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.9f;
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.put("block_state", NbtHelper.fromBlockState(Blocks.GREEN_STAINED_GLASS.getDefaultState()));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", 0xAAFFAA);
            entity.readNbt(nbt);
            entity.noClip = true;
            entity.setGlowing(true);
            entity.setInvisible(true);
            entity.setInvulnerable(true);

            float offset = (float) ((1.0f - scale) / 2.0f);
            entity.setPos(pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");

            if (world instanceof ServerWorld serverWorld) {
                addMarkerToTeam(serverWorld, "blockEventTeam", entity);
            }
            world.spawnEntity(entity);
            return entity;
        }
    }

    @Override
    protected void storeVisualizer(BlockPos key, BlockEventObject blockEventObject) {
        visualizers.put(key, Map.entry(blockEventObject, getDeleteTick(SURVIVE_TIME, (ServerWorld) blockEventObject.tickMarker.getWorld())));
    }

    @Override
    protected void updateVisualizerEntity(BlockEventObject marker, Object data) {
        if (data instanceof Integer order && marker.tickMarker != null && !marker.tickMarker.isRemoved()) {
            NbtCompound nbt2 = marker.typeMarker.writeNbt(new NbtCompound());
            nbt2 = EntityHelper.scaleEntity(nbt2, 0.9f);
            float offset = (float) ((1.0f - 0.9f) / 2.0f);
            marker.typeMarker.setPos(marker.typeMarker.getX() + offset, marker.typeMarker.getY() + offset, marker.typeMarker.getZ() + offset);
            marker.typeMarker.readNbt(nbt2);


            NbtCompound nbt = marker.tickMarker.writeNbt(new NbtCompound());
            JsonObject orderPart = new JsonObject();
            JsonObject textJson = new JsonObject();
            if (marker.tickMarker.age != 0) {
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();
                orderPart.addProperty("text", "[" + String.valueOf(order) + "]");
                orderPart.addProperty("color", "green");
                extra.add(orderPart);
                textJson.add("extra", extra);
            } else {
                String existingText = nbt.getString("text");
                JsonArray extraArray = new JsonArray();
                JsonObject orderLine = new JsonObject();
                orderLine.addProperty("text", "\n[" + order + "]");
                orderLine.addProperty("color", "green");

                try {
                    JsonElement parsed = JsonParser.parseString(existingText);
                    if (parsed.isJsonObject()) {
                        JsonObject existingJson = parsed.getAsJsonObject();

                        if (existingJson.has("extra") && existingJson.get("extra").isJsonArray()) {
                            JsonArray originalExtras = existingJson.getAsJsonArray("extra");
                            for (JsonElement e : originalExtras) {
                                extraArray.add(e);
                            }
                        }

                        if (existingJson.has("text")) {
                            String baseText = existingJson.get("text").getAsString();
                            if (!baseText.isEmpty()) {
                                JsonObject base = new JsonObject();
                                base.addProperty("text", baseText);
                                extraArray.add(base);
                            }
                        }

                        extraArray.add(orderLine);
                    } else {
                        JsonObject fallback = new JsonObject();
                        fallback.addProperty("text", existingText);
                        extraArray.add(fallback);
                        extraArray.add(orderLine);
                    }
                } catch (Exception e) {
                    JsonObject fallback = new JsonObject();
                    fallback.addProperty("text", existingText);
                    extraArray.add(fallback);
                    extraArray.add(orderLine);
                }

                textJson = new JsonObject();
                textJson.addProperty("text", "");
                textJson.add("extra", extraArray);
            }


            nbt.putString("text", textJson.toString());
            marker.tickMarker.age = 0;
            marker.typeMarker.age = 0;
            marker.tickMarker.readNbt(nbt);

            visualizers.put(marker.tickMarker.getBlockPos(), Map.entry(marker, getDeleteTick(SURVIVE_TIME, (ServerWorld) marker.tickMarker.getWorld())));
        }
    }

    @Override
    protected BlockEventObject createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof Integer order) {
            BlockPos blockPos = BlockPos.ofFloored(pos);
            return new BlockEventObject(world, blockPos, order, getVisualizerTag());
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<BlockEventObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected BlockEventObject getVisualizer(BlockPos key) {
        Map.Entry<BlockEventObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    protected String getVisualizerTag() {
        return "blockEventVisualizer";
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.values().forEach(entry -> entry.getKey().removeVisualizer());
        visualizers.clear();
    }

    @Override
    public void updateVisualizer() {
        if (!YetAnotherCarpetAdditionRules.blockEventVisualize || !CarpetServer.minecraft_server.getTickManager().shouldTick()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            BlockEventObject object = entry.getKey();
            long deleteTick = entry.getValue();
            if (deleteTick < object.tickMarker.getWorld().getTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }
            NbtCompound nbt = entry.getKey().typeMarker.writeNbt(new NbtCompound());

            float scale = mapSize(SURVIVE_TIME - entry.getKey().typeMarker.age, SURVIVE_TIME, 0.9f);
            nbt = EntityHelper.scaleEntity(nbt, scale);
            entry.getKey().typeMarker.readNbt(nbt);
            entry.getKey().typeMarker.setPos(pos.toCenterPos().getX() - (scale / 2), pos.toCenterPos().getY() - (scale / 2), pos.toCenterPos().getZ() - (scale / 2));

        });
    }

    @Override
    public void setVisualizer(ServerWorld world, BlockPos key, Vec3d pos, Object data) {
        /*if (visualizers.containsKey(key)) {
            removeVisualizer(key);
        }*/
        super.setVisualizer(world, key, pos, data);
    }

    private static void addMarkerToTeam(ServerWorld world, String teamName, DisplayEntity.BlockDisplayEntity marker) {
        Scoreboard scoreboard = world.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
            team.setColor(Formatting.GREEN);
        }
        String entityName = marker.getUuidAsString();
        scoreboard.addScoreHolderToTeam(entityName, team);
    }
}