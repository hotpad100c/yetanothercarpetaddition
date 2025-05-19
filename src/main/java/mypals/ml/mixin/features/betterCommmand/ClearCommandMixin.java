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

package mypals.ml.mixin.features.betterCommmand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ClearCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Predicate;

@Mixin(ClearCommand.class)
public class ClearCommandMixin {
    private static final DynamicCommandExceptionType FAILED_SINGLE_EXCEPTION = new DynamicCommandExceptionType(
            playerName -> Text.stringifiedTranslatable("clear.failed.single", playerName)
    );
    private static final DynamicCommandExceptionType FAILED_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(
            playerCount -> Text.stringifiedTranslatable("clear.failed.multiple", playerCount)
    );
    private static final ThreadLocal<List<ItemStack>> REMOVED_ITEMS = ThreadLocal.withInitial(ArrayList::new);

    @Inject(
            method = "execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Ljava/util/function/Predicate;I)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onExecute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Predicate<ItemStack> item, int maxCount, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) {
            return;
        }

        List<ItemStack> removedItems = new ArrayList<>();
        int totalRemoved = 0;

        for (ServerPlayerEntity player : targets) {
            PlayerInventory inventory = player.getInventory();
            totalRemoved += removeItemsWithTracking(inventory, item, maxCount, removedItems);
            player.currentScreenHandler.sendContentUpdates();
            player.playerScreenHandler.onContentChanged(inventory);
        }

        REMOVED_ITEMS.set(removedItems);

        if (totalRemoved == 0) {
            if (targets.size() == 1) {
                throw FAILED_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw FAILED_MULTIPLE_EXCEPTION.create(targets.size());
            }
        } else {
            int finalTotal = totalRemoved;
            if (maxCount == 0) {
                if (targets.size() == 1) {
                    source.sendFeedback(() -> createFeedbackWithTooltip("commands.clear.test.single", finalTotal, targets.iterator().next().getDisplayName(), removedItems), true);
                } else {
                    source.sendFeedback(() -> createFeedbackWithTooltip("commands.clear.test.multiple", finalTotal, targets.size(), removedItems), true);
                }
            } else {
                if (targets.size() == 1) {
                    source.sendFeedback(() -> createFeedbackWithTooltip("commands.clear.success.single", finalTotal, targets.iterator().next().getDisplayName(), removedItems), true);
                } else {
                    source.sendFeedback(() -> createFeedbackWithTooltip("commands.clear.success.multiple", finalTotal, targets.size(), removedItems), true);
                }
            }

            cir.setReturnValue(totalRemoved);
            cir.cancel();
        }
    }

    //remove and collect
    private static int removeItemsWithTracking(PlayerInventory inventory, Predicate<ItemStack> item, int maxCount, List<ItemStack> removedItems) {
        int removedCount = 0;

        for (int i = 0; i < inventory.size() && (maxCount == -1 || removedCount < maxCount); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && item.test(stack)) {
                int countToRemove = maxCount == -1 ? stack.getCount() : Math.min(maxCount - removedCount, stack.getCount());
                if (countToRemove > 0) {
                    ItemStack removed = stack.split(countToRemove);
                    removedItems.add(removed);
                    removedCount += countToRemove;
                }
            }
        }

        return removedCount;
    }

    // tooltip
    private static MutableText createFeedbackWithTooltip(String translationKey, int count, Object arg, List<ItemStack> removedItems) {
        MutableText baseText = Text.translatable(translationKey, count, arg);
        MutableText tooltip = Text.literal("");

        // merge
        Map<Item, Integer> itemCounts = new HashMap<>();
        for (ItemStack stack : removedItems) {
            itemCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }

        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
            ItemStack representativeStack = new ItemStack(entry.getKey(), 1);
            tooltip.append(Text.literal("- ").append(representativeStack.getName()).append(" x" + entry.getValue() + "\n"));
        }

        return baseText.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }
}
