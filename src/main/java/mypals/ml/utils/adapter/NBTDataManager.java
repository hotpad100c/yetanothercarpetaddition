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

package mypals.ml.utils.adapter;

import mypals.ml.YetAnotherCarpetAdditionServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
public class NBTDataManager {

    public static void writeToEntity(Entity entity, CompoundTag data) {

        //#if MC >= 12106

        ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), YetAnotherCarpetAdditionServer.LOGGER);
        ValueInput nbtReadView = TagValueInput.create(logging, entity.registryAccess(), data);
        entity.load(nbtReadView);
        //#else
        //$$
        //$$ entity.readNbt(data);
        //#endif
    }

    public static CompoundTag readFromEntity(Entity entity, CompoundTag nbtCompound) {
        //#if MC >= 12106
        ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), YetAnotherCarpetAdditionServer.LOGGER);
        TagValueOutput nbtWriteView2 = TagValueOutput.createWithContext(logging, entity.registryAccess());
        nbtWriteView2.buildResult().merge(nbtCompound);
        entity.saveWithoutId(nbtWriteView2);
        nbtCompound = nbtWriteView2.buildResult();
        //#else
        //$$ entity.writeNbt(nbtCompound);
        //#endif
        return nbtCompound;
    }
}
