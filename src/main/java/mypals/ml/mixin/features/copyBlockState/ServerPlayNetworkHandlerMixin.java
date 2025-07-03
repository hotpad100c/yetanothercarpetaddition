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

package mypals.ml.mixin.features.copyBlockState;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.ModIds;
import net.minecraft.block.BlockState;
//#if MC >= 12104
import net.minecraft.component.DataComponentTypes;
//$$import net.minecraft.component.type.BlockStateComponent;
//$$import net.minecraft.entity.player.PlayerEntity;
//$$import net.minecraft.item.ItemStack;
//$$import net.minecraft.nbt.NbtCompound;
//$$import net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket;
//#endif
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = ">=1.21.4"))
@Mixin(ServerPlayNetworkHandler.class)

public class ServerPlayNetworkHandlerMixin {
//#if MC >= 12104
//$$     @Inject(method = "onPickItemFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onPickItem(Lnet/minecraft/item/ItemStack;)V"))
//$$     private void injectBlockStateData(PickItemFromBlockC2SPacket packet, CallbackInfo ci, @Local ItemStack itemStack) {
//$$         if (!YetAnotherCarpetAdditionRules.copyBlockState) return;
//$$
//$$         ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
//$$         PlayerEntity player = handler.player;
//$$         World serverWorld = player.getWorld();
//$$         BlockPos blockPos = packet.pos();
//$$         if (player.isSneaking()) {
//$$             BlockState blockState = serverWorld.getBlockState(blockPos);
//$$             setBlockStateData(itemStack, blockState);
//$$         }
//$$     }

//$$     @Unique
//$$     private static void setBlockStateData(ItemStack stack, BlockState state) {
//$$         Map<String, String> map = new HashMap<>();
//$$
//$$         for (Property<?> property : state.getProperties()) {
//$$             setPropertyToMap(state, (Property<?>) property, map);
//$$         }
//$$
//$$         BlockStateComponent component = new BlockStateComponent(map);
//$$         stack.set(DataComponentTypes.BLOCK_STATE, component);
//$$     }
//$$
//$$     private static <T extends Comparable<T>> void setPropertyToMap(BlockState state, Property<T> property, Map<String, String> map) {
//$$         T value = state.get(property);
//$$         map.put(property.getName(), property.name(value));
//$$     }
//#endif
}
