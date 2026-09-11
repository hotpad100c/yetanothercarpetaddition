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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;

import static mypals.ml.features.visualizingFeatures.MobGoals.getGoalName;
//#if MC >= 260200
//$$ import net.minecraft.world.entity.EntityTypes;
//#endif

public class MobAIVisualizer extends AbstractVisualizingManager<Entity, Map.Entry<MobAIVisualizer.MobAIData, Display.TextDisplay>> {
    private static final Map<Entity, Map.Entry<MobAIData, Display.TextDisplay>> visualizers = new HashMap<>();

    @Override
    protected void storeVisualizer(Entity key, Map.Entry<MobAIData, Display.TextDisplay> data) {
        visualizers.put(key, data);
    }

    @Override
    protected void updateVisualizerEntity(Map.Entry<MobAIData, Display.TextDisplay> marker, Object data) {

        Display.TextDisplay display = (Display.TextDisplay) marker.getValue();
        MobAIData mobAIData = (MobAIData) marker.getKey();
        Entity keyEntity = (Entity) marker.getKey().entity;
        if (keyEntity == null || keyEntity.isRemoved() || !keyEntity.isAlive() || display == null || display.isRemoved()) {
            display.discard();
            visualizers.remove(keyEntity);
            return;
        }
        displayTargetAndGoals(mobAIData, display);
        display.setPosRaw(keyEntity.getX(), keyEntity.getY() + keyEntity.getBbHeight() + 0.5, keyEntity.getZ());
    }

    @Override
    protected Map.Entry<MobAIData, Display.TextDisplay> createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof MobAIData mobAIData) {
            //#if MC >= 260200
            //$$ Display.TextDisplay display = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
            //#else
            Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            //#endif
            display.setNoGravity(true);
            //#if MC >= 260300
            //$$ display.setPermanentlyInvulnerable(true);
            //#else
            display.setInvulnerable(true);
            //#endif
            display.setPosRaw(pos.x(), pos.y() + 0.1f, pos.z());
            display.addTag(getVisualizerTag());
            display.addTag("DoNotTick");
            world.addFreshEntity(display);
            display.startRiding(mobAIData.entity);
            displayTargetAndGoals(mobAIData, display);
            return Map.entry(mobAIData, display);
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(Entity key) {
        Display.TextDisplay entity = visualizers.get(key).getValue();
        if (entity != null) {
            entity.discard();
            visualizers.remove(key);
        }
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.clear();
    }

    @Override
    protected Map.Entry<MobAIData, Display.TextDisplay> getVisualizer(Entity key) {
        return visualizers.get(key) == null ? null : visualizers.get(key);
    }

    @Override
    public String getVisualizerTag() {
        return "mobAIVisualize";
    }

    @Override
    public void updateVisualizer() {
        for (Map.Entry<Entity, Map.Entry<MobAIData, Display.TextDisplay>> entry : visualizers.entrySet()) {
            Display.TextDisplay display = entry.getValue().getValue();
            MobAIData mobAIData = entry.getValue().getKey();
            Entity keyEntity = entry.getKey();
            if (keyEntity == null || keyEntity.isRemoved() || !keyEntity.isAlive() || display == null || display.isRemoved()) {
                display.discard();
                visualizers.remove(keyEntity);
                return;
            }
            displayTargetAndGoals(mobAIData, display);
            //display.setPos(keyEntity.getX(), keyEntity.getY() + keyEntity.getHeight() + 0.5, keyEntity.getZ());

        }
    }

    private void displayTargetAndGoals(MobAIData data, Display.TextDisplay display) {

        JsonObject textJson = new JsonObject();
        textJson.addProperty("text", "");
        JsonArray extra = new JsonArray();

        JsonObject goalSelectorHeader = new JsonObject();
        goalSelectorHeader.addProperty("text", "GoalSelector:\n");
        goalSelectorHeader.addProperty("color", "white");
        extra.add(goalSelectorHeader);

        data.goalSelector.getAvailableGoals().forEach(goal -> {
            if (goal.getGoal() != null) {
                String goalName = getGoalName(goal.getGoal().getClass());
                String translatedName = Component.translatable(goalName).getString();
                String color = goal.isRunning() ? "gold" : "gray";
                JsonObject goalPart = new JsonObject();
                goalPart.addProperty("text", "- " + translatedName + "\n");
                goalPart.addProperty("color", color);
                extra.add(goalPart);
            }
        });

        JsonObject targetSelectorHeader = new JsonObject();
        targetSelectorHeader.addProperty("text", "TargetSelector:\n");
        targetSelectorHeader.addProperty("color", "white");
        extra.add(targetSelectorHeader);

        data.targetSelector.getAvailableGoals().forEach(goal -> {
            if (goal.getGoal() != null) {
                String goalName = getGoalName(goal.getGoal().getClass());
                String translatedName = Component.translatable(goalName).getString();
                String color = goal.isRunning() ? "gold" : "gray";
                JsonObject goalPart = new JsonObject();
                goalPart.addProperty("text", "- " + translatedName + "\n");
                goalPart.addProperty("color", color);
                extra.add(goalPart);
            }
        });

        textJson.add("extra", extra);
        CompoundTag nbt = NBTDataManager.readFromEntity(display, new CompoundTag());
        //#if MC >= 12105
        ListTag nbtList = new ListTag();
        extra.forEach(element -> {
           JsonObject obj = element.getAsJsonObject();
           CompoundTag component = new CompoundTag();
           component.putString("text", obj.get("text").getAsString());
            component.putString("color", obj.get("color").getAsString());
           nbtList.add(component);
        });
        nbt.put("text", nbtList);
        //#else
        //$$ nbt.putString("text", textJson.toString());
        //#endif
        nbt = configureCommonNbt(nbt);
        NBTDataManager.writeToEntity(display, nbt);
    }


    public static class MobAIData {
        public GoalSelector goalSelector;
        public GoalSelector targetSelector;
        public Mob entity;

        public MobAIData(Mob entity, GoalSelector goalSelector, GoalSelector targetSelector) {
            this.entity = entity;
            this.goalSelector = goalSelector;
            this.targetSelector = targetSelector;
        }
    }
}
