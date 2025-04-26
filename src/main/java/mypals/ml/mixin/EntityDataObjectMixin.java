package mypals.ml.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

import static net.minecraft.command.EntitySelectorReader.INVALID_ENTITY_EXCEPTION;

@Mixin(EntityDataObject.class)
public class EntityDataObjectMixin {
    @Shadow @Final private Entity entity;

    /**
     * @author AB
     * @reason For unlock modify player
     */
    @Overwrite
    public void setNbt(NbtCompound nbt) throws CommandSyntaxException {
        if (this.entity instanceof PlayerEntity && !YetAnotherCarpetAdditionRules.bypassModifyPlayerDataRestriction) {
            throw INVALID_ENTITY_EXCEPTION.create();
        } else {
            UUID uUID = this.entity.getUuid();
            this.entity.readNbt(nbt);
            this.entity.setUuid(uUID);
        }
    }
}
