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

package mypals.ml.mixin.client.optionalTicking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.transformer.meta.MixinInner;

import java.util.Iterator;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderFreeze {
    @Shadow
    private int ticks;

    @Shadow
    @Nullable
    private ClientWorld world;

    @Shadow
    @Final
    private Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos;

    @Shadow
    protected abstract void removeBlockBreakingInfo(BlockBreakingInfo info);

    @WrapOperation(
            //#if MC < 12102
            method = "render",
            //#else
            //$$ method = "renderEntities",
            //#endif
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void blockTickEntityRender(WorldRenderer instance, Entity entity,
                                       double cameraX, double cameraY, double cameraZ,
                                       float tickDelta, MatrixStack matrices,
                                       VertexConsumerProvider vertexConsumers, Operation<Void> original,
                                       @Local(argsOnly = true) RenderTickCounter renderTickCounter) {
        tickDelta = YetAnotherCarpetAdditionRules.stopTickingEntities? 1.0F : tickDelta;
        original.call(instance, entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers);
    }

    @WrapMethod(method = "tick")
    private void blockTick(Operation<Void> original) {
        if (this.world.getTickManager().shouldTick() &&
                !YetAnotherCarpetAdditionRules.stopTickingBlockEntities &&
                !YetAnotherCarpetAdditionRules.stopTickingWeather &&
                !YetAnotherCarpetAdditionRules.stopTickingBlocks &&
                !YetAnotherCarpetAdditionRules.stopTickingFluids) {
            ++this.ticks;
        }

        if (this.ticks % 20 == 0) {
            Iterator<BlockBreakingInfo> iterator = this.blockBreakingInfos.values().iterator();

            while (iterator.hasNext()) {
                BlockBreakingInfo blockBreakingInfo = (BlockBreakingInfo) iterator.next();
                int i = blockBreakingInfo.getLastUpdateTick();
                if (this.ticks - i > 400) {
                    iterator.remove();
                    this.removeBlockBreakingInfo(blockBreakingInfo);
                }
            }

        }

    }
}
