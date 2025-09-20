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

package mypals.ml.features.updateAnylizer;

import mypals.ml.YetAnotherCarpetAdditionServer;


import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler;

public class UpdateLoggerHelper {
    public static int NCUpdateCounter = 0;
    public static int PPUpdateCounter = 0;
    public static boolean updating = false;
    private static long startTime = 0;
    private static Throwable stackTraceCapture = null;

    public static void setUpdating(boolean bl) {
        updating = bl;
    }

    public static void incrementNC() {
        if (updating) {
            NCUpdateCounter++;
        }
    }

    public static void incrementPP() {
        if (updating) {
            PPUpdateCounter++;
        }
    }

    public static void start() {
        setUpdating(true);
        NCUpdateCounter = 0;
        PPUpdateCounter = 0;
        startTime = System.nanoTime();
        stackTraceCapture = new Throwable("Triggered here");
    }

    public static void reset() {
        NCUpdateCounter = 0;
        PPUpdateCounter = 0;
        setUpdating(false);
        startTime = 0;
        stackTraceCapture = null;
    }

    public static boolean isUpdating() {
        return updating;
    }

    public static void finish() {
        if (!updating) return;

        long elapsed = System.nanoTime() - startTime;
        boolean hasPrev = false;

        Text ncText = Text.empty();
        Text ppText = Text.empty();

        if (NCUpdateCounter > 0) {
            ncText = Text.literal(NCUpdateCounter + " x NC").setStyle(Style.EMPTY.withColor(0xFF5555));
            hasPrev = true;
        }
        if (PPUpdateCounter > 0) {
            ppText = Text.literal(hasPrev ? " & " : "").append(Text.literal(PPUpdateCounter + " x PP")
                    .setStyle(Style.EMPTY.withColor(0x5555FF)));
            hasPrev = true;
        }
        double ms = elapsed / 1_000_000.0;
        Text body = Text.literal(hasPrev ? "" : "no")
                .append(" updates in ").append(String.valueOf(ms)).append("ms");

        StringBuilder sb = new StringBuilder();
        if (stackTraceCapture != null) {
            for (StackTraceElement elem : stackTraceCapture.getStackTrace()) {
                sb.append(elem.toString()).append("\n");
            }
        }

        Text hoverText = Text.literal("[@]").styled(style ->
                style.withHoverEvent(HoverEvent.showText(Text.literal(sb.toString())))
                        .withColor(0xAAAAAA)
                        .withUnderline(true)
                        .withClickEvent(ClickEvent.copyToClipboard(sb.toString())));

        Text fullMessage = Text.literal("[UpdateCounter]")
                .append(ncText)
                .append(ppText)
                .append(body)
                .append(" ")
                .append(hoverText);

        MinecraftServer server = YetAnotherCarpetAdditionServer.serverWorld.getServer();

        server.getPlayerManager().broadcast(fullMessage, false);

        reset();
    }

}
