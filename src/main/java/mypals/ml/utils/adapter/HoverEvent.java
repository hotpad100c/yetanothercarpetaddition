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

import net.minecraft.text.Text;

public class HoverEvent {
    public static net.minecraft.text.HoverEvent showText(Text text) {
        //#if MC < 12105
        return new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, text);
        //#else
        //$$ return new net.minecraft.text.HoverEvent.ShowText(text);
        //#endif
    }

    public static net.minecraft.text.HoverEvent showEntity(net.minecraft.text.HoverEvent.EntityContent entityContent) {
        //#if MC < 12105
        return new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_ENTITY, entityContent);
        //#else
        //$$ return new net.minecraft.text.HoverEvent.ShowEntity(entityContent);
        //#endif
    }

    public static net.minecraft.text.HoverEvent showItem(
            //#if MC < 12105
            net.minecraft.text.HoverEvent.ItemStackContent itemContent
            //#else
            //$$ net.minecraft.item.ItemStack itemContent
            //#endif
    ) {
        //#if MC < 12105
        return new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_ITEM, itemContent);
        //#else
        //$$ return new net.minecraft.text.HoverEvent.ShowItem(itemContent);
        //#endif
    }
}
