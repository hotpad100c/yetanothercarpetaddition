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
