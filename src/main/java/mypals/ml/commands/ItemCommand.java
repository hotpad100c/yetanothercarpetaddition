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

package mypals.ml.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

//#if MC >= 12006
import net.minecraft.core.component.DataComponents;
//#endif

public class ItemCommand {
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("rename")
                .requires((player) -> CommandHelper.canUseCommand(player, YetAnotherCarpetAdditionRules.commandRenameItem))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")
                        )))
                .executes(context -> execute(context.getSource())));
        dispatcher.register(Commands.literal("itemshadowing")
                .requires((player) -> CommandHelper.canUseCommand(player, YetAnotherCarpetAdditionRules.commandEasyItemShadowing))
                .executes(context -> itemShadowing(context.getSource())));
    }

    public static int execute(CommandSourceStack source, String name) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayer)) return 0;

        ItemStack itemStack = ((ServerPlayer) entity).getMainHandItem();

        //#if MC >= 12006
        itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        //#else
        //$$ itemStack.setHoverName(Component.literal(name));
        //#endif

        return 1;
    }

    public static int execute(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayer)) return 0;

        ItemStack itemStack = ((ServerPlayer) entity).getMainHandItem();

        //#if MC >= 12006
        itemStack.remove(DataComponents.CUSTOM_NAME);
        //#else
        //$$ itemStack.resetHoverName();
        //#endif


        return 1;
    }

    public static int itemShadowing(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayer)) return 0;

        ((ServerPlayer) entity).setItemSlot(EquipmentSlot.OFFHAND, ((ServerPlayer) entity).getItemBySlot(EquipmentSlot.MAINHAND));

        return 1;
    }
}
