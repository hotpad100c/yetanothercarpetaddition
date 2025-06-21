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

package mypals.ml.mixin.features;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {


  @ModifyExpressionValue(
        method = "execute",
        at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"
      )
    )
    private boolean modifyLevelCheck(boolean original, ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment, int level) {
          return original && !YetAnotherCarpetAdditionRules.enchantCommandLimitOverwrite;
   }


    
    @ModifyExpressionValue(
            method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z")
    )
    private static boolean isAcceptableItem(boolean original) {
        return YetAnotherCarpetAdditionRules.enchantCommandBypassItemType || original;
    }

    @ModifyExpressionValue(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    //#if MC >= 12101
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;isCompatible(Ljava/util/Collection;Lnet/minecraft/registry/entry/RegistryEntry;)Z"
                    //#else
                    //$$ target = "Lnet/minecraft/enchantment/EnchantmentHelper;isCompatible(Ljava/util/Collection;Lnet/minecraft/enchantment/Enchantment;)Z"
                    //#endif
            )
    )
    private static boolean isCompatible(boolean original) {
        return YetAnotherCarpetAdditionRules.enchantCommandBypassItemType || original;
    }
}
