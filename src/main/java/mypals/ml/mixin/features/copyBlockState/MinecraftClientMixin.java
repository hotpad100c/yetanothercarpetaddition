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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
//#if MC >= 12006
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
//#else
//$$ import net.minecraft.nbt.NbtCompound;
//#endif
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = "<1.21.4"))
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    //#if MC >= 12104
    //#else
    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "doItemPick", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"))
    private void doItemPick(CallbackInfo ci, @Local ItemStack itemStack) {
        if (!YetAnotherCarpetAdditionRules.copyBlockState) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (this.crosshairTarget != null && this.crosshairTarget.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
            if (this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) this.crosshairTarget).getBlockPos();
                if (Screen.hasShiftDown()) {
                    setBlockStateData(itemStack, player.getWorld().getBlockState(blockPos));
                }
            }
        }

    }

    @Unique
    private static void setBlockStateData(ItemStack stack, BlockState state) {
        //#if MC >= 12006
        Map<String, String> map = new HashMap<>();

        for (Property<?> property : state.getProperties()) {
            setPropertyToMap(state, (Property<?>) property, map);
        }

        BlockStateComponent component = new BlockStateComponent(map);
        stack.set(DataComponentTypes.BLOCK_STATE, component);
        //#else
        //$$ NbtCompound nbt = new NbtCompound();
        //$$ state.getEntries().forEach((property, value) -> {
        //$$     nbt.putString(property.getName(), value.toString());
        //$$});
        //$$stack.getOrCreateNbt().put("BlockStateTag", nbt);
        //#endif

    }

    //#if MC > 12004
    @Unique
    private static <T extends Comparable<T>> void setPropertyToMap(BlockState state, Property<T> property, Map<String, String> map) {
        T value = state.get(property);
        map.put(property.getName(), property.name(value));
    }


    //#else
    //$$ @Unique
    //$$ private static <T extends Comparable<T>> String getPropertyValueAsString(BlockState state, Property<T> property) {
    //$$    T value = state.get(property);
    //$$    return property.name(value);
    //$$ }
    //#endif

    //#endif
}
