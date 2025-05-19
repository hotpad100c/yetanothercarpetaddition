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

package mypals.ml.mixin.features.bypassRideRestriction;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.command.RideCommand.class)
public class RideCommandMixin {
    @Inject(method = "executeMount",cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/server/command/RideCommand;CANT_RIDE_PLAYERS_EXCEPTION:Lcom/mojang/brigadier/exceptions/SimpleCommandExceptionType;"))
    private static void playerMount(ServerCommandSource source, Entity rider, Entity vehicle, CallbackInfoReturnable<Integer> cir){
        if (!rider.getWorld().isClient && YetAnotherCarpetAdditionRules.enableMountPlayers){
            while (rider.getFirstPassenger() != null)
            {
                rider = rider.getFirstPassenger();
            }
            rider.startRiding(vehicle);

            ((ServerPlayerEntity) vehicle).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
            Entity finalRider = rider;
            source.sendFeedback(() -> Text.translatable("commands.ride.mount.success", finalRider.getDisplayName(), vehicle.getDisplayName()), true);
            cir.setReturnValue(1);
        }

    }


    @ModifyVariable(method = "executeDismount", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;stopRiding()V", shift = At.Shift.AFTER),index = 2)
    private static Entity playerDismount(Entity entity){
        if (entity.getType() == EntityType.PLAYER){
            ((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
        }
        return null;
    }
}
