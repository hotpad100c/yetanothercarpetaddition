/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Ryan100c and contributors
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

package mypals.ml.mixin.features.instantFalling;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {

	@Unique
	private static boolean isStalactite(BlockState state) {
		return state.is(Blocks.POINTED_DRIPSTONE) && state.getValue(PointedDripstoneBlock.TIP_DIRECTION) == Direction.DOWN;
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void scheduledTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (!YetAnotherCarpetAdditionRules.instantFalling) {
			return;
		}
		if (!isStalactite(state)) {
			return;
		}
		BlockPos.MutableBlockPos cursor = pos.mutable();
		BlockState cursorState = state;
		while (isStalactite(cursorState)) {
			world.destroyBlock(cursor, true);
			cursor.move(Direction.DOWN);
			cursorState = world.getBlockState(cursor);
		}

		ci.cancel();
	}
}
