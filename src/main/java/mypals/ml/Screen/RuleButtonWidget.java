package mypals.ml.Screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RuleButtonWidget extends ButtonWidget {
    protected RuleButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }
}
