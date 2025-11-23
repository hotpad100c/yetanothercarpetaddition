/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.NBTDataManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static mypals.ml.features.visualizingFeatures.EntityHelper.mapSize;

public class BlockEventVisualizing extends AbstractVisualizingManager<BlockPos, BlockEventVisualizing.BlockEventObject> {
    private static final ConcurrentHashMap<BlockPos, Map.Entry<BlockEventObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 50;

    public static class BlockEventObject {
        public String tag;
        public Display.TextDisplay tickMarker;
        public Display.BlockDisplay typeMarker;
        public Long summonTime;

        public BlockEventObject(ServerLevel world, BlockPos pos, int order, String tag) {
            this.tag = tag;
            setVisualizer(world, pos, order);
            summonTime = world.getGameTime();
        }

        public void setVisualizer(ServerLevel world, BlockPos pos, int order) {
            if (tickMarker != null && !tickMarker.isRemoved()) {
                JsonObject textJson = new JsonObject();
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();
                JsonObject orderPart = new JsonObject();
                orderPart.addProperty("text", String.valueOf(order));
                orderPart.addProperty("color", "green");
                extra.add(orderPart);
                textJson.add("extra", extra);

                CompoundTag nbt = NBTDataManager.readFromEntity(tickMarker, new CompoundTag());
                nbt.putString("text", textJson.toString());
                NBTDataManager.writeToEntity(tickMarker, nbt);
            } else {
                tickMarker = summonText(world, pos.getCenter().add(0, -0.4, 0), String.valueOf(order));
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

        private Display.TextDisplay summonText(ServerLevel world, Vec3 pos, String order) {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
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

            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.putString("billboard", "center");
            nbt.putString("text", textJson.toString());
            nbt.putByte("see_through", (byte) 1);
            //nbt.putInt("background", 0x00000000);
            NBTDataManager.writeToEntity(entity, nbt);

            entity.setPosRaw(pos.x(), pos.y() + 0.2, pos.z());
            entity.addTag(tag);
            entity.addTag("DoNotTick");
            world.addFreshEntity(entity);
            return entity;
        }

        private Display.BlockDisplay summonMarker(Level world, BlockPos pos) {
            Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.9f;
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.put("block_state", NbtUtils.writeBlockState(Blocks.GREEN_STAINED_GLASS.defaultBlockState()));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", 0xAAFFAA);
            NBTDataManager.writeToEntity(entity, nbt);
            entity.noPhysics = true;
            entity.setGlowingTag(true);
            entity.setInvisible(true);
            entity.setInvulnerable(true);

            float offset = (float) ((1.0f - scale) / 2.0f);
            entity.setPosRaw(pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
            entity.addTag(tag);
            entity.addTag("DoNotTick");

            if (world instanceof ServerLevel serverWorld) {
                addMarkerToTeam(serverWorld, "blockEventTeam", entity);
            }
            world.addFreshEntity(entity);
            return entity;
        }
    }

    @Override
    protected void storeVisualizer(BlockPos key, BlockEventObject blockEventObject) {
        visualizers.put(key, Map.entry(blockEventObject, getDeleteTick(SURVIVE_TIME, (ServerLevel) blockEventObject.tickMarker.level())));
    }

    @Override
    protected void updateVisualizerEntity(BlockEventObject marker, Object data) {
        if (data instanceof Integer order && marker.tickMarker != null && !marker.tickMarker.isRemoved()) {
            CompoundTag nbt2 = NBTDataManager.readFromEntity(marker.typeMarker, new CompoundTag());
            nbt2 = EntityHelper.scaleEntity(nbt2, 0.9f);
            float offset = (float) ((1.0f - 0.9f) / 2.0f);
            marker.typeMarker.setPosRaw(marker.typeMarker.getX() + offset, marker.typeMarker.getY() + offset, marker.typeMarker.getZ() + offset);
            NBTDataManager.writeToEntity(marker.typeMarker, nbt2);

            CompoundTag nbt = NBTDataManager.readFromEntity(marker.tickMarker, new CompoundTag());
            JsonObject orderPart = new JsonObject();
            JsonObject textJson = new JsonObject();
            if (marker.tickMarker.level().getGameTime() != marker.summonTime) {
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();
                orderPart.addProperty("text", "[" + String.valueOf(order) + "]");
                orderPart.addProperty("color", "green");
                extra.add(orderPart);
                textJson.add("extra", extra);
            } else {
                String existingText = nbt.
                        //#if MC >= 12105
                        getStringOr
                        //#else
                        //$$getString
                        //#endif
                        (
                        "text"
                        //#if MC >= 12105
                        , ""
                        //#endif
                );
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
            marker.tickMarker.tickCount = 0;
            marker.typeMarker.tickCount = 0;
            NBTDataManager.writeToEntity(marker.tickMarker, nbt);
            visualizers.put(marker.tickMarker.blockPosition(), Map.entry(marker, getDeleteTick(SURVIVE_TIME, (ServerLevel) marker.tickMarker.level())));
        }
    }

    @Override
    protected BlockEventObject createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof Integer order) {
            BlockPos blockPos = BlockPos.containing(pos);
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
    public String getVisualizerTag() {
        return "blockEventVisualize";
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.values().forEach(entry -> entry.getKey().removeVisualizer());
        visualizers.clear();
    }

    @Override
    public void updateVisualizer() {
        if (!YetAnotherCarpetAdditionRules.blockEventVisualize || !CarpetServer.minecraft_server.tickRateManager().runsNormally()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            BlockEventObject object = entry.getKey();
            long deleteTick = entry.getValue();
            if (deleteTick < object.tickMarker.level().getGameTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }
            CompoundTag nbt = NBTDataManager.readFromEntity(entry.getKey().typeMarker, new CompoundTag());

            float scale = mapSize((int) (deleteTick - CarpetServer.minecraft_server.overworld().getGameTime()), SURVIVE_TIME, 0.9f);
            nbt = EntityHelper.scaleEntity(nbt, scale);
            NBTDataManager.writeToEntity(entry.getKey().typeMarker, nbt);
            entry.getKey().typeMarker.setPosRaw(pos.getCenter().x() - (scale / 2), pos.getCenter().y() - (scale / 2), pos.getCenter().z() - (scale / 2));

        });
    }

    @Override
    public void setVisualizer(ServerLevel world, BlockPos key, Vec3 pos, Object data) {
        /*if (visualizers.containsKey(key)) {
            removeVisualizer(key);
        }*/
        super.setVisualizer(world, key, pos, data);
    }

    private static void addMarkerToTeam(ServerLevel world, String teamName, Display.BlockDisplay marker) {
        Scoreboard scoreboard = world.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(ChatFormatting.GREEN);
        }
        String entityName = marker.getStringUUID();
        scoreboard.addPlayerToTeam(entityName, team);
    }
}