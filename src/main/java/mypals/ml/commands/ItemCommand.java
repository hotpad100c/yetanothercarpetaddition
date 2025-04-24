package mypals.ml.commands;

import carpet.CarpetSettings;
import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ItemCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("rename")
                .requires((player) -> CommandHelper.canUseCommand(player, YetAnotherCarpetAdditionRules.commandRenameItem))
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")
                        )))
                .executes(context -> execute(context.getSource())));
        dispatcher.register(CommandManager.literal("itemshadowing")
                .requires((player) -> CommandHelper.canUseCommand(player, YetAnotherCarpetAdditionRules.commandEasyItemShadowing))
                .executes(context -> itemShadowing(context.getSource())));
    }

    public static int execute(ServerCommandSource source, String name) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayerEntity)) return 0;

        ItemStack itemStack = ((ServerPlayerEntity) entity).getMainHandStack();

        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));

        return 1;
    }

    public static int execute(ServerCommandSource source) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayerEntity)) return 0;

        ItemStack itemStack = ((ServerPlayerEntity) entity).getMainHandStack();

        itemStack.remove(DataComponentTypes.CUSTOM_NAME);

        return 1;
    }

    public static int itemShadowing(ServerCommandSource source) {
        Entity entity = source.getEntity();
        if(!(entity instanceof ServerPlayerEntity)) return 0;

        ((ServerPlayerEntity) entity).equipStack(EquipmentSlot.OFFHAND, ((ServerPlayerEntity) entity).getEquippedStack(EquipmentSlot.MAINHAND));

        return 1;
    }
}
