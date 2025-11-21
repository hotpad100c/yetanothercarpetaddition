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

package mypals.ml.mixin.features.allowBadBlockEntity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public class WorldChunkMixin {

    //#if MC>=12101
    //$$
    //$$ @ModifyExpressionValue(method = "setBlockEntity",require = 0, at = @At(value = "INVOKE",
    //$$         target = "Lnet/minecraft/block/entity/BlockEntityType;supports(Lnet/minecraft/block/BlockState;)Z"))
    //$$ private boolean allowInvalidBlockEntities(boolean original) {
    //$$     if (YetAnotherCarpetAdditionRules.allowIllegalBlockEntities) {
    //$$         return true;
    //$$     }
    //$$     return original;
    //$$ }
    //#else

    //#endif
}
