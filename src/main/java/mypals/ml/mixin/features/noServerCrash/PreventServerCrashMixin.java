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

package mypals.ml.mixin.features.noServerCrash;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class PreventServerCrashMixin {

    @Shadow
    public abstract PlayerList getPlayerList();


    @Shadow
    public abstract void tickServer(BooleanSupplier shouldKeepTicking);

    @WrapOperation(method = "runServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;waitUntilNextTick()V"))
    private void preventServerCrashRunServer(MinecraftServer instance, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call(instance);
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                if (this != null && this.getPlayerList() != null) {
                    this.getPlayerList().broadcastSystemMessage(Component.literal("[CrashPrevented] " + t)
                            .withStyle(ChatFormatting.RED).withStyle(s -> s.withHoverEvent(HoverEvent.showText(Component.literal(t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage())))), false);
                }
            }
        } else {
            original.call(instance);
        }
    }

    @WrapMethod(method = "tickServer")
    private void preventServerCrashAll(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call(shouldKeepTicking);
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                if (this != null && this.getPlayerList() != null) {
                    this.getPlayerList().broadcastSystemMessage(Component.literal("[CrashPrevented] " + t)
                            .withStyle(ChatFormatting.RED).withStyle(s -> s.withHoverEvent(HoverEvent.showText(Component.literal(t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage())))), false);
                }
            }
        } else {
            original.call(shouldKeepTicking);
        }
    }

    @WrapOperation(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void preventServerCrashWorld(ServerLevel instance, BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call(instance, shouldKeepTicking);
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                if (this != null && this.getPlayerList() != null) {
                    this.getPlayerList().broadcastSystemMessage(Component.literal("[CrashPrevented] " + t)
                            .withStyle(ChatFormatting.RED).withStyle(s -> s.withHoverEvent(HoverEvent.showText(Component.literal(t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage())))), false);
                }
            }
        } else {
            original.call(instance, shouldKeepTicking);
        }
    }
}
