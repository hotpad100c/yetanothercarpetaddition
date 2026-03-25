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

package mypals.ml.screen.rulesEditScreen;

import mypals.ml.utils.adapter.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import mypals.ml.network.RuleData;
import mypals.ml.settings.YACAConfigManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//#if MC >= 12109
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
//#endif

import static mypals.ml.YetAnotherCarpetAdditionClient.defaultRules;
import static mypals.ml.YetAnotherCarpetAdditionClient.favoriteRules;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;
// TODO something is missing here
// //#elseif MC >= 12102
// //$$ import static net.minecraft.client.render.RenderLayer.getGui;
// //#endif

//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#else
//$$import net.minecraft.client.renderer.RenderType;
//#endif

public class RuleWidget {
    private RuleData ruleData;
    private int x, y;
    public EditBox valueWidget;
    public StateSwitchingButton trueFalseButton;

    public StateSwitchingButton lockRule;
    public StateSwitchingButton favoriteRule;
    public boolean isTrueFalseRule = false;
    private RulesEditScreen rulesEditScreen;
    private WidgetSprites LOCK = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/lock.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/unlock.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/lock_s.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/unlock_s.png"));
    private WidgetSprites LOVE = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/loved.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/love.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/loved_s.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/love_s.png"));
    private WidgetSprites TRUE_FALSE = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/true_t.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/false_t.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/true_t_s.png"), ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/false_t_s.png"));

    protected RuleWidget(RuleData ruleData, RulesEditScreen rulesEditScreen) {
        this.rulesEditScreen = rulesEditScreen;
        this.ruleData = ruleData;
        valueWidget = new EditBox(Minecraft.getInstance().font, x + 30, y + 5, 100, 20, Component.nullToEmpty(ruleData.value)) {
            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                if (this.isVisible()) {
                    context.fill(
                            //#if MC >= 12106

                            //#else
                            //$$ RenderType.guiOverlay(),
                            //#endif

                            this.getX() + this.width / 2, this.getY() + this.height - 4,
                            this.getX(), this.getY() + this.height - 5,
                            //#if MC >= 12106
                            -1072689136
                            //#else
                            //$$ 0xAFFFFFFF
                            //#endif
                    );
                    super.renderWidget(context, mouseX, mouseY, delta);
                }
            }
        };
        valueWidget.setBordered(false);
        valueWidget.setEditable(true);
        valueWidget.active = true;
        valueWidget.setMaxLength(114514);
        valueWidget.setSize(60, 15);
        valueWidget.setSuggestion(ruleData.value);

        trueFalseButton = new StateSwitchingButton(x + 30, y + 5, 30, 13, ruleData.value.toLowerCase().equals("true")) {
            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.isHovered = this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());

                if (this.sprites != null) {
                    RenderSystem.disableDepthTest();
                    context.blit(
                            //#if MC >= 12106
                            RenderPipelines.GUI_TEXTURED,
                            //#elseif MC >= 12102
                            //$$ RenderType::guiTextured,
                            //#endif
                            this.sprites.get(this.isStateTriggered(), this.isMouseOver(mouseX, mouseY)), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean
            //#if MC > 12101
            mouseClicked
            //#else
            //$$ clicked
            //#endif
            (
                    //#if MC < 12109
                    //$$ double mouseX, double mouseY
                    //#if MC >= 12103
                    //$$ , int button
                    //#endif
                    //#else
                    MouseButtonEvent click, boolean doubled
                    //#endif
            ) {
                //#if MC >= 12109
                double mouseX = click.x();
                double mouseY = click.y();
                //#endif

                //#if MC >= 12103
                if(this.isMouseOver(mouseX, mouseY)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    //#if MC >= 12109
                    this.onClick(click, doubled);
                    //#else
                    //$$    this.onClick(mouseX, mouseY);
                    //#endif
                }
                //#endif
                return this.isMouseOver(mouseX, mouseY);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.isHovered = mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());

                return this.active && this.visible && this.isHovered;
            }


            @Override
            public void onClick(
                    //#if MC >= 12109
                    MouseButtonEvent click, boolean doubled
                    //#else
                    //$$ double mouseX, double mouseY
                    //#endif
            ) {
                this.isStateTriggered = !this.isStateTriggered;
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                String commandName = ruleData.name.split("```").length > 1 ? ruleData.name.split("```")[1] : ruleData.name.split("```")[0];
                Minecraft.getInstance().getConnection()
                        //#if MC >= 12106
                        .sendCommand(
                                //#else
                                //$$ .sendCommand(
                                //#endif
                                ("carpet " + commandName + " " + this.isStateTriggered));
                ruleData.value = this.isStateTriggered ? "true" : "false";
                valueWidget.setValue("");
            }
        };
        trueFalseButton.initTextureValues(TRUE_FALSE);

        isTrueFalseRule = ruleData.suggestions.size() == 2 &&
                ((ruleData.suggestions.getLast().toLowerCase().equals("true") &&
                        ruleData.suggestions.getFirst().toLowerCase().equals("false"))
                        ||
                        (ruleData.suggestions.getFirst().toLowerCase().equals("true")
                                && ruleData.suggestions.getLast().toLowerCase().equals("false")));

        String orgName = ruleData.name.split("```").length > 1 ? ruleData.name.split("```")[1] : ruleData.name.split("```")[0];
        lockRule = new StateSwitchingButton(x - 15, y + 3, 10, 11, defaultRules.contains(orgName)) {
            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                if (this.sprites != null) {
                    RenderSystem.disableDepthTest();
                    context.blit(
                            //#if MC >= 12106
                            RenderPipelines.GUI_TEXTURED,
                            //#elseif MC >= 12102
                            //$$ RenderType::guiTextured,
                            //#endif
                            this.sprites.get(this.isStateTriggered(), this.isMouseOver(mouseX, mouseY)), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean
                //#if MC > 12101
            mouseClicked
            //#else
            //$$ clicked
            //#endif
            (
                    //#if MC < 12109
                    //$$ double mouseX, double mouseY
                    //#if MC >= 12103
                    //$$ , int button
                    //#endif
                    //#else
                    MouseButtonEvent click, boolean doubled
                    //#endif
            ) {
                //#if MC >= 12109
                double mouseX = click.x();
                double mouseY = click.y();
                //#endif

                //#if MC >= 12103
                if(this.isMouseOver(mouseX, mouseY)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    //#if MC >= 12109
                    this.onClick(click, doubled);
                    //#else
                    //$$    this.onClick(mouseX, mouseY);
                    //#endif
                }
                //#endif
                return this.isMouseOver(mouseX, mouseY);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.isHovered = mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());

                return this.active && this.visible && this.isHovered;
            }


            @Override
            public void onClick(
                    //#if MC >= 12109
                    MouseButtonEvent click, boolean doubled
                    //#else
                    //$$ double mouseX, double mouseY
                    //#endif
            ) {
                this.isStateTriggered = !this.isStateTriggered;
                System.out.println("Clicked lock button");
                String commandName = ruleData.name.split("```").length > 1 ? ruleData.name.split("```")[1] : ruleData.name.split("```")[0];
                Minecraft.getInstance().getConnection()

                        //#if MC >= 12106
                        .sendCommand(
                                //#else
                                //$$ .sendCommand(
                                //#endif
                                (isStateTriggered ? "carpet setDefault " : "carpet removeDefault ") + commandName + (isStateTriggered ? " " + ruleData.value : "")
                        );

                if (isStateTriggered) {
                    defaultRules.add(commandName);
                } else {
                    defaultRules.remove(commandName);
                }
                if (Objects.equals(rulesEditScreen.currentCategory, "default"))
                    rulesEditScreen.setCurrentCategory("default");
            }
        };

        lockRule.initTextureValues(LOCK);

        favoriteRule = new StateSwitchingButton(x - 15, y - 3, 10, 11, favoriteRules.contains(orgName)) {
            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.isHovered = this.active && this.visible && mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());
                if (this.sprites != null) {
                    RenderSystem.disableDepthTest();
                    context.blit(
                            //#if MC >= 12106
                            RenderPipelines.GUI_TEXTURED,
                            //#elseif MC >= 12102
                            //$$ RenderType::guiTextured,
                            //#endif
                            this.sprites.get(this.isStateTriggered(), this.isMouseOver(mouseX, mouseY)), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    RenderSystem.enableDepthTest();
                }
            }

            @Override
            public boolean
                //#if MC>12101
            mouseClicked
            //#else
            //$$ clicked
            //#endif
            (
                    //#if MC < 12109
                    //$$ double mouseX, double mouseY
                    //#if MC >= 12103
                    //$$ , int button
                    //#endif
                    //#else
                    MouseButtonEvent click, boolean doubled
                    //#endif
            ) {
                //#if MC >= 12109
                double mouseX = click.x();
                double mouseY = click.y();
                //#endif

                //#if MC >= 12103
                if(this.isMouseOver(mouseX, mouseY)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    //#if MC >= 12109
                    this.onClick(click, doubled);
                    //#else
                    //$$    this.onClick(mouseX, mouseY);
                    //#endif
                }
                //#endif
                return this.isMouseOver(mouseX, mouseY);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                double adjustedMouseY = mouseY + rulesEditScreen.rulesScrollableWidget.getScrollY();
                this.isHovered = mouseX >= (double) this.getX()
                        && adjustedMouseY >= (double) this.getY() &&
                        mouseX < (double) (this.getX() + this.getWidth()) &&
                        adjustedMouseY < (double) (this.getY() + this.getHeight());

                return this.active && this.visible && this.isHovered;
            }


            @Override
            public void onClick(
                    //#if MC >= 12109
                    MouseButtonEvent click, boolean doubled
                    //#else
                    //$$ double mouseX, double mouseY
                    //#endif
            ) {
                this.isStateTriggered = !this.isStateTriggered;

                String orgName = ruleData.name.split("```").length > 1 ? ruleData.name.split("```")[1] : ruleData.name.split("```")[0];

                if (isStateTriggered) {
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

        favoriteRule.initTextureValues(LOVE);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected List<Component> renderContents(GuiGraphics context, int mouseX, int mouseY, float delta, boolean isMouseOver, int index, int spacing, int boxHeight, int boxWidth) {

        context.fill(x, y, x + boxWidth, y + boxHeight, 0x50060606);

        context.fill(x, y+boxHeight-1, x + boxWidth,  y+boxHeight,  isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        StringBuilder categories = new StringBuilder();
        for (String c : ruleData.categories) {
            categories.append(c).append(" | ");
        }
        String name = ruleData.name.split("```")[0];

        lockRule.setPosition(boxWidth - 15, y + 4);
        lockRule.render(context, mouseX, mouseY, delta);


        favoriteRule.setPosition(boxWidth - 6, y + 5);
        favoriteRule.render(context, mouseX, mouseY, delta);
        context.drawString(Minecraft.getInstance().font, name + " : ", x + 5, y + 5, 0xFFFFFFFF, true);
        if (isTrueFalseRule) {
            trueFalseButton.setPosition(boxWidth - 50, y + 2);
            trueFalseButton.render(context, mouseX, mouseY, delta);
        } else {
            valueWidget.setPosition(boxWidth - 50, y + 5);
            valueWidget.render(context, mouseX, mouseY, delta);
        }
        context.drawString(Minecraft.getInstance().font, categories.toString(), x + 5, y + boxHeight - 12, 0xFFFFFFFF, true);
        //context.drawTexture(LOCK.get(true, true), x + 5, y + 5, 0, 0, 10, 10, 10, 10);

        if (isMouseOver && mouseX <= x + boxWidth / 2) {
            List<Component> toolTips = new ArrayList<>();

            toolTips.add(Component.nullToEmpty(name));
            toolTips.add(Component.nullToEmpty(ruleData.description));
            toolTips.add(Component.nullToEmpty(Component.translatable("gui.screen.tooltip.defaultValue").getString() + ": " + ruleData.defaultValue));
            toolTips.add(Component.nullToEmpty(Component.translatable("gui.screen.tooltip.currentValue").getString() + ": " + ruleData.value));
            toolTips.add(Component.nullToEmpty(Component.translatable("gui.screen.tooltip.suggestions").getString() + ":"));
            for (String c : ruleData.suggestions) {
                toolTips.add(Component.nullToEmpty("  " + c));
            }
            return toolTips;
        }
        return null;
    }

    public void onClicked(double mouseX, double mouseY, boolean clicked, int button) {
        //#if MC >= 12109
        MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
        //#endif
        if (!lockRule.mouseClicked(
                //#if MC >= 12109
                click, false
                //#else
                //$$ mouseX, mouseY, button
                //#endif
        ) && !favoriteRule.mouseClicked(
                //#if MC >= 12109
                click, false
                //#else
                //$$ mouseX, mouseY, button
                //#endif
        )) {
            valueWidget.setFocused(clicked && !isTrueFalseRule);
            valueWidget.setSuggestion(clicked || !valueWidget.getValue().isEmpty() ? "" : ruleData.value);
            if (clicked && isTrueFalseRule) {
                trueFalseButton.onClick(
                        //#if MC >= 12109
                        click, false
                        //#else
                        //$$ mouseX, mouseY
                        //#endif
                );
                System.out.println("Clicked toggle button");
            } else if (clicked && !isTrueFalseRule) {
                valueWidget.onClick(
                        //#if MC >= 12109
                        click, false
                        //#else
                        //$$ mouseX, mouseY
                        //#endif
                );
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            } else {
                if (!valueWidget.getValue().isEmpty() && !isTrueFalseRule) {
                    String commandName = ruleData.name.split("```").length > 1 ? ruleData.name.split("```")[1] : ruleData.name.split("```")[0];
                    Minecraft.getInstance().getConnection()
                            //#if MC >= 12106
                            .sendCommand(
                                    //#else
                                    //$$ .sendCommand(
                                    //#endif
                                    ("carpet " + commandName + " " + valueWidget.getValue()));
                    ruleData.value = valueWidget.getValue();
                    valueWidget.setValue("");
                }
            }
        }
    }
}
