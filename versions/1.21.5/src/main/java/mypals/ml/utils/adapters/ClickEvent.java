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

package mypals.ml.utils.adapters;

import java.net.URI;

public class ClickEvent implements net.minecraft.text.ClickEvent {

    private final Action action;
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    @Override
    public net.minecraft.text.ClickEvent.Action getAction() {
        switch (action) {
            case OPEN_URL:return new net.minecraft.text.ClickEvent.OpenUrl(URI.create(value)).getAction();
            case OPEN_FILE:return new net.minecraft.text.ClickEvent.OpenFile(value).getAction();
            case RUN_COMMAND:return new net.minecraft.text.ClickEvent.RunCommand(value).getAction();
            case SUGGEST_COMMAND:return new net.minecraft.text.ClickEvent.SuggestCommand(value).getAction();
            case CHANGE_PAGE:return new net.minecraft.text.ClickEvent.ChangePage(Integer.parseInt(value)).getAction();
            case COPY_TO_CLIPBOARD:return new net.minecraft.text.ClickEvent.CopyToClipboard(value).getAction();
            default: throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    public static enum Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD;
    }
}
