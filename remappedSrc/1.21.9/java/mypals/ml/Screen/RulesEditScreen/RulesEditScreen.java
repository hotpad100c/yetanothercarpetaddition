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

package mypals.ml.Screen.RulesEditScreen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static mypals.ml.YetAnotherCarpetAdditionClient.*;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class RulesEditScreen extends Screen implements ContainerEventHandler {
    private static final Component CONFIGURE_TEXT = Component.translatable("gui.screen.configure");
    private static final ResourceLocation CONFIGURE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/configure.png");
    public String currentCategory = "unknown";
    public String lastCategoryBeforeSearching = currentCategory;
    private static CopyOnWriteArrayList<RuleWidget> rulesInCurrentCategory = new CopyOnWriteArrayList<>();
    private static List<CategoryEntry> categoriesInScreen = new ArrayList<>();
    public ConstantScrollableWidget rulesScrollableWidget;
    public ConstantScrollableWidget categoriesScrollableWidget;
    public List<Component> currentToolTips = new ArrayList<>();
    public EditBox searchFieldWidget;
    public boolean searching = false;
    private static Pattern searchRulePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    public RulesEditScreen(Component title) {
        super(title);
    }

    //Im sorry.

    //#if MC >= 12109
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
       return super.mouseDragged(click,deltaX,deltaY) || this.rulesScrollableWidget.mouseDragged(click,deltaX,deltaY) || this.categoriesScrollableWidget.mouseDragged(click,deltaX,deltaY);
    }
    @Override
    public boolean keyPressed(KeyEvent keyInput) {
       return super.keyPressed(keyInput) || this.rulesScrollableWidget.keyPressed(keyInput) || this.categoriesScrollableWidget.keyPressed(keyInput) || searchFieldWidget.keyPressed(keyInput);
    }
    @Override
    public boolean charTyped(CharacterEvent charInput){
       return super.charTyped(charInput) || this.rulesScrollableWidget.charTyped(charInput) || this.categoriesScrollableWidget.charTyped(charInput) || searchFieldWidget.charTyped(charInput);
    }
    //#elseif MC >= 12106
    //$$ public boolean mouseDragged(double x,double y,int click, double deltaX, double deltaY) {
    //$$    return super.mouseDragged(x,y,click,deltaX,deltaY) || this.rulesScrollableWidget.mouseDragged(x,y,click,deltaX,deltaY) || this.categoriesScrollableWidget.mouseDragged(x,y,click,deltaX,deltaY);
    //$$ }
    //$$ @Override
    //$$ public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    //$$    return super.keyPressed(keyCode,scanCode,modifiers) || this.rulesScrollableWidget.keyPressed(keyCode,scanCode,modifiers) || this.categoriesScrollableWidget.keyPressed(keyCode,scanCode,modifiers) || searchFieldWidget.keyPressed(keyCode,scanCode,modifiers);
    //$$ }
    //$$ @Override
    //$$ public boolean charTyped(char chr, int modifiers){
    //$$    return super.charTyped(chr,modifiers) || this.rulesScrollableWidget.charTyped(chr,modifiers) || this.categoriesScrollableWidget.charTyped(chr,modifiers) || searchFieldWidget.charTyped(chr,modifiers);
    //$$ }
    //#endif

    public void setCurrentCategory(String category) {
        this.currentCategory = category;
        if (!Objects.equals(currentCategory, "searching")) {
            lastCategoryBeforeSearching = category;
        }

        categoriesInScreen.clear();
        chachedCategories.forEach(c -> categoriesInScreen.add(new CategoryEntry(c)));
        rulesInCurrentCategory.clear();
        chachedRules.stream().filter(r -> r.categories.contains(currentCategory))
                .sorted(Comparator.comparing(
                        rule -> {
                            String englishName = rule.name.split("```", 2)[0].trim();
                            return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                        }
                )).toList().forEach(r -> {
                    rulesInCurrentCategory.add(new RuleWidget(r, this));
                });
        if (Objects.equals(currentCategory, "default")) {
            rulesInCurrentCategory.clear();

            chachedRules.stream().filter(r -> {
                String orgName = r.name.split("```").length > 1 ? r.name.split("```")[1] : r.name.split("```")[0];
                return defaultRules.contains(orgName);
            }).sorted(Comparator.comparing(
                    rule -> {
                        String englishName = rule.name.split("```", 2)[0].trim();
                        return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                    }
            )).toList().forEach(r -> {
                rulesInCurrentCategory.add(new RuleWidget(r, this));
            });
        }
        if (Objects.equals(currentCategory, "favorite")) {
            rulesInCurrentCategory.clear();

            chachedRules.stream().filter(r -> {
                        String orgName = r.name.split("```").length > 1 ? r.name.split("```")[1] : r.name.split("```")[0];
                        return favoriteRules.contains(orgName);
                    }).sorted(Comparator.comparing(
                            rule -> {
                                String englishName = rule.name.split("```", 2)[0].trim();
                                return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                            }
                    ))
                    .toList().forEach(r -> {
                        rulesInCurrentCategory.add(new RuleWidget(r, this));
                    });
        }
        if (!(rulesScrollableWidget == null)) {
            this.rulesScrollableWidget.setScrollY(0);
        }

    }

    public static List<String> splitRuleName(String ruleName) {
        List<String> parts = new ArrayList<>();
        if (ruleName == null || ruleName.isEmpty()) {
            return parts;
        }

        String[] sections = ruleName.split("```", 2);
        String englishPart = sections[0].trim();
        String otherLangPart = sections.length > 1 ? sections[1].trim() : "";

        if (!englishPart.isEmpty()) {

            String[] words = searchRulePattern.split(englishPart);
            for (String word : words) {
                if (!word.isEmpty()) {
                    parts.add(word.toLowerCase());
                }
            }
        }

        if (!otherLangPart.isEmpty()) {
            parts.add(otherLangPart);
        }

        return parts;
    }

    public static boolean matchesRule(List<String> parts, String input) {
        if (input == null || input.isEmpty() || parts == null || parts.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();
        for (String part : parts) {
            String lowerPart = part.toLowerCase();
            if (lowerPart.equals(lowerInput) || lowerPart.contains(lowerInput)) {
                return true;
            }
            if (part.contains(input)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void init() {
        setCurrentCategory(chachedCategories.get(2));

        this.addRenderableWidget(

                searchFieldWidget =
                        new EditBox(Minecraft.getInstance().font,
                                15, 10, this.width - (this.width / 3) - 7, 15, CONFIGURE_TEXT) {
                            @Override
                            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                                if (this.isVisible()) {
                                    context.fill(
                                            //#if MC < 12106
                                            //$$ RenderLayer.getGuiOverlay(),
                                            //#endif
                                            this.getX(), this.getY() - 1,
                                            this.width + 1, this.height + 1 + 9, 0x0AAAAAAA);

                                    context.fill(
                                            //#if MC < 12106
                                            //$$ RenderLayer.getGuiOverlay(),
                                            //#endif
                                            this.getX(), this.getY() + this.height - 4,
                                            this.width + 1, this.getY() + this.height - 5, 0xAFFFFFFF);
                                    super.renderWidget(context, mouseX, mouseY, delta);
                                }
                            }
                        });
        searchFieldWidget.setBordered(false);
        searchFieldWidget.setResponder(newText -> {
            if (newText.isEmpty()) {
                searching = false;
                setCurrentCategory(lastCategoryBeforeSearching);
            } else {
                searching = true;
                setCurrentCategory("searching");
                rulesInCurrentCategory.clear();

                chachedRules.stream().filter(r -> {
                            List<String> splitRuleName = splitRuleName(r.name);
                            splitRuleName.addAll(r.categories);
                            return matchesRule(splitRuleName, newText);
                        }).sorted(Comparator.comparing(
                                rule -> {
                                    String englishName = rule.name.split("```", 2)[0].trim();
                                    return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                                }
                        ))
                        .toList().forEach(r -> {
                            rulesInCurrentCategory.add(new RuleWidget(r, this));
                        });
            }
        });
        searchFieldWidget.setMaxLength(100);


        this.addRenderableWidget(
                rulesScrollableWidget = new

                        ConstantScrollableWidget
                                (0, 30, this.width - (this.width / 3), this.height - 30, CommonComponents.EMPTY) {
                            int boxWidth = this.width - 10;
                            int boxHeight = 30;
                            int spacing = 5;

                            @Override
                            protected int
                            //if MC < 12104
                            getContentsHeight
                            //else
                            //getContentsHeightWithPadding
                            //endif
                            () {
                                return (boxHeight + spacing) * rulesInCurrentCategory.size() - spacing;
                            }

                            @Override
                            protected double getDeltaYPerScroll() {
                                return 10f;
                            }

                            @Override
                            protected void
                            //if MC < 12104
                            renderContents
                            //else
                            //renderWidget
                            //endif
                            (GuiGraphics context, int mouseX, int mouseY, float delta) {


                                int index = 0;
                                double adjustedMouseY = mouseY + this.getScrollY();

                                currentToolTips = new ArrayList<>();
                                for (RuleWidget entry : rulesInCurrentCategory) {
                                    int x = 5;
                                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                                    entry.setPosition(x, y);
                                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y &&
                                            adjustedMouseY <= y + boxHeight && mouseY >= this.getY();

                                    List<Component> tooltips = entry.renderContents(context, mouseX, mouseY, delta, isMouseOver,
                                            index, spacing, boxHeight, boxWidth);

                                    currentToolTips = currentToolTips.isEmpty() && !(tooltips == null) ? tooltips
                                            : currentToolTips;

                                    index++;
                                }
                            }

                            @Override
                            public boolean mouseClicked(
                                    //#if MC >= 12109
                                    MouseButtonEvent click, boolean doubled
                                    //#else
                                    //$$ double mouseX, double mouseY, int button
                                    //#endif
                            ) {
                                //#if MC >= 12109
                                double mouseX = click.x();
                                double mouseY = click.y();
                                int button = click.button();
                                //#endif

                                int index = 0;

                                double adjustedMouseY = mouseY + this.getScrollY();

                                for (RuleWidget entry : rulesInCurrentCategory) {
                                    int x = 5;
                                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                                    entry.onClicked(mouseX, mouseY, mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight, button);
                                    //System.out.println("Clicked:" + entry);
                                    index++;

                                }

                                return super.mouseClicked(
                                        //#if MC >= 12109
                                        click, doubled
                                        //#else
                                        //$$ mouseX, mouseY, button
                                        //#endif
                                );
                            }

                            @Override
                            public boolean charTyped(
                                    //#if MC >= 12109
                                    CharacterEvent charInput
                                    //#else
                                    //$$ char chr, int modifiers
                                    //#endif
                            ) {
                                for (RuleWidget entry : rulesInCurrentCategory) {
                                    if (entry.valueWidget.isFocused()) {
                                        entry.valueWidget.charTyped(
                                                //#if MC >= 12109
                                                charInput
                                                //#else
                                                //$$ chr, modifiers
                                                //#endif
                                        );
                                        return true;
                                    }
                                }
                                return super.charTyped(
                                        //#if MC >= 12109
                                        charInput
                                        //#else
                                        //$$ chr, modifiers
                                        //#endif
                                );
                            }

                            @Override
                            public boolean keyPressed(
                                    //#if MC >= 12109
                                    KeyEvent keyInput
                                    //#else
                                    //$$ int keyCode, int scanCode, int modifiers
                                    //#endif
                            ) {

                                for (RuleWidget entry : rulesInCurrentCategory) {
                                    if (entry.valueWidget.isFocused()) {
                                        entry.valueWidget.keyPressed(
                                                //#if MC >= 12109
                                                keyInput
                                                //#else
                                                //$$ keyCode, scanCode, modifiers
                                                //#endif
                                        );
                                        return true;
                                    }
                                }
                                return super.keyPressed(
                                        //#if MC >= 12109
                                        keyInput
                                        //#else
                                        //$$ keyCode, scanCode, modifiers
                                        //#endif
                                );
                            }

                            @Override
                            protected void updateWidgetNarration(NarrationElementOutput builder) {

                            }

                            @Override
                            protected void drawBox(GuiGraphics context, int x, int y, int width, int height) {
                                context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(),
                                        0x19000000
                                );
                            }
                        });
        //#if MC >= 12106
        rulesScrollableWidget.setAlpha(0.7f);
        //#endif
        this.addRenderableWidget(categoriesScrollableWidget = new

                ConstantScrollableWidget
                        (this.width - (this.width / 3) + 30, 30, 120, this.height - 30, CommonComponents.EMPTY) {
                    int boxWidth = this.width - 10;
                    int boxHeight = 20;
                    int spacing = 5;

                    @Override
                    protected int
                    //if MC < 12104
                    getContentsHeight
                    //else
                    //getContentsHeightWithPadding
                    //endif
                    () {
                        return (boxHeight + spacing) * categoriesInScreen.size() - spacing;
                    }

                    @Override
                    protected double getDeltaYPerScroll() {
                        return 10f;
                    }

                    @Override
                    protected void
                    //if MC < 12104
                    renderContents
                    //else
                    //renderWidget
                    //endif
                    (GuiGraphics context, int mouseX, int mouseY, float delta) {
                        int index = 0;
                        double adjustedMouseY = mouseY + this.getScrollY();

                        for (CategoryEntry categoryEntry : categoriesInScreen) {
                            int x = this.getX() + 5;
                            int y = (this.getY() + boxHeight / 4 + (boxHeight + spacing) * index);

                            boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight;
                            context.fillGradient(x, y, x + boxWidth, y + boxHeight, categoryEntry.selected ? 0x2F060606 : 0x50060606, categoryEntry.selected ? 0x50060606 : 0x20060606);
                            context.drawString(Minecraft.getInstance().font,
                                    categoryEntry.name, x + 5, y + 5, 0xFFFFFFFF, true);

                            context.fill(x, y+boxHeight-2, x + boxWidth,  y+boxHeight,  isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

                            index++;
                        }
                    }


                    @Override
                    public boolean mouseClicked(
                            //#if MC >= 12109
                            MouseButtonEvent click, boolean doubled
                            //#else
                            //$$ double mouseX, double mouseY, int button
                            //#endif
                    ) {
                        //#if MC >= 12109
                        double mouseX = click.x();
                        double mouseY = click.y();
                        int button = click.button();
                        //#endif

                        int index = 0;
                        double adjustedMouseY = mouseY + this.getScrollY();
                        for (CategoryEntry entry : categoriesInScreen) {
                            int x = this.getX() + 5;
                            int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                            if (mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight) {
                                setCurrentCategory(entry.getName());
                                entry.setSelected(true);
                                categoriesInScreen.get(index).setSelected(true);
                                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                                //rulesScrollableWidget.setScrollY()
                                return true;
                            } else {
                                entry.setSelected(false);
                            }
                            index++;
                        }

                        return super.mouseClicked(
                                //#if MC >= 12109
                                click, doubled
                                //#else
                                //$$ mouseX, mouseY, button
                                //#endif
                        );
                    }

                    @Override
                    protected void updateWidgetNarration(NarrationElementOutput builder) {

                    }

                    @Override
                    protected void drawBox(GuiGraphics context, int x, int y, int width, int height) {
                        context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(), 0x0F060606);
                    }

                });
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.blit(
                //#if MC >= 12106
                RenderPipelines.GUI_TEXTURED,
                //#elseif MC >= 12102
                //$$ RenderLayer::getGuiTextured,
                //#endif
                searching ? ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/search_s.png") : ResourceLocation.fromNamespaceAndPath(MOD_ID, "ui/search.png"), 2, 10, 0, 0, 10, 11, 10, 11);
        if (!(currentToolTips == null || currentToolTips.isEmpty()))
            context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, currentToolTips, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderMenuBackgroundTexture(context
                //#if MC > 12004
                , MENU_BACKGROUND, 0, 0, 0.0F, 0.0F, width, height
                //#endif
        );
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        if (FabricLoader.getInstance().isModLoaded("blur") || FabricLoader.getInstance().isModLoaded("modernui")) {
            super.renderBackground(context, mouseX, mouseY, delta);
        } else {
            //#if MC >= 12102
            ResourceLocation BLUR_SHADER = ResourceLocation.withDefaultNamespace("blur");
            PostChain blur = minecraft.getShaderManager().getPostChain(BLUR_SHADER, LevelTargetBundle.MAIN_TARGETS);
            if (blur != null) {

            //#if MC >= 12106
            context.blurBeforeThisStratum();
            //#elseif MC >= 12105
            //$$ blur.render(this.client.getFramebuffer(), gameRenderer.pool, pass -> pass.setUniform("Radius", 20F));
            //#else
            //$$ blur.setUniforms("Radius", 20F);
            //$$ blur.render(client.getFramebuffer(), gameRenderer.pool);
            //#endif
            }
            //#elseif MC > 12004
            //$$ gameRenderer.blurPostProcessor.setUniforms("Radius", 20);
            //$$ gameRenderer.blurPostProcessor.render(delta);
            //#endif

            //#if MC < 12105
            //$$ this.client.getFramebuffer().beginWrite(false);
            //#endif
        }


        context.drawString(Minecraft.getInstance().font,
                currentCategory, this.width - (this.width / 3) + 20, 17, 0xFFFFFFFF, true);
        /*context.fill(this.width - (this.width / 3) + 15, 0, this.width - (this.width / 3) + 20,
                this.height, 0xAAC0C0C0);
        context.fill(this.width - (this.width / 3) + 20, 25, this.width - 2,
                30, 0xAAC0C0C0);*/
    }
}
