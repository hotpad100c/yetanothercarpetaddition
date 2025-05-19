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

package mypals.ml.mixin.features.slienceTP;

import carpet.patches.EntityPlayerMPFake;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    @Inject(method = "execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/entity/Entity;)I",
            at = @At("HEAD"))
    private static void setPlayerMode(ServerCommandSource source, Collection<? extends Entity> targets, Entity destination, CallbackInfoReturnable<Integer> cir) {
       if(destination instanceof ServerPlayerEntity && YetAnotherCarpetAdditionRules.silenceTP){
           if(!(destination instanceof EntityPlayerMPFake)) {
               for (Entity entity : targets) {
                   if(entity instanceof ServerPlayerEntity) ((ServerPlayerEntity) entity).changeGameMode(GameMode.SPECTATOR);
               }
           }
       }
    }
}
