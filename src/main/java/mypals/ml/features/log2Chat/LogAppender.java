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

package mypals.ml.features.log2Chat;

import carpet.CarpetServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import net.minecraft.text.Text;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;

public class LogAppender extends AbstractAppender {

    public LogAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        if (CarpetServer.minecraft_server != null && YetAnotherCarpetAdditionRules.logInfoToChat) {
            String context = event.getContextStack().isEmpty() ? "No context" : event.getContextStack().peek(); // 使用 peek 避免破坏堆栈
            String marker = event.getMarker() != null ? event.getMarker().toString() : "None";
            String fqcn = event.getLoggerFqcn() != null ? event.getLoggerFqcn() : "Unknown";
            String message = event.getMessage().getFormattedMessage();
            if (message.contains("<L2C>")) return;
            for (ServerPlayerEntity player : CarpetServer.minecraft_server.getPlayerManager().getPlayerList()) {
                Formatting color = event.getLevel() == Level.TRACE ? Formatting.GOLD :
                        event.getLevel() == Level.DEBUG ? Formatting.LIGHT_PURPLE :
                                event.getLevel() == Level.ERROR ? Formatting.RED :
                                        event.getLevel() == Level.WARN ? Formatting.YELLOW :
                                                Formatting.GRAY;
                player.sendMessage(
                        Text.literal("").append(Text.literal("<L2C>").formatted(Formatting.ITALIC).formatted(Formatting.GRAY))
                                .append(Text.literal(message).formatted(color))

                                .styled(style -> style
                                        .withClickEvent(mypals.ml.utils.adapter.ClickEvent.copyToClipboard(context))
                                        .withHoverEvent(HoverEvent.showText(
                                                Text.literal("")
                                                        .append(Text.literal("Logger: " + event.getLoggerName() + "\n").formatted(Formatting.GOLD))
                                                        .append(Text.literal("Level: " + event.getLevel() + "\n").formatted(color))
                                                        .append("Marker: " + marker + "\n")
                                                        .append("Thread: " + event.getThreadName() + "\n")
                                                        .append("Fqcn: " + fqcn + "\n")
                                        ))
                                ),
                        false
                );
            }
        }

        /*if (CarpetServer.minecraft_server != null ) {
            CarpetServer.minecraft_server.sendMessage(
                    Text.literal(event.getThreadName() + " " + message)
                            .styled(style -> style.withClickEvent(mypals.ml.utils.adapter.ClickEvent.copyToClipboard(
                                    event.getContextStack().pop())).withHoverEvent(HoverEvent.showText(
                                    Text.literal("Logger: " + event.getLoggerName() + "\n").formatted(Formatting.GOLD)
                                            .append("Level: " + event.getLevel() + "\n")
                                            .append("Marker: " + event.getMarker() + "\n")
                                            .append("Thread: " + event.getThreadName() + "\n")
                                            .append("Fqcn: " + event.getLoggerFqcn() + "\n")

                            )))
            );
        }*/
    }
}
