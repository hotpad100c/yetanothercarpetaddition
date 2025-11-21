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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import mypals.ml.features.betterCommands.DataModifyCapture;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.server.commands.data.DataCommands.getSingleTag;

@Mixin(DataCommands.class)
public class DataCommandMixin {
    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_GET_NOT_NUMBER;

    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_GET_NON_EXISTENT;

    @Inject(
            method = "manipulateData",
            at = @At("HEAD")
    )
    private static void beforeModify(CommandContext<CommandSourceStack> context, DataCommands.DataProvider objectType, DataCommands.DataManipulator modifier, List<Tag> elements, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException, CommandSyntaxException {
        DataAccessor dataObject = objectType.access(context);
        CompoundTag originalNbt = dataObject.getData().copy();
        DataModifyCapture.setOriginalNbt(originalNbt);
    }

    @WrapOperation(
            method = "manipulateData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;sendSuccess(Ljava/util/function/Supplier;Z)V")
    )
    private static void onAfterModify(CommandSourceStack instance, Supplier<Component> feedbackSupplier,
                                      boolean broadcastToOps, Operation<Void> original, @Local DataAccessor dataCommandObject) throws CommandSyntaxException {
        CompoundTag after = dataCommandObject.getData();
        CompoundTag before = DataModifyCapture.getOriginalNbt();

        List<Component> diffs = new ArrayList<>();
        for (String key : after.getAllKeys()) {
            Tag newVal = after.get(key);
            Tag oldVal = before.get(key);
            if (oldVal == null || !oldVal.equals(newVal)) {
                diffs.add(Component.literal("§e" + key + "§r: ")
                        .append(Component.literal(oldVal == null ? "null" : oldVal.toString()).withStyle(ChatFormatting.RED))
                        .append(" -> ")
                        .append(Component.literal(newVal.toString()).withStyle(ChatFormatting.GREEN)));
            }
        }

        if (!diffs.isEmpty()) {
            MutableComponent hoverText = Component.literal("§aModified:").append("\n");
            for (Component line : diffs) {
                hoverText.append(line).append("\n");
            }
            Component feedBack = feedbackSupplier.get();
            Supplier<Component> st = () -> feedBack.copy().withStyle(style -> style.withHoverEvent(
                    HoverEvent.showText(hoverText)
            ));
            original.call(instance, st, broadcastToOps);
        }
    }

    @Inject(
            method = "getData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(CommandSourceStack source, DataAccessor object, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false") || (YetAnotherCarpetAdditionRules.commandEnhance.equals("player") && !source.isPlayer())) {
            return;
        }
        CompoundTag nbtCompound = object.getData();
        String targetStr = getTargetString(object);
        MutableComponent feedback = Component.literal(getTargetString(object) + ":").withStyle(ChatFormatting.GREEN);

        appendNbtWithClickablePaths(feedback, nbtCompound, "", targetStr);
        source.sendSuccess(() -> feedback, false);
        cir.setReturnValue(1);
    }

    private static String getTargetString(DataAccessor object) {

        if (object instanceof EntityDataAccessor entityDataObject) {
            return "entity " + entityDataObject.entity.getUuidAsString();
        } else if (object instanceof BlockDataAccessor blockDataObject) {
            return "block " + blockDataObject.pos.getX() + " " + blockDataObject.pos.getY() + " " + blockDataObject.pos.getZ();
        } else if (object instanceof StorageDataAccessor storageDataObject) {
            return "storage " + storageDataObject.id;
        }
        return "unknown";
    }

    @Inject(
            method = "getData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false") || (YetAnotherCarpetAdditionRules.commandEnhance.equals("player") && !source.isPlayer())) {
            return;
        }
        Tag nbtElement = getSingleTag(path, object);
        int i;
        if (nbtElement instanceof NumericTag) {
            i = Mth.floor(((NumericTag) nbtElement).getAsDouble());
        } else if (nbtElement instanceof CollectionTag) {
            i = ((CollectionTag
                    //#if MC < 12105
                    <?>
                    //#endif
                    )
                    nbtElement).size();
        } else if (nbtElement instanceof CompoundTag) {
            i = ((CompoundTag) nbtElement).size();
        } else if (nbtElement instanceof StringTag) {
            i = nbtElement.toString().length();
        } else {
            throw ERROR_GET_NON_EXISTENT.create(path.toString());
        }

        String targetStr = getTargetString(object) + " ";
        MutableComponent feedback = Component.literal(getTargetString(object) + ":").withStyle(ChatFormatting.GREEN);
        appendNbtWithClickablePaths(feedback, nbtElement, path.toString(), targetStr);
        source.sendSuccess(() -> feedback, false);
        cir.setReturnValue(i);
    }

    @Inject(
            method = "getNumeric(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;D)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path, double scale, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false") || (YetAnotherCarpetAdditionRules.commandEnhance.equals("player") && !source.isPlayer())) {
            return;
        }
        Tag nbtElement = getSingleTag(path, object);
        if (!(nbtElement instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create(path.toString());
        }
        int i = Mth.floor(((NumericTag) nbtElement).getAsDouble() * scale);

        String targetStr = getTargetString(object) + " " + path.toString() + " " + scale;
        MutableComponent feedback = Component.literal(getTargetString(object) + ":").withStyle(ChatFormatting.GREEN);
        appendNbtWithClickablePaths(
                feedback,
                nbtElement,
                path.toString(),
                targetStr
        );
        source.sendSuccess(() -> feedback, false);
        cir.setReturnValue(i);
    }

    private static void appendNbtWithClickablePaths(MutableComponent text, Tag element, String currentPath, String targetStr) {
        if (element instanceof CompoundTag compound) {
            text.append(Component.literal("{\n"));
            for (String key : compound.getAllKeys()) {
                Tag child = compound.get(key);
                String path = currentPath + key;

                MutableComponent line = Component.literal("  " + key + ": ")
                        .append(renderNbtAsClickable(child, path, targetStr))
                        .append(Component.literal("\n"));

                text.append(line);
            }
            text.append(Component.literal("}"));
        } else if (element instanceof CollectionTag
                //#if MC < 12105
                <?>
                //#endif
                list) {
            text.append(Component.literal("[\n"));
            int i = 0;
            for (Tag child : list) {
                ++i;
                String path = currentPath + "[" + i + "]";
                text.append(renderNbtAsClickable(child, path, targetStr));
            }
            text.append(Component.literal("]"));
        } else {
            text.append(renderNbtAsClickable(element, currentPath, targetStr));
        }
    }

    @Unique
    private static MutableComponent renderNbtAsClickable(Tag element, String path, String targetStr) {
        String cmd = "/data modify " + targetStr + " " + path + " set value ...";
        return Component.literal(element.toString())
                .withStyle(style -> style
                        .withColor(ChatFormatting.YELLOW)
                        .withClickEvent(ClickEvent.suggestCommand(cmd))

                        .withHoverEvent(HoverEvent.showText(Component.literal(cmd)))
                );
    }
}
