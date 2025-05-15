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


    @ModifyVariable(method = "executeDismount", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.stopRiding ()V", shift = At.Shift.AFTER),index = 2)
    private static Entity playerDismount(Entity entity){
        if (entity.getType() == EntityType.PLAYER){
            ((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
        }
        return null;
    }
}
