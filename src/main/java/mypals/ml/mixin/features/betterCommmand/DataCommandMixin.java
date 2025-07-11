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
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.*;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
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

import static net.minecraft.server.command.DataCommand.getNbt;

@Mixin(DataCommand.class)
public class DataCommandMixin {
    @Shadow
    @Final
    private static DynamicCommandExceptionType GET_INVALID_EXCEPTION;

    @Shadow
    @Final
    private static DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION;

    @Inject(
            method = "executeModify",
            at = @At("HEAD")
    )
    private static void beforeModify(CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<NbtElement> elements, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException, CommandSyntaxException {
        DataCommandObject dataObject = objectType.getObject(context);
        NbtCompound originalNbt = dataObject.getNbt().copy();
        DataModifyCapture.setOriginalNbt(originalNbt);
    }

    @WrapOperation(
            method = "executeModify",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Ljava/util/function/Supplier;Z)V")
    )
    private static void onAfterModify(ServerCommandSource instance, Supplier<Text> feedbackSupplier,
                                      boolean broadcastToOps, Operation<Void> original, @Local DataCommandObject dataCommandObject) throws CommandSyntaxException {
        NbtCompound after = dataCommandObject.getNbt();
        NbtCompound before = DataModifyCapture.getOriginalNbt();

        List<Text> diffs = new ArrayList<>();
        for (String key : after.getKeys()) {
            NbtElement newVal = after.get(key);
            NbtElement oldVal = before.get(key);
            if (oldVal == null || !oldVal.equals(newVal)) {
                diffs.add(Text.literal("§e" + key + "§r: ")
                        .append(Text.literal(oldVal == null ? "null" : oldVal.toString()).formatted(Formatting.RED))
                        .append(" -> ")
                        .append(Text.literal(newVal.toString()).formatted(Formatting.GREEN)));
            }
        }

        if (!diffs.isEmpty()) {
            MutableText hoverText = Text.literal("§aModified:").append("\n");
            for (Text line : diffs) {
                hoverText.append(line).append("\n");
            }
            Text feedBack = feedbackSupplier.get();
            Supplier<Text> st = () -> feedBack.copy().styled(style -> style.withHoverEvent(
                    HoverEvent.showText(hoverText)
            ));
            original.call(instance, st, broadcastToOps);
        }
    }

    @Inject(
            method = "executeGet(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(ServerCommandSource source, DataCommandObject object, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;
        NbtCompound nbtCompound = object.getNbt();
        String targetStr = getTargetString(object);
        MutableText feedback = Text.literal(getTargetString(object) + ":").formatted(Formatting.GREEN);

        appendNbtWithClickablePaths(feedback, nbtCompound, "", targetStr);
        source.sendFeedback(() -> feedback, false);
        cir.setReturnValue(1);
    }

    private static String getTargetString(DataCommandObject object) {

        if (object instanceof EntityDataObject entityDataObject) {
            return "entity " + entityDataObject.entity.getUuidAsString();
        } else if (object instanceof BlockDataObject blockDataObject) {
            return "block " + blockDataObject.pos.getX() + " " + blockDataObject.pos.getY() + " " + blockDataObject.pos.getZ();
        } else if (object instanceof StorageDataObject storageDataObject) {
            return "storage " + storageDataObject.id;
        }
        return "unknown";
    }

    @Inject(
            method = "executeGet(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;
        NbtElement nbtElement = getNbt(path, object);
        int i;
        if (nbtElement instanceof AbstractNbtNumber) {
            i = MathHelper.floor(((AbstractNbtNumber) nbtElement).doubleValue());
        } else if (nbtElement instanceof AbstractNbtList) {
            i = ((AbstractNbtList
                    //#if MC < 12105
                    <?>
                    //#endif
                    )
                    nbtElement).size();
        } else if (nbtElement instanceof NbtCompound) {
            i = ((NbtCompound) nbtElement).getSize();
        } else if (nbtElement instanceof NbtString) {
            i = nbtElement.toString().length();
        } else {
            throw GET_UNKNOWN_EXCEPTION.create(path.toString());
        }

        String targetStr = getTargetString(object) + " ";
        MutableText feedback = Text.literal(getTargetString(object) + ":").formatted(Formatting.GREEN);
        appendNbtWithClickablePaths(feedback, nbtElement, path.toString(), targetStr);
        source.sendFeedback(() -> feedback, false);
        cir.setReturnValue(i);
    }

    @Inject(
            method = "executeGet(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/DataCommandObject;Lnet/minecraft/command/argument/NbtPathArgumentType$NbtPath;D)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customExecuteGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;
        NbtElement nbtElement = getNbt(path, object);
        if (!(nbtElement instanceof AbstractNbtNumber)) {
            throw GET_INVALID_EXCEPTION.create(path.toString());
        }
        int i = MathHelper.floor(((AbstractNbtNumber) nbtElement).doubleValue() * scale);

        String targetStr = getTargetString(object) + " " + path.toString() + " " + scale;
        MutableText feedback = Text.literal(getTargetString(object) + ":").formatted(Formatting.GREEN);
        appendNbtWithClickablePaths(
                feedback,
                nbtElement,
                path.toString(),
                targetStr
        );
        source.sendFeedback(() -> feedback, false);
        cir.setReturnValue(i);
    }

    private static void appendNbtWithClickablePaths(MutableText text, NbtElement element, String currentPath, String targetStr) {
        if (element instanceof NbtCompound compound) {
            text.append(Text.literal("{\n"));
            for (String key : compound.getKeys()) {
                NbtElement child = compound.get(key);
                String path = currentPath + key;

                MutableText line = Text.literal("  " + key + ": ")
                        .append(renderNbtAsClickable(child, path, targetStr))
                        .append(Text.literal("\n"));

                text.append(line);
            }
            text.append(Text.literal("}"));
        } else if (element instanceof AbstractNbtList
                //#if MC < 12105
                <?>
                //#endif
                list) {
            text.append(Text.literal("[\n"));
            int i = 0;
            for (NbtElement child : list) {
                ++i;
                String path = currentPath + "[" + i + "]";
                text.append(renderNbtAsClickable(child, path, targetStr));
            }
            text.append(Text.literal("]"));
        } else {
            text.append(renderNbtAsClickable(element, currentPath, targetStr));
        }
    }

    @Unique
    private static MutableText renderNbtAsClickable(NbtElement element, String path, String targetStr) {
        String cmd = "/data modify " + targetStr + " " + path + " set value ...";
        return Text.literal(element.toString())
                .styled(style -> style
                        .withColor(Formatting.YELLOW)
                        .withClickEvent(ClickEvent.suggestCommand(cmd))

                        .withHoverEvent(HoverEvent.showText(Text.literal(cmd)))
                );
    }
}
