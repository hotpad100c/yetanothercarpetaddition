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

package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.selectiveFreeze.SelectiveFreezeManager;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.world.ServerEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityManager.class)
public class ServerChunkManagerMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickChunkManager(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingChunkManager || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingChunkManager) {
            ci.cancel();
        }
    }

}
