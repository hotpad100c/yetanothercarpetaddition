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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.commands.RideCommand.class)
public class RideCommandMixin {
    @Inject(method = "mount", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/server/commands/RideCommand;ERROR_MOUNTING_PLAYER:Lcom/mojang/brigadier/exceptions/SimpleCommandExceptionType;"))
    private static void playerMount(CommandSourceStack source, Entity rider, Entity vehicle, CallbackInfoReturnable<Integer> cir) {
        if (!rider.level().isClientSide() && YetAnotherCarpetAdditionRules.enableMountPlayers && rider != vehicle) {
            while (rider.getFirstPassenger() != null) {
                rider = rider.getFirstPassenger();
            }
            rider.startRiding(vehicle);

            ((ServerPlayer) vehicle).connection.send(new ClientboundSetPassengersPacket(vehicle));
            Entity finalRider = rider;
            source.sendSuccess(() -> Component.translatable("commands.ride.mount.success", finalRider.getDisplayName(), vehicle.getDisplayName()), true);
            cir.setReturnValue(1);
        }

    }

    @ModifyVariable(method = "dismount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V", shift = At.Shift.AFTER), index = 2)
    private static Entity playerDismount(Entity entity) {
        if (entity.getType() == EntityType.PLAYER) {
            ((ServerPlayer) entity).connection.send(new ClientboundSetPassengersPacket(entity));
        }
        return null;
    }
}
