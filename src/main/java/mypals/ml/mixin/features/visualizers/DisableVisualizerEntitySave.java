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

package mypals.ml.mixin.features.visualizers;

import mypals.ml.interfaces.ISelf;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

import static mypals.ml.YetAnotherCarpetAdditionServer.VisualizerTags;

//#if MC < 12106
//$$ import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(Entity.class)
public abstract class DisableVisualizerEntitySave implements ISelf<Entity>{

    //#if MC >= 260100
    //$$ @Shadow
    //$$ public abstract Set<String> entityTags();
    //#else
    @Shadow
    public abstract Set<String> getTags();
    //#endif

    @Inject(method = "saveAsPassenger", at = @At("HEAD"), cancellable = true)
    public void saveSelfNbt(
            //#if MC < 12106
            //$$ CompoundTag nbt,
            //#endif
            CallbackInfoReturnable<Boolean> cir) {
        if (yetanothercarpetaddition$self() instanceof Display) {
            //#if MC >= 260100
            //$$ Set<String> tags = this.entityTags();
            //#else
            Set<String> tags = this.getTags();
            //#endif
            for (String tag : VisualizerTags) {
                if (tags.contains(tag)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

}
