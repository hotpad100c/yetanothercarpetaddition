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
