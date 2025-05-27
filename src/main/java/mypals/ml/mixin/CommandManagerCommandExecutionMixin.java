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

package mypals.ml.mixin;

import com.mojang.brigadier.ParseResults;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.*;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mixin(CommandManager.class)
public class CommandManagerCommandExecutionMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private void onCommandExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
        if (command.startsWith("carpet") && command.contains("hopperCooldownVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("scheduledTickVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.scheduledTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("randomTickVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.randomTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("blockEventVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("gameEventVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.gameEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("blockUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.NC);
        }
        if (command.startsWith("carpet") && command.contains("stateUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.PP);
        }
        if (command.startsWith("carpet") && command.contains("comparatorUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.CP);
        }
        if (command.startsWith("carpet") && command.contains("blockEntityOrderVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("POIVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.poiVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }


    }


}
