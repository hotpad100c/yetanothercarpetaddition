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
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Predicate;

@Mixin(ClearInventoryCommands.class)
public class ClearCommandMixin {
    private static final DynamicCommandExceptionType FAILED_SINGLE_EXCEPTION = new DynamicCommandExceptionType(
            playerName -> Component.translatableEscape("clear.failed.single", playerName)
    );
    private static final DynamicCommandExceptionType FAILED_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(
            playerCount -> Component.translatableEscape("clear.failed.multiple", playerCount)
    );
    private static final ThreadLocal<List<ItemStack>> REMOVED_ITEMS = ThreadLocal.withInitial(ArrayList::new);

    @Inject(
            method = "clearInventory(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Ljava/util/function/Predicate;I)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onExecute(CommandSourceStack source, Collection<ServerPlayer> targets, Predicate<ItemStack> item, int maxCount, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false") || (YetAnotherCarpetAdditionRules.commandEnhance.equals("player") && !source.isPlayer())) {
            return;
        }

        List<ItemStack> removedItems = new ArrayList<>();
        int totalRemoved = 0;

        for (ServerPlayer player : targets) {
            Inventory inventory = player.getInventory();
            totalRemoved += removeItemsWithTracking(inventory, item, maxCount, removedItems);
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(inventory);
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
                    source.sendSuccess(() -> createFeedbackWithTooltip("commands.clear.test.single", finalTotal, targets.iterator().next().getDisplayName(), removedItems), true);
                } else {
                    source.sendSuccess(() -> createFeedbackWithTooltip("commands.clear.test.multiple", finalTotal, targets.size(), removedItems), true);
                }
            } else {
                if (targets.size() == 1) {
                    source.sendSuccess(() -> createFeedbackWithTooltip("commands.clear.success.single", finalTotal, targets.iterator().next().getDisplayName(), removedItems), true);
                } else {
                    source.sendSuccess(() -> createFeedbackWithTooltip("commands.clear.success.multiple", finalTotal, targets.size(), removedItems), true);
                }
            }

            cir.setReturnValue(totalRemoved);
            cir.cancel();
        }
    }

    //remove and collect
    private static int removeItemsWithTracking(Inventory inventory, Predicate<ItemStack> item, int maxCount, List<ItemStack> removedItems) {
        int removedCount = 0;

        for (int i = 0; i < inventory.getContainerSize() && (maxCount == -1 || removedCount < maxCount); ++i) {
            ItemStack stack = inventory.getItem(i);
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
    private static MutableComponent createFeedbackWithTooltip(String translationKey, int count, Object arg, List<ItemStack> removedItems) {
        MutableComponent baseText = Component.translatable(translationKey, count, arg);
        MutableComponent tooltip = Component.literal("");

        // merge
        Map<Item, Integer> itemCounts = new HashMap<>();
        for (ItemStack stack : removedItems) {
            itemCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }

        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
            ItemStack representativeStack = new ItemStack(entry.getKey(), 1);
            tooltip.append(Component.literal("- ").append(representativeStack.getHoverName()).append(" x" + entry.getValue() + "\n"));
        }

        return baseText.setStyle(Style.EMPTY.withHoverEvent(HoverEvent.showText(tooltip)));
    }
}
