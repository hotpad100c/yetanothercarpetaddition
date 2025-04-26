package mypals.ml.Screen;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.network.RuleData;
import mypals.ml.settings.YACAConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static mypals.ml.YetAnotherCarpetAdditionClient.defaultRules;
import static mypals.ml.YetAnotherCarpetAdditionClient.favoriteRules;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class RuleWidget {
    private RuleData ruleData;
    private int x, y;
    public TextFieldWidget valueWidget;
    public ToggleButtonWidget trueFalseButton;

    public ToggleButtonWidget lockRule;
    public ToggleButtonWidget favoriteRule;
    public boolean isTrueFalseRule = false;
    private RulesEditScreen rulesEditScreen;
    private ButtonTextures LOCK = new ButtonTextures(Identifier.of(MOD_ID, "ui/lock.png"), Identifier.of(MOD_ID, "ui/unlock.png"), Identifier.of(MOD_ID, "ui/lock_s.png"), Identifier.of(MOD_ID, "ui/unlock_s.png"));
    private ButtonTextures LOVE = new ButtonTextures(Identifier.of(MOD_ID, "ui/loved.png"), Identifier.of(MOD_ID, "ui/love.png"), Identifier.of(MOD_ID, "ui/loved_s.png"), Identifier.of(MOD_ID, "ui/love_s.png"));
    private ButtonTextures TRUE_FALSE = new ButtonTextures(Identifier.of(MOD_ID, "ui/true_t.png"), Identifier.of(MOD_ID, "ui/false_t.png"), Identifier.of(MOD_ID, "ui/true_t_s.png"), Identifier.of(MOD_ID, "ui/false_t_s.png"));

    protected RuleWidget(RuleData ruleData, RulesEditScreen rulesEditScreen) {
        this.rulesEditScreen = rulesEditScreen;
        this.ruleData = ruleData;
        valueWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 30, y + 5, 100, 20, Text.of(ruleData.value)) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                if (this.isVisible()) {
                    context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY() + this.height - 4,
                            this.width + 1, this.getY() + this.height - 5, 0xAFFFFFFF);
                    super.renderWidget(context, mouseX, mouseY, delta);
                }
            }
        };
        valueWidget.setDrawsBackground(false);
        valueWidget.setEditable(true);
        valueWidget.active = true;
        valueWidget.setMaxLength(114514);
        valueWidget.setDimensions(60, 15);
        valueWidget.setSuggestion(ruleData.suggestions.getFirst());

        trueFalseButton = new ToggleButtonWidget(x + 30, y + 5, 30, 12, ruleData.value.toLowerCase().equals("true")) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.hovered = this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());

                if (this.textures != null) {
                    RenderSystem.disableDepthTest();
                    context.drawTexture(this.textures.get(this.isToggled(), this.isSelected()), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean clicked(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                return this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
            }


            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggled = !this.toggled;
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                String commandName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
                MinecraftClient.getInstance().getNetworkHandler().sendCommand(("carpet " + commandName + " " + this.toggled));
                ruleData.value = valueWidget.getText();
                valueWidget.setText("");
            }
        };
        trueFalseButton.setTextures(TRUE_FALSE);

        isTrueFalseRule = ruleData.suggestions.size() == 2 &&
                ((ruleData.suggestions.getLast().toLowerCase().equals("true") &&
                        ruleData.suggestions.getFirst().toLowerCase().equals("false"))
                        ||
                        (ruleData.suggestions.getFirst().toLowerCase().equals("true")
                                && ruleData.suggestions.getLast().toLowerCase().equals("false")));

        String orgName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
        lockRule = new ToggleButtonWidget(x - 15, y + 3, 10, 10, defaultRules.contains(orgName)) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.hovered = this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
                if (this.textures != null) {
                    RenderSystem.disableDepthTest();
                    context.drawTexture(this.textures.get(this.isToggled(), this.isSelected()), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean clicked(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                return this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
            }


            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggled = !this.toggled;

                String commandName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
                MinecraftClient.getInstance().getNetworkHandler().sendCommand((toggled ? "carpet setDefault " : "carpet removeDefault ") + commandName + (toggled ? " " + ruleData.value : ""));

                if (toggled) {
                    defaultRules.add(commandName);
                } else {
                    defaultRules.remove(commandName);
                }
                if (Objects.equals(rulesEditScreen.currentCategory, "default"))
                    rulesEditScreen.setCurrentCategory("default");
            }
        };

        lockRule.setTextures(LOCK);

        favoriteRule = new ToggleButtonWidget(x - 15, y - 3, 10, 10, favoriteRules.contains(orgName)) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.hovered = this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
                if (this.textures != null) {
                    RenderSystem.disableDepthTest();
                    context.drawTexture(this.textures.get(this.isToggled(), this.isSelected()), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean clicked(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                return this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
            }


            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggled = !this.toggled;

                String orgName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];

                if (toggled) {
                    YACAConfigManager.addFavoriteRule(orgName);
                    favoriteRules.add(orgName);
                } else {
                    YACAConfigManager.removeFavoriteRule(orgName);
                    favoriteRules.remove(orgName);
                }
                if (Objects.equals(rulesEditScreen.currentCategory, "favorite"))
                    rulesEditScreen.setCurrentCategory("favorite");
            }
        };

        favoriteRule.setTextures(LOVE);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected List<Text> renderContents(DrawContext context, int mouseX, int mouseY, float delta, boolean isMouseOver, int index, int spacing, int boxHeight, int boxWidth) {

        context.fill(x, y, x + boxWidth, y + boxHeight, 0x50060606);

        int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
        context.drawBorder(x, y, boxWidth, boxHeight, borderColor);
        StringBuilder categories = new StringBuilder();
        for (String c : ruleData.categories) {
            categories.append(c).append(" | ");
        }
        String name = ruleData.name.split("\\|")[0];
        if (isTrueFalseRule) {
            trueFalseButton.setPosition(x + 5, y + 17);
            trueFalseButton.render(context, mouseX, mouseY, delta);
        } else {
            valueWidget.setPosition(x + 5, y + 17);
            valueWidget.render(context, mouseX, mouseY, delta);
        }

        lockRule.setPosition(boxWidth - 15, y + 3);
        //lockRule.setPosition(100, 100);
        lockRule.render(context, mouseX, mouseY, delta);


        favoriteRule.setPosition(boxWidth - 6, y + 4);
        //lockRule.setPosition(100, 100);
        favoriteRule.render(context, mouseX, mouseY, delta);
        context.drawText(MinecraftClient.getInstance().textRenderer, name + " : " + ruleData.value, x + 5, y + 5, 0xFFFFFF, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, categories.toString(), x + 5, y + 35, 0xFFFFFF, true);
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
        if (!lockRule.mouseClicked(mouseX, mouseY, button) && !favoriteRule.mouseClicked(mouseX, mouseY, button)) {
            valueWidget.setFocused(clicked && !isTrueFalseRule);
            valueWidget.setSuggestion(clicked || !valueWidget.getText().isEmpty() ? "" : ruleData.suggestions.getFirst());
            if (clicked && isTrueFalseRule) {
                trueFalseButton.onClick(mouseX, mouseY);
            } else if (clicked && !isTrueFalseRule) {
                valueWidget.onClick(mouseX, mouseY);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            } else {
                if (!valueWidget.getText().isEmpty() && !isTrueFalseRule) {
                    String commandName = ruleData.name.split("\\|").length > 1 ? ruleData.name.split("\\|")[1] : ruleData.name.split("\\|")[0];
                    MinecraftClient.getInstance().getNetworkHandler().sendCommand(("carpet " + commandName + " " + valueWidget.getText()));
                    ruleData.value = valueWidget.getText();
                    valueWidget.setText("");
                }
            }
        }
    }
}
