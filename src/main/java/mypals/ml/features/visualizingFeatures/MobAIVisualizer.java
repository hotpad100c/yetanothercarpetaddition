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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
//#if MC >= 12105
//$$import net.minecraft.nbt.NbtList;
//#endif
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

import static mypals.ml.features.visualizingFeatures.MobGoals.getGoalName;

public class MobAIVisualizer extends AbstractVisualizingManager<Entity, Map.Entry<MobAIVisualizer.MobAIData, DisplayEntity.TextDisplayEntity>> {
    private static final Map<Entity, Map.Entry<MobAIVisualizer.MobAIData, DisplayEntity.TextDisplayEntity>> visualizers = new HashMap<>();

    @Override
    protected void storeVisualizer(Entity key, Map.Entry<MobAIData, DisplayEntity.TextDisplayEntity> data) {
        visualizers.put(key, data);
    }

    @Override
    protected void updateVisualizerEntity(Map.Entry<MobAIData, DisplayEntity.TextDisplayEntity> marker, Object data) {

        DisplayEntity.TextDisplayEntity display = (DisplayEntity.TextDisplayEntity) marker.getValue();
        MobAIData mobAIData = (MobAIData) marker.getKey();
        Entity keyEntity = (Entity) marker.getKey().entity;
        if (keyEntity == null || keyEntity.isRemoved() || !keyEntity.isAlive() || display == null || display.isRemoved()) {
            display.discard();
            visualizers.remove(keyEntity);
            return;
        }
        displayTargetAndGoals(mobAIData, display);
        display.setPos(keyEntity.getX(), keyEntity.getY() + keyEntity.getHeight() + 0.5, keyEntity.getZ());
    }

    @Override
    protected Map.Entry<MobAIData, DisplayEntity.TextDisplayEntity> createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof MobAIData mobAIData) {
            DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            display.setNoGravity(true);
            display.setInvulnerable(true);
            display.setPos(pos.getX(), pos.getY() + 0.1f, pos.getZ());
            display.addCommandTag(getVisualizerTag());
            display.addCommandTag("DoNotTick");
            world.spawnEntity(display);
            display.startRiding(mobAIData.entity);
            displayTargetAndGoals(mobAIData, display);
            return Map.entry(mobAIData, display);
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(Entity key) {
        DisplayEntity.TextDisplayEntity entity = visualizers.get(key).getValue();
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
    protected Map.Entry<MobAIData, DisplayEntity.TextDisplayEntity> getVisualizer(Entity key) {
        return visualizers.get(key) == null ? null : visualizers.get(key);
    }

    @Override
    protected String getVisualizerTag() {
        return "MobAIVisualizer";
    }

    @Override
    public void updateVisualizer() {
        for (Map.Entry<Entity, Map.Entry<MobAIData, DisplayEntity.TextDisplayEntity>> entry : visualizers.entrySet()) {
            DisplayEntity.TextDisplayEntity display = entry.getValue().getValue();
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

    private void displayTargetAndGoals(MobAIData data, DisplayEntity.TextDisplayEntity display) {

        JsonObject textJson = new JsonObject();
        textJson.addProperty("text", "");
        JsonArray extra = new JsonArray();

        JsonObject goalSelectorHeader = new JsonObject();
        goalSelectorHeader.addProperty("text", "GoalSelector:\n");
        goalSelectorHeader.addProperty("color", "white");
        extra.add(goalSelectorHeader);

        data.goalSelector.getGoals().forEach(goal -> {
            if (goal.getGoal() != null) {
                String goalName = getGoalName(goal.getGoal().getClass());
                String translatedName = Text.translatable(goalName).getString();
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

        data.targetSelector.getGoals().forEach(goal -> {
            if (goal.getGoal() != null) {
                String goalName = getGoalName(goal.getGoal().getClass());
                String translatedName = Text.translatable(goalName).getString();
                String color = goal.isRunning() ? "gold" : "gray";
                JsonObject goalPart = new JsonObject();
                goalPart.addProperty("text", "- " + translatedName + "\n");
                goalPart.addProperty("color", color);
                extra.add(goalPart);
            }
        });

        textJson.add("extra", extra);
        NbtCompound nbt = display.writeNbt(new NbtCompound());
        //#if MC >= 12105
        //$$NbtList nbtList = new NbtList();
        //$$extra.forEach(element -> {
        //$$    JsonObject obj = element.getAsJsonObject();
        //$$    NbtCompound component = new NbtCompound();
        //$$    component.putString("text", obj.get("text").getAsString());
        //$$     component.putString("color", obj.get("color").getAsString());
        //$$    nbtList.add(component);
        //$$});
        //$$nbt.put("text", nbtList);
        //#else
        nbt.putString("text", textJson.toString());
        //#endif
        nbt = configureCommonNbt(nbt);
        display.readNbt(nbt);
    }


    public static class MobAIData {
        public GoalSelector goalSelector;
        public GoalSelector targetSelector;
        public MobEntity entity;

        public MobAIData(MobEntity entity, GoalSelector goalSelector, GoalSelector targetSelector) {
            this.entity = entity;
            this.goalSelector = goalSelector;
            this.targetSelector = targetSelector;
        }
    }
}
