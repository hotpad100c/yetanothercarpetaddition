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

package mypals.ml.mixin.features.visualizers;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ScheduledTickAndEventsServerWorldMixin {
    @Shadow
    @Final
    private LevelTicks<Block> blockTicks;

    @Shadow
    @Final
    private LevelTicks<Fluid> fluidTicks;

    @Shadow
    public abstract ServerLevel getLevel();


    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void ServerTickAddScheduledTickMarker(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.scheduledTickVisualize) return;
        
        List<ScheduledTick<Block>> allBlockTicks = blockTicks.allContainers.values().stream()
                .flatMap(LevelChunkTicks::getAll)
                .sorted(Comparator.comparingLong(ScheduledTick::subTickOrder))
                .toList();
    
        int blockIndex = 1;
        for (ScheduledTick<Block> orderedTick : allBlockTicks) {
            long triggerTick = orderedTick.triggerTick();
            YetAnotherCarpetAdditionServer.scheduledTickVisualizing.setVisualizer(
                    (ServerLevel) (Object) this,
                    orderedTick.pos(),
                    triggerTick,
                    orderedTick.priority().getValue(),
                    blockIndex++,
                    Component.translatable(orderedTick.type().getDescriptionId()).getString(),
                    false
            );
        }
        List<ScheduledTick<Fluid>> allFluidTicks = fluidTicks.allContainers.values().stream()
                .flatMap(LevelChunkTicks::getAll)
                .sorted(Comparator.comparingLong(ScheduledTick::subTickOrder))
                .toList();
    
        int fluidIndex = 1;
        for (ScheduledTick<Fluid> orderedTick : allFluidTicks) {
            long triggerTick = orderedTick.triggerTick();
            YetAnotherCarpetAdditionServer.scheduledTickVisualizing.setVisualizer(
                    (ServerLevel) (Object) this,
                    orderedTick.pos(),
                    triggerTick,
                    orderedTick.priority().getValue(),
                    fluidIndex++,
                    Component.translatable(
                            orderedTick.type()
                                    .getStateDefinition()
                                    .any()
                                    .createLegacyBlock()
                                    .getBlock()
                                    .getDescriptionId()
                    ).getString(),
                    true
            );
        }
    }



    @Inject(
            method = "tickChunk",
            at = @At(target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", value = "INVOKE")
    )
    private void ServerTickAddRandomTickMarker(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci, @Local BlockPos blockPos2) {
        if (YetAnotherCarpetAdditionRules.randomTickVisualize) {
            if (blockPos2 instanceof BlockPos.MutableBlockPos mutable) {
                YetAnotherCarpetAdditionServer.randomTickVisualizing.setVisualizer(chunk.getLevel(), mutable.immutable());
            } else {
                YetAnotherCarpetAdditionServer.randomTickVisualizing.setVisualizer(chunk.getLevel(), blockPos2);
            }

        }
    }

    @Unique
    List<BlockEventData> eventCurrentTick = new ArrayList<>();

    @Inject(
            method = "doBlockEvent",
            at = @At("HEAD")
    )
    private void ServerTickAddBlockEventMarker(BlockEventData event, CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blockEventVisualize) {
            eventCurrentTick.add(event);
            YetAnotherCarpetAdditionServer.blockEventVisualizing.setVisualizer(this.getLevel(), event.pos(), Vec3.atCenterOf(event.pos()), eventCurrentTick.size());
        }
    }

    @Inject(
            method = "runBlockEvents",
            at = @At("HEAD")

    )
    private void processSyncedBlockEvents(CallbackInfo ci) {
        eventCurrentTick.clear();
    }

    @Inject(
            method = "gameEvent",
            at = @At("HEAD")
    )
    private void ServerTickAddGameEventMarker(
            //#if MC >= 12006
            Holder<GameEvent> event,
            //#else
            //$$ GameEvent event,
            //#endif
            Vec3 emitterPos, GameEvent.Context emitter, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.gameEventVisualize) {
            String type = event
                    //#if MC < 12006
                    //$$ .builtInRegistryHolder()
                    //#endif
                    .unwrapKey().get().identifier().toString();
            String emitterName = "";
            if (emitter.sourceEntity() != null) {
                emitterName = Component.translatable(emitter.sourceEntity().getType().getDescriptionId()).getString();
            } else {
                if (emitter.affectedState() != null) {
                    emitterName = Component.translatable(emitter.affectedState().getBlock().getDescriptionId()).getString();
                }
            }
            String[] eventData = new String[]{emitterName, type};
            YetAnotherCarpetAdditionServer.gameEventVisualizing.setVisualizer(this.getLevel(), emitterPos, emitterPos, eventData);
        }
    }
}
