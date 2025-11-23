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

import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//#if MC < 12105
//$$ import com.google.gson.JsonArray;
//$$ import com.google.gson.JsonObject;
//#else
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
//#endif
//#if MC >= 12105
import org.jetbrains.annotations.NotNull;
//#endif

public class ScheduledTickVisualizing extends AbstractVisualizingManager<BlockPos, ScheduledTickVisualizing.ScheduledTickObject> {
    private static final ConcurrentHashMap<BlockPos, Map.Entry<ScheduledTickObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 100;

    public static class ScheduledTickObject {
        public long triggerTick;
        public int priority;
        public long subTickOrder;
        public String type;
        public Display.TextDisplay tickMarker;
        public String tag;

        public ScheduledTickObject(ServerLevel world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String type, boolean isFluid, String tag) {
            this.triggerTick = triggerTick;
            this.priority = priority;
            this.subTickOrder = subTickOrder;
            this.type = type;
            this.tag = tag;
            setVisualizer(world, pos, triggerTick, priority, subTickOrder);
        }

        public void setVisualizer(ServerLevel world, BlockPos pos, long triggerTick, int priority, long subTickOrder) {
            long time = world.getGameTime();
            int trigger = (int) (triggerTick - time) - 1;

            if (tickMarker != null && !tickMarker.isRemoved()) {
                //#if MC < 12105
                //$$ JsonObject textJson = new JsonObject();
                //$$ textJson.addProperty("text", "");
                //$$ JsonArray extra = new JsonArray();
                //$$
                //$$ JsonObject triggerPart = new JsonObject();
                //$$ triggerPart.addProperty("text", "T:" + trigger);
                //$$ triggerPart.addProperty("color", "red");
                //$$ extra.add(triggerPart);
                //$$
                //$$ JsonObject priorityPart = new JsonObject();
                //$$ priorityPart.addProperty("text", "\nP:" + priority);
                //$$ priorityPart.addProperty("color", "green");
                //$$ extra.add(priorityPart);
                //$$
                //$$ JsonObject subTickPart = new JsonObject();
                //$$ subTickPart.addProperty("text", "\nS:" + subTickOrder);
                //$$ subTickPart.addProperty("color", "blue");
                //$$ extra.add(subTickPart);
                //#else
                ListTag nbtList = getNbtElements(trigger, priority, subTickOrder);
                //#endif

                CompoundTag nbt = NBTDataManager.readFromEntity(tickMarker, new CompoundTag());
                //#if MC < 12105
                //$$ nbt.putString("text", textJson.toString());
                //#else
                nbt.put("text", nbtList);
                //#endif
                NBTDataManager.writeToEntity(tickMarker, nbt);
            } else {
                tickMarker = summonText(world, pos.getCenter().add(0, -0.4, 0), trigger, priority, subTickOrder);
            }
        }

        public void removeVisualizer() {
            if (tickMarker != null) {
                tickMarker.discard();
            }
        }

        private Display.TextDisplay summonText(ServerLevel world, Vec3 pos, int trigger, int priority, long subTickOrder) {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);

            //#if MC < 12105
            //$$ JsonObject textJson = new JsonObject();
            //$$ textJson.addProperty("text", "");
            //$$ JsonArray extra = new JsonArray();
            //$$
            //$$ JsonObject triggerPart = new JsonObject();
            //$$ triggerPart.addProperty("text", "T:" + trigger);
            //$$ triggerPart.addProperty("color", "red");
            //$$ extra.add(triggerPart);
            //$$
            //$$ JsonObject priorityPart = new JsonObject();
            //$$ priorityPart.addProperty("text", "\nP:" + priority);
            //$$ priorityPart.addProperty("color", "green");
            //$$ extra.add(priorityPart);
            //$$
            //$$ JsonObject subTickPart = new JsonObject();
            //$$ subTickPart.addProperty("text", "\nS:" + subTickOrder);
            //$$ subTickPart.addProperty("color", "blue");
            //$$ extra.add(subTickPart);
            //$$
            //$$ textJson.add("extra", extra);
            //#else
            ListTag nbtList = getNbtElements(trigger, priority, subTickOrder);
            //#endif

            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.putString("billboard", "center");
            //#if MC < 12105
            //$$ nbt.putString("text", textJson.toString());
            //#else
            nbt.put("text", nbtList);
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
    }

    @Override
    protected void storeVisualizer(BlockPos key, ScheduledTickObject entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerLevel) entity.tickMarker.level())));
    }

    @Override
    @SuppressWarnings("resource")
    protected void updateVisualizerEntity(ScheduledTickObject marker, Object data) {
        if (data instanceof Object[] tickData && marker.tickMarker != null && !marker.tickMarker.isRemoved()) {
            long triggerTick = (long) tickData[0];
            int priority = (int) tickData[1];
            long subTickOrder = (long) tickData[2];

            long time = marker.tickMarker.level().getGameTime();
            int trigger = (int) (triggerTick - time) - 1;

            //#if MC < 12105
            //$$ JsonObject textJson = new JsonObject();
            //$$ textJson.addProperty("text", "");
            //$$ JsonArray extra = new JsonArray();
            //$$
            //$$ JsonObject triggerPart = new JsonObject();
            //$$ triggerPart.addProperty("text", "T:" + trigger);
            //$$ triggerPart.addProperty("color", "red");
            //$$ extra.add(triggerPart);
            //$$
            //$$ JsonObject priorityPart = new JsonObject();
            //$$ priorityPart.addProperty("text", "\nP:" + priority);
            //$$ priorityPart.addProperty("color", "green");
            //$$ extra.add(priorityPart);
            //$$
            //$$ JsonObject subTickPart = new JsonObject();
            //$$ subTickPart.addProperty("text", "\nS:" + subTickOrder);
            //$$ subTickPart.addProperty("color", "blue");
            //$$ extra.add(subTickPart);
            //$$ textJson.add("extra", extra);
            //#else
            ListTag nbtList = getNbtElements(trigger, priority, subTickOrder);
            //#endif

            CompoundTag nbt = NBTDataManager.readFromEntity(marker.tickMarker, new CompoundTag());
            //#if MC < 12105
            //$$ nbt.putString("text", textJson.toString());
            //#else
            nbt.put("text", nbtList);
            //#endif
            NBTDataManager.writeToEntity(marker.tickMarker, nbt);
        }
    }

    //#if MC >= 12105
    public static @NotNull ListTag getNbtElements(int trigger, int priority, long subTickOrder) {
        ListTag nbtList = new ListTag();

        HashMap<String, Tag> triggerPart = new HashMap<>();
        triggerPart.put("text", StringTag.valueOf("T:" + trigger));
        triggerPart.put("color", StringTag.valueOf("red"));
        CompoundTag textComponent = new CompoundTag(triggerPart);
        nbtList.add(textComponent);

        HashMap<String, Tag> priorityPart = new HashMap<>();
        priorityPart.put("text", StringTag.valueOf("\nP:" + priority ));
        priorityPart.put("color", StringTag.valueOf("green"));
        textComponent = new CompoundTag(priorityPart);
        nbtList.add(textComponent);

        HashMap<String, Tag> subTickPart = new HashMap<>();
        subTickPart.put("text", StringTag.valueOf("\nS:" + subTickOrder));
        subTickPart.put("color", StringTag.valueOf("blue"));
        textComponent = new CompoundTag(subTickPart);
        nbtList.add(textComponent);
        return nbtList;
    }
    //#endif

    @Override
    protected ScheduledTickObject createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof Object[] tickData) {
            long triggerTick = (long) tickData[0];
            int priority = (int) tickData[1];
            long subTickOrder = (long) tickData[2];
            String type = (String) tickData[3];
            boolean isFluid = (boolean) tickData[4];
            BlockPos blockPos = BlockPos.containing(pos);
            return new ScheduledTickObject(world, blockPos, triggerTick, priority, subTickOrder, type, isFluid, getVisualizerTag());
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<ScheduledTickObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected ScheduledTickObject getVisualizer(BlockPos key) {
        Map.Entry<ScheduledTickObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    public String getVisualizerTag() {
        return "scheduledTickVisualize";
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.values().forEach(entry -> entry.getKey().removeVisualizer());
        visualizers.clear();
    }

    @Override
    public void updateVisualizer() {

    }

    public void setVisualizer(ServerLevel world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String content, boolean isFluid) {
        Object[] data = new Object[]{triggerTick, priority, subTickOrder, content, isFluid};
        setVisualizer(world, pos, pos.getCenter(), data);
    }
}