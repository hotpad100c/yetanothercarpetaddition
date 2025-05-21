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
