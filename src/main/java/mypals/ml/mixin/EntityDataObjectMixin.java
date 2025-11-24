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

package mypals.ml.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityDataAccessor.class)
public class EntityDataObjectMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "setData",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/commands/data/EntityDataAccessor;ERROR_NO_PLAYERS:Lcom/mojang/brigadier/exceptions/SimpleCommandExceptionType;"),
            cancellable = true)
    public void setNbt(CompoundTag nbt, CallbackInfo ci) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.bypassModifyPlayerDataRestriction) {
            UUID uUID = this.entity.getUUID();
            NBTDataManager.writeToEntity(this.entity, nbt);
            this.entity.setUUID(uUID);
            ci.cancel();
        }
    }
}
