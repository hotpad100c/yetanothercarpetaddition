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

import carpet.logging.Logger;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import carpet.utils.Messenger;
import mypals.ml.features.betterCommands.TrajectoryLogHelperExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(TrajectoryLogHelper.class)
public abstract class ProjectileLoggerMixin implements TrajectoryLogHelperExtension {
    @Shadow
    public abstract void onFinish();

    @Shadow
    private boolean doLog;

    @Shadow
    @Final
    private Logger logger;

    @Shadow
    @Final
    private ArrayList<Vec3d> positions;

    @Shadow
    @Final
    private ArrayList<Vec3d> motions;

    @Shadow
    @Final
    private static int MAX_TICKS_PER_LINE;

    @Override
    public void yetanothercarpetaddition$finish(Entity entity, Vec3d posEnd, Vec3d velocityEnd) {
        if (!this.doLog || !YetAnotherCarpetAdditionRules.commandEnhance) return;
        this.logger.log((option) -> {
            List<Text> comp = new ArrayList<>();
            MutableText header = Text.literal("--=== ");
            header.append(entity.getDisplayName()).styled(style -> style.withHoverEvent(
                    HoverEvent.showText(Text.of(entity.getUuidAsString()))
            ));
            header.append(" ===--");
            header.append("\n");
            header.append(Messenger.tp(""
                    , posEnd));
            comp.add(header);
            return comp.toArray(new Text[0]);
        });
    }
}
