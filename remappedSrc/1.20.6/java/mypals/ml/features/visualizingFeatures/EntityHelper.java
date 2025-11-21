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

import mypals.ml.YetAnotherCarpetAdditionServer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;


public class EntityHelper {
    public static CompoundTag scaleEntity(CompoundTag nbt, float scale) {
        CompoundTag scaleNbt = new CompoundTag();
        CompoundTag transformation = new CompoundTag();
        // Right rotation (identity quaternion: no rotation)
        ListTag rightRotation = new ListTag();
        rightRotation.add(FloatTag.valueOf(0.0f));
        rightRotation.add(FloatTag.valueOf(0.0f));
        rightRotation.add(FloatTag.valueOf(0.0f));
        rightRotation.add(FloatTag.valueOf(1.0f));
        transformation.put("right_rotation", rightRotation);
        // Left rotation (identity quaternion: no rotation)
        ListTag leftRotation = new ListTag();
        leftRotation.add(FloatTag.valueOf(0.0f));
        leftRotation.add(FloatTag.valueOf(0.0f));
        leftRotation.add(FloatTag.valueOf(0.0f));
        leftRotation.add(FloatTag.valueOf(1.0f));
        transformation.put("left_rotation", leftRotation);
        // Translation (no offset)


        ListTag translation = new ListTag();
        translation.add(FloatTag.valueOf(0));
        translation.add(FloatTag.valueOf(0));
        translation.add(FloatTag.valueOf(0));
        transformation.put("translation", translation);
        // Scale
        ListTag scaleList = new ListTag();
        scaleList.add(FloatTag.valueOf(scale));
        scaleList.add(FloatTag.valueOf(scale));
        scaleList.add(FloatTag.valueOf(scale));
        transformation.put("scale", scaleList);
        nbt.put("transformation", transformation);
        return nbt;
    }

    public static void clearVisualizersInServer(MinecraftServer server, String target) {
        for (ServerLevel world : server.getAllLevels()) {
            clearWorldVisualizers(world, target);
        }
    }

    public static float mapSize(int timeLeft, int originalMax, float targetMax) {
        float mapped = timeLeft * (targetMax / (float) originalMax);
        return mapped;
    }


    public static void clearWorldVisualizers(ServerLevel world, String target) {
        if (world != null) {
            List<Display.TextDisplay> entitiesText = new ArrayList<>();
            Predicate<Display.TextDisplay> predicate = marker -> marker.getTags().contains(target);
            world.getEntities(EntityType.TEXT_DISPLAY,
                    predicate,
                    entitiesText);
            entitiesText.forEach(Entity::discard);

            List<Display.BlockDisplay> entitiesBlock = new ArrayList<>();
            Predicate<Display.BlockDisplay> predicate2 = bd -> bd.getTags().contains(target);
            world.getEntities(EntityType.BLOCK_DISPLAY,
                    predicate2,
                    entitiesBlock);
            entitiesBlock.forEach(Entity::discard);
        }
    }
}
