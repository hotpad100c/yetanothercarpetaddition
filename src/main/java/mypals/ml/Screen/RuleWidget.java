package mypals.ml.Screen;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.network.RuleData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mypals.ml.YetAnotherCarpetAdditionClient.defaultRules;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class RuleWidget {
    private RuleData ruleData;
    private int x, y;
    public TextFieldWidget valueWidget;
    public ToggleButtonWidget lockRule;
    private ButtonTextures LOCK = new ButtonTextures(Identifier.of(MOD_ID, "ui/lock.png"), Identifier.of(MOD_ID, "ui/unlock.png"), Identifier.of(MOD_ID, "ui/lock_s.png"), Identifier.of(MOD_ID, "ui/unlock_s.png"));

    protected RuleWidget(RuleData ruleData) {
        this.ruleData = ruleData;
        valueWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 30, y + 5, 100, 20, Text.of(ruleData.value));
        valueWidget.setEditable(true);
        valueWidget.active = true;
        valueWidget.setMaxLength(114514);
        valueWidget.setDimensions(60, 15);
        valueWidget.setSuggestion(ruleData.suggestions.getFirst());

        String orgName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
        lockRule = new ToggleButtonWidget(x - 15, y + 3, 10, 10, defaultRules.contains(orgName)) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                if (this.textures != null) {
                    RenderSystem.disableDepthTest();
                    context.drawTexture(this.textures.get(this.isToggled(), this.isSelected()), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }
        };

        lockRule.setTextures(LOCK);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected List<Text> renderContents(DrawContext context, int mouseX, int mouseY, float delta, boolean isMouseOver, int index, int spacing, int boxHeight, int boxWidth) {

        context.fill(x, y, x + boxWidth, y + boxHeight, 0x19E0E0E0);

        int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
        context.drawBorder(x, y, boxWidth, boxHeight, borderColor);
        StringBuilder categories = new StringBuilder();
        for (String c : ruleData.categories) {
            categories.append(c).append(" | ");
        }
        String name = ruleData.name.split("\\|")[0];
        valueWidget.setPosition(boxWidth - 300, y + 15);
        valueWidget.render(context, mouseX, mouseY, delta);
        lockRule.setPosition(boxWidth - 15, y + 3);
        //lockRule.setPosition(100, 100);
        lockRule.render(context, mouseX, mouseY, delta);
        context.drawText(MinecraftClient.getInstance().textRenderer, name + " : " + ruleData.value, x + 5, y + 5, 0xFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, categories.toString(), x + 5, y + 35, 0xFFFFFF, false);
        //context.drawTexture(LOCK.get(true, true), x + 5, y + 5, 0, 0, 10, 10, 10, 10);

        if (isMouseOver) {
            List<Text> toolTips = new ArrayList<>();

            toolTips.add(Text.of(name));
            toolTips.add(Text.of(ruleData.description));
            toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.defaultValue").getString() + ": " + ruleData.defaultValue));
            toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.currentValue").getString() + ": " + ruleData.value));
            toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.suggestions").getString() + ":"));
            for (String c : ruleData.suggestions) {
                toolTips.add(Text.of("  " + c));
            }
            return toolTips;
        }
        return null;
    }

    public void onClicked(double mouseX, double mouseY, boolean clicked, int button) {
        if (!lockRule.mouseClicked(mouseX, mouseY, button)) {
            valueWidget.setFocused(clicked);
            valueWidget.setSuggestion(clicked || !valueWidget.getText().isEmpty() ? "" : ruleData.suggestions.getFirst());
            if (clicked) {
                valueWidget.onClick(mouseX, mouseY);
            } else {
                if (!valueWidget.getText().isEmpty()) {
                    String commandName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
                    MinecraftClient.getInstance().getNetworkHandler().sendCommand(("carpet " + commandName + " " + valueWidget.getText()));
                    ruleData.value = valueWidget.getText();
                    valueWidget.setText("");
                }
            }
        }
    }
}
