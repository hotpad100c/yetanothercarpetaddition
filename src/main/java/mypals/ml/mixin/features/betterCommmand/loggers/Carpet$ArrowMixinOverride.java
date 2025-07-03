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

package mypals.ml.mixin.features.betterCommmand.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import mypals.ml.features.betterCommands.TrajectoryLogHelperExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PersistentProjectileEntity.class, priority = 1)
public abstract class Carpet$ArrowMixinOverride extends Entity {
    @Unique
    private TrajectoryLogHelper YACA$logHelper;

    public Carpet$ArrowMixinOverride(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void addLogger(EntityType<? extends ProjectileEntity> entityType_1, World world_1, CallbackInfo ci) {
        if (LoggerRegistry.__projectiles && !world_1.isClient)
            YACA$logHelper = new TrajectoryLogHelper("projectiles");
    }

    @Inject(method = "onEntityHit", at = @At("RETURN"))
    private void removeOnEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (LoggerRegistry.__projectiles && YACA$logHelper != null) {
            if (YetAnotherCarpetAdditionRules.commandEnhance)
                ((TrajectoryLogHelperExtension) YACA$logHelper).yetanothercarpetaddition$finish(this, getPos(), getVelocity());
            YACA$logHelper = null;
        }
    }

    @Inject(method = "onBlockHit", at = @At("RETURN"))
    private void removeOnBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (LoggerRegistry.__projectiles && YACA$logHelper != null) {
            if (YetAnotherCarpetAdditionRules.commandEnhance)
                ((TrajectoryLogHelperExtension) YACA$logHelper).yetanothercarpetaddition$finish(this, getPos(), getVelocity());
            YACA$logHelper = null;
        }
    }

}
