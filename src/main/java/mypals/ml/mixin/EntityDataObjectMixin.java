package mypals.ml.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityDataObject.class)
public class EntityDataObjectMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "setNbt",
            at = @At(value = "FIELD", target = "Lnet/minecraft/command/EntityDataObject;INVALID_ENTITY_EXCEPTION:Lcom/mojang/brigadier/exceptions/SimpleCommandExceptionType;"),
            cancellable = true)
    public void setNbt(NbtCompound nbt, CallbackInfo ci) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.bypassModifyPlayerDataRestriction) {
            UUID uUID = this.entity.getUuid();
            this.entity.readNbt(nbt);
            this.entity.setUuid(uUID);
            ci.cancel();
        }
    }
}
