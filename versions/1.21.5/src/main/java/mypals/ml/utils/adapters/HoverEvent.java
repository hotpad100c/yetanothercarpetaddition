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

import net.minecraft.text.Text;

public class HoverEvent implements net.minecraft.text.HoverEvent {
    private final Action action;
    private final Object value;


    public HoverEvent(Action action, Object value) {
        this.action = action;
        this.value = value;
    }

    @Override
    public net.minecraft.text.HoverEvent.Action getAction() {
        switch (action) {
            case SHOW_TEXT:
                if (value instanceof Text text) {
                    return new net.minecraft.text.HoverEvent.ShowText(text).getAction();
                }break;
            case SHOW_ITEM:
                if (value instanceof net.minecraft.item.ItemStack itemStack) {
                    return new net.minecraft.text.HoverEvent.ShowItem(itemStack).getAction();
                }break;
            case SHOW_ENTITY:
                if (value instanceof EntityContent entity) {
                    return new net.minecraft.text.HoverEvent.ShowEntity(entity).getAction();
                }break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
        return null;
    }
    public static enum Action {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY
    }
}
