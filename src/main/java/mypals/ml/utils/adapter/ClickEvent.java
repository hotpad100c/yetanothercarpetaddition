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

package mypals.ml.utils.adapter;
//#if MC >= 12105
//$$ import java.net.URISyntaxException;
//$$ import java.net.URI;
//#endif

public class ClickEvent {
    public static net.minecraft.text.ClickEvent runCommand(String value) {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, value);
        //#else
        //$$ return new net.minecraft.text.ClickEvent.RunCommand(value);
        //#endif
    }

    public static net.minecraft.text.ClickEvent suggestCommand(String value) {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, value);
        //#else
        //$$ return new net.minecraft.text.ClickEvent.SuggestCommand(value);
        //#endif
    }

    public static net.minecraft.text.ClickEvent openURL(String value)
            //#if MC >= 12105
            //$$ throws URISyntaxException
            //#endif
    {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.OPEN_URL, value);
        //#else
        //$$ return new net.minecraft.text.ClickEvent.OpenUrl(new URI(value));
        //#endif
    }

    public static net.minecraft.text.ClickEvent openFile(String value) {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.OPEN_FILE, value);
        //#else
        //$$ return new net.minecraft.text.ClickEvent.OpenFile(value);
        //#endif
    }

    public static net.minecraft.text.ClickEvent changePage(int value) {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.CHANGE_PAGE, String.valueOf(value));
        //#else
        //$$ return new net.minecraft.text.ClickEvent.ChangePage(value);
        //#endif
    }

    public static net.minecraft.text.ClickEvent copyToClipboard(String value) {
        //#if MC < 12105
        return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.COPY_TO_CLIPBOARD, value);
        //#else
        //$$ return new net.minecraft.text.ClickEvent.CopyToClipboard(value);
        //#endif
    }
}
