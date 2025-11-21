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
import com.google.gson.JsonObject;
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.ChatFormatting;
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
//#if MC >= 12105
//$$ import net.minecraft.nbt.NbtList;
//$$ import net.minecraft.nbt.NbtElement;
//$$ import net.minecraft.nbt.NbtString;
//$$ import java.util.HashMap;
//#endif
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameEventVisualizing extends AbstractVisualizingManager<Vec3, GameEventVisualizing.GameEventObject> {
    private static final ConcurrentHashMap<Vec3, Map.Entry<GameEventObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 30;

    public static class GameEventObject {
        public String type;
        public String tag;
        public Display.TextDisplay textMarker;
        public Display.BlockDisplay posMarker;

        public GameEventObject(ServerLevel world, Vec3 pos, String emitter, String type, String tag) {
            this.tag = tag;
            setVisualizer(world, pos, emitter, type);
        }

        public void setVisualizer(ServerLevel world, Vec3 pos, String trigger, String type) {
            if (textMarker != null && !textMarker.isRemoved()) {
                //#if MC < 12105
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
                //#else
                //$$ NbtList nbtList = new NbtList();
                //$$ HashMap<String, NbtElement> triggerPart = new HashMap<>();
                //$$ triggerPart.put("text", NbtString.of(String.valueOf(trigger)));
                //$$ triggerPart.put("color", NbtString.of("blue"));
                //$$ NbtCompound textComponent = new NbtCompound(triggerPart);
                //$$ nbtList.add(textComponent);
                //$$ HashMap<String, NbtElement> typePart = new HashMap<>();
                //$$ typePart.put("text", NbtString.of("\n" + type));
                //$$ typePart.put("color", NbtString.of("blue"));
                //$$ textComponent = new NbtCompound(typePart);
                //$$ nbtList.add(textComponent);
                //#endif

                CompoundTag nbt = NBTDataManager.readFromEntity(textMarker, new CompoundTag());
                //#if MC < 12105
                nbt.putString("text", textJson.toString());
                //#else
                //$$ nbt.put("text", nbtList);
                //#endif
                NBTDataManager.writeToEntity(textMarker, nbt);
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

        private Display.TextDisplay summonText(ServerLevel world, Vec3 pos, String trigger, String type) {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);

            //#if MC < 12105
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
            //#else
            //$$ NbtList nbtList = new NbtList();
            //$$ HashMap<String, NbtElement> triggerPart = new HashMap<>();
            //$$ triggerPart.put("text", NbtString.of(trigger));
            //$$ triggerPart.put("color", NbtString.of("blue"));
            //$$ NbtCompound textComponent = new NbtCompound(triggerPart);
            //$$ nbtList.add(textComponent);
            //$$ HashMap<String, NbtElement> typePart = new HashMap<>();
            //$$ typePart.put("text", NbtString.of("\n" + type));
            //$$ typePart.put("color", NbtString.of("blue"));
            //$$ textComponent = new NbtCompound(typePart);
            //$$ nbtList.add(textComponent);
            //#endif

            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.putString("billboard", "center");
            //#if MC < 12105
            nbt.putString("text", textJson.toString());
            //#else
            //$$ nbt.put("text", nbtList);
            //#endif
            nbt.putByte("see_through", (byte) 1);
            //nbt.putInt("background", 0x00000000);
            NBTDataManager.writeToEntity(entity, nbt);
            entity.setPosRaw(pos.x(), pos.y(), pos.z());
            entity.addTag(tag);
            entity.addTag("DoNotTick");
            world.addFreshEntity(entity);
            return entity;
        }

        private Display.BlockDisplay summonMarker(Level world, Vec3 pos) {
            Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.3f;
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.put("block_state", NbtUtils.writeBlockState(Blocks.BLUE_STAINED_GLASS_PANE.defaultBlockState()));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", 0xAAAAFF);
            
            NBTDataManager.writeToEntity(entity, nbt);
            entity.noPhysics = true;
            entity.setGlowingTag(true);
            entity.setPosRaw(pos.x() - (scale / 2), pos.y() - (scale / 2) - 0.1f, pos.z() - (scale / 2));
            entity.addTag(tag);
            entity.addTag("DoNotTick");
            if (world instanceof ServerLevel serverWorld) {
                addMarkerToTeam(serverWorld, "gameEventTeam", entity);
            }
            entity.setInvisible(true);
            world.addFreshEntity(entity);
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
    protected void storeVisualizer(Vec3 key, GameEventObject entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerLevel) entity.textMarker.level())));
    }

    @Override
    protected GameEventObject createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof String[] eventData) {
            String emitter = eventData[0];
            String type = eventData[1];
            GameEventObject object = new GameEventObject(world, pos, emitter, type, getVisualizerTag());
            return object;
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(Vec3 key) {
        Map.Entry<GameEventObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected GameEventObject getVisualizer(Vec3 key) {
        Map.Entry<GameEventObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    public String getVisualizerTag() {
        return "gameEventVisualize";
    }


    @Override
    public void updateVisualizer() {
        if (!CarpetServer.minecraft_server.tickRateManager().runsNormally()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            GameEventObject object = entry.getKey();
            long time = entry.getValue();
            if (time < object.textMarker.level().getGameTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }
        });
    }

    private static void addMarkerToTeam(ServerLevel world, String teamName, Display.BlockDisplay marker) {
        Scoreboard scoreboard = world.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(ChatFormatting.AQUA);
        }
        String entityName = marker.getStringUUID();
        scoreboard.addPlayerToTeam(entityName, team);
    }
}
