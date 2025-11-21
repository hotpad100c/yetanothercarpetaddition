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
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mypals.ml.YetAnotherCarpetAdditionClient;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

//#if MC >= 12109
//$$ import net.minecraft.client.render.Camera;
//#endif

import java.util.Iterator;

@Mixin(LevelRenderer.class)
public abstract class WorldRenderFreeze {
    @Shadow
    private int ticks;

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Shadow
    protected abstract void removeProgress(BlockDestructionProgress info);

    //#if MC < 12109
    @WrapOperation(
            //#if MC < 12102
            method = "renderLevel",
            //#else
            //$$ method = "renderEntities",
            //#endif
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void blockTickEntityRender(LevelRenderer instance, Entity entity,
                                       double cameraX, double cameraY, double cameraZ,
                                       float tickDelta, PoseStack matrices,
                                       MultiBufferSource vertexConsumers, Operation<Void> original) {
        tickDelta = (YetAnotherCarpetAdditionRules.stopTickingEntities
                || YetAnotherCarpetAdditionClient.selectiveFreezeManager
                .stopTickingEntities) && !(entity instanceof Player) ? 1.0F : tickDelta;
        original.call(instance, entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers);
    }
    //#else
    //$$ @ModifyArgs(
    //$$         method = "getAndUpdateRenderState",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/client/render/entity/EntityRenderManager;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;"
    //$$         )
    //$$ )
    //$$ public void blockTickEntityRender(Args args) {
    //$$     Entity entity = args.get(0);
    //$$     float tickDelta = args.get(1);
    //$$     tickDelta = (YetAnotherCarpetAdditionRules.stopTickingEntities
    //$$             || YetAnotherCarpetAdditionClient.selectiveFreezeManager
    //$$             .stopTickingEntities) && !(entity instanceof PlayerEntity) ? 1.0F : tickDelta;
    //$$     args.set(1, tickDelta);
    //$$ }
    //#endif

    @WrapMethod(method = "tick")
    private void blockTick(
            //#if MC >= 12109
            //$$ Camera camera,
            //#endif
            Operation<Void> original
    ) {
        if (this.level.tickRateManager().runsNormally() &&
                !YetAnotherCarpetAdditionRules.stopTickingBlockEntities &&
                !YetAnotherCarpetAdditionRules.stopTickingWeather &&
                !YetAnotherCarpetAdditionRules.stopTickingBlocks &&
                !YetAnotherCarpetAdditionRules.stopTickingFluids &&
                !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingBlockEntities &&
                !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingWeather &&
                !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileBlocks &&
                !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileFluids &&
                !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileTick) {
            ++this.ticks;
        }

        if (this.ticks % 20 == 0) {
            Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

            while (iterator.hasNext()) {
                BlockDestructionProgress blockBreakingInfo = (BlockDestructionProgress) iterator.next();
                int i = blockBreakingInfo.getUpdatedRenderTick();
                if (this.ticks - i > 400) {
                    iterator.remove();
                    this.removeProgress(blockBreakingInfo);
                }
            }

        }

    }
}
