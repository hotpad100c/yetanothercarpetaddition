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

package mypals.ml.mixin.features.kExplosion;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mypals.ml.interfaces.ExplosionExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.ModIds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = "<1.21.2"))
@Mixin(Explosion.class)
public class ExplosionMixin implements ExplosionExtension {
    @Shadow @Final private World world;

    @Shadow @Final private @Nullable Entity entity;

    @Shadow @Final private Explosion.DestructionType destructionType;

    @Unique
    @Override
    public boolean preservesDecorativeEntities() {

        if(!YetAnotherCarpetAdditionRules.waterTNT)
            return true;

        boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        boolean bl2 = this.entity == null || !this.entity.isTouchingWater();
        boolean bl3 = this.entity == null || this.entity.getType() != EntityType.BREEZE_WIND_CHARGE && this.entity.getType() != EntityType.WIND_CHARGE;
        if (bl) {
            return bl2 && bl3;
        } else {
            return !(this.destructionType == Explosion.DestructionType.TRIGGER_BLOCK) && bl2 && bl3;
        }
    }
}
