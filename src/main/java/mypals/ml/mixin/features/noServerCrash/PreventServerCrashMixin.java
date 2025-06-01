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
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class PreventServerCrashMixin {

    @Shadow
    public abstract PlayerManager getPlayerManager();

    @WrapMethod(method = "runServer")
    private void preventServerCrashAll(Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call();
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(stackTraceElement -> {
                    sb.append(stackTraceElement.toString()).append("\n");
                });
                if (this != null && this.getPlayerManager() != null) {
                    this.getPlayerManager().broadcast(Text.literal("[CrashPrevented] " + e.getLocalizedMessage())
                            .formatted(Formatting.RED).styled(s -> s.withClickEvent(ClickEvent.copyToClipboard(sb.toString())).withHoverEvent(HoverEvent.showText(Text.literal("Copy stack trace")))), false);
                }
            }
        } else {
            original.call();
        }
    }

    @WrapMethod(method = "tickWorlds")
    private void preventServerCrashWorld(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call(shouldKeepTicking);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(stackTraceElement -> {
                    sb.append(stackTraceElement.toString()).append("\n");
                });
                if (this != null && this.getPlayerManager() != null) {
                    this.getPlayerManager().broadcast(Text.literal("[CrashPrevented] " + e.getLocalizedMessage())
                            .formatted(Formatting.RED).styled(s -> s.withClickEvent(ClickEvent.copyToClipboard(sb.toString())).withHoverEvent(HoverEvent.showText(Text.literal("Copy stack trace")))), false);
                }
            }
        } else {
            original.call(shouldKeepTicking);
        }
    }
}
