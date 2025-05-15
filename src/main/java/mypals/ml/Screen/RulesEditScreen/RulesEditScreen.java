package mypals.ml.Screen.RulesEditScreen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableTextFieldWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static mypals.ml.YetAnotherCarpetAdditionClient.*;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;
import static net.minecraft.client.render.RenderLayer.getGui;

public class RulesEditScreen extends Screen implements ParentElement {
    private static final Text CONFIGURE_TEXT = Text.translatable("gui.screen.configure");
    private static final Identifier CONFIGURE_TEXTURE = Identifier.of(MOD_ID, "textures/gui/configure.png");
    public String currentCategory = "unknown";
    public String lastCategoryBeforeSearching = currentCategory;
    private static CopyOnWriteArrayList<RuleWidget> rulesInCurrentCategory = new CopyOnWriteArrayList<>();
    private static List<CategoryEntry> categoriesInScreen = new ArrayList<>();
    public ScrollableWidget rulesScrollableWidget;
    public ScrollableWidget categoriesScrollableWidget;
    public List<Text> currentToolTips = new ArrayList<>();
    public TextFieldWidget searchFieldWidget;
    public boolean searching = false;
    private static Pattern searchRulePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    public RulesEditScreen(Text title) {
        super(title);
    }

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
                            String englishName = rule.name.split("\\|", 2)[0].trim();
                            return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                        }
                )).toList().forEach(r -> {
                    rulesInCurrentCategory.add(new RuleWidget(r, this));
                });
        if (Objects.equals(currentCategory, "default")) {
            rulesInCurrentCategory.clear();

            chachedRules.stream().filter(r -> {
                String orgName = r.name.split("\\|").length > 1 ? r.name.split("\\|")[1] : r.name.split("\\|")[0];
                return defaultRules.contains(orgName);
            }).sorted(Comparator.comparing(
                    rule -> {
                        String englishName = rule.name.split("\\|", 2)[0].trim();
                        return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                    }
            )).toList().forEach(r -> {
                rulesInCurrentCategory.add(new RuleWidget(r, this));
            });
        }
        if (Objects.equals(currentCategory, "favorite")) {
            rulesInCurrentCategory.clear();

            chachedRules.stream().filter(r -> {
                        String orgName = r.name.split("\\|").length > 1 ? r.name.split("\\|")[1] : r.name.split("\\|")[0];
                        return favoriteRules.contains(orgName);
                    }).sorted(Comparator.comparing(
                            rule -> {
                                String englishName = rule.name.split("\\|", 2)[0].trim();
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

        String[] sections = ruleName.split("\\|", 2);
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
        this.addDrawableChild(searchFieldWidget =
                new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                        15, 10, this.width - (this.width / 3) - 7, 15, CONFIGURE_TEXT) {
                    @Override
                    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                        if (this.isVisible()) {
                            context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY() - 1,
                                    this.width + 1, this.height + 1 + 9, 0x0AAAAAAA);

                            context.fill(RenderLayer.getGuiOverlay(), this.getX(), this.getY() + this.height - 4,
                                    this.width + 1, this.getY() + this.height - 5, 0xAFFFFFFF);
                            super.renderWidget(context, mouseX, mouseY, delta);
                        }
                    }
                });
        searchFieldWidget.setDrawsBackground(false);
        searchFieldWidget.setChangedListener(newText -> {
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
                                    String englishName = rule.name.split("\\|", 2)[0].trim();
                                    return englishName.isEmpty() ? "" : englishName.toLowerCase().substring(0, 1);
                                }
                        ))
                        .toList().forEach(r -> {
                            rulesInCurrentCategory.add(new RuleWidget(r, this));
                        });
            }
        });
        searchFieldWidget.setMaxLength(100);

        this.addDrawableChild(rulesScrollableWidget = new ScrollableTextFieldWidget(0, 30,
                this.width - (this.width / 3), this.height - 30, ScreenTexts.EMPTY) {
            int boxWidth = this.width - 10;
            int boxHeight = 50;
            int spacing = 5;

            @Override
            protected int getContentsHeight() {
                return (boxHeight + spacing) * rulesInCurrentCategory.size() - spacing;
            }

            @Override
            protected double getDeltaYPerScroll() {
                return 0;
            }

            @Override
            protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
                int index = 0;
                double adjustedMouseY = mouseY + this.getScrollY();

                currentToolTips = new ArrayList<>();
                for (RuleWidget entry : rulesInCurrentCategory) {
                    int x = 5;
                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                    entry.setPosition(x, y);
                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y &&
                            adjustedMouseY <= y + boxHeight && mouseY >= this.getY();

                    List<Text> tooltips = entry.renderContents(context, mouseX, mouseY, delta, isMouseOver,
                            index, spacing, boxHeight, boxWidth);

                    currentToolTips = currentToolTips.isEmpty() && !(tooltips == null) ? tooltips
                            : currentToolTips;

                    index++;
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int index = 0;


                double adjustedMouseY = mouseY + this.getScrollY();

                for (RuleWidget entry : rulesInCurrentCategory) {
                    int x = 5;
                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                    entry.onClicked(mouseX, mouseY, mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight, button);
                    index++;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                for (RuleWidget entry : rulesInCurrentCategory) {
                    if (entry.valueWidget.isFocused()) {
                        entry.valueWidget.charTyped(chr, modifiers);
                        return true;
                    }
                }
                return super.charTyped(chr, modifiers);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

                for (RuleWidget entry : rulesInCurrentCategory) {
                    if (entry.valueWidget.isFocused()) {
                        entry.valueWidget.keyPressed(keyCode, scanCode, modifiers);
                        return true;
                    }
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }

//            @Override
//            protected void drawBox(DrawContext context, int x, int y, int width, int height) {
//                context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(), 0x1A000000);
//                //context.fill(x, y, width, height, 0x19000000);
//            }
        });
        this.addDrawableChild(categoriesScrollableWidget = new ScrollableTextFieldWidget(
                this.width - (this.width / 3) + 30, 30, 120, this.height - 30, ScreenTexts.EMPTY) {
            int boxWidth = this.width - 10;
            int boxHeight = 20;
            int spacing = 5;

            @Override
            protected int getContentsHeight() {
                return (boxHeight + spacing) * categoriesInScreen.size() - spacing;
            }

            @Override
            protected double getDeltaYPerScroll() {
                return 0;
            }

            @Override
            protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
                int index = 0;
                double adjustedMouseY = mouseY + this.getScrollY();

                for (CategoryEntry categoryEntry : categoriesInScreen) {
                    int x = this.getX() + 5;
                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;

                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight;
                    context.fill(x, y, x + boxWidth, y + boxHeight, categoryEntry.selected ? 0x2F060606 : 0x50060606);
                    int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
                    context.drawBorder(x, y, boxWidth, boxHeight, borderColor);
                    context.drawText(MinecraftClient.getInstance().textRenderer, categoryEntry.name, x + 5, y + 5, 0xFFFFFF, true);
                    index++;
                }
            }


            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int index = 0;
                double adjustedMouseY = mouseY + this.getScrollY();
                for (CategoryEntry entry : categoriesInScreen) {
                    int x = this.getX() + 5;
                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                    if (mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight) {
                        setCurrentCategory(entry.getName());
                        entry.setSelected(true);
                        categoriesInScreen.get(index).setSelected(true);
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        //rulesScrollableWidget.setScrollY()
                        return true;
                    } else {
                        entry.setSelected(false);
                    }
                    index++;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }

//            @Override
//            protected void drawBox(DrawContext context, int x, int y, int width, int height) {
//                context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(), 0x0F060606);
//            }
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return categoriesScrollableWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || rulesScrollableWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTexture(RenderLayer::getGuiTextured,searching ? Identifier.of(MOD_ID, "ui/search_s.png") : Identifier.of(MOD_ID, "ui/search.png"), 2, 10, 0, 0, 10, 10, 10, 10);
        if (!(currentToolTips == null || currentToolTips.isEmpty()))
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, currentToolTips, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context, MENU_BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, width, height);
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
        if (FabricLoader.getInstance().isModLoaded("blur") || FabricLoader.getInstance().isModLoaded("modernui")) {
            super.renderBackground(context, mouseX, mouseY, delta);
        } else {

            Identifier BLUR_SHADER = Identifier.ofVanilla("blur");
            PostEffectProcessor blur = client.getShaderLoader().loadPostEffect(BLUR_SHADER, DefaultFramebufferSet.MAIN_ONLY);
            if (blur != null) {
                blur.setUniforms("Radius", 20F);
                blur.render(client.getFramebuffer(), gameRenderer.pool);
            }
            this.client.getFramebuffer().beginWrite(false);
        }


        context.drawText(MinecraftClient.getInstance().textRenderer,
                currentCategory, this.width - (this.width / 3) + 20, 17, 0xFFFFFF, true);
        /*context.fill(this.width - (this.width / 3) + 15, 0, this.width - (this.width / 3) + 20,
                this.height, 0xAAC0C0C0);
        context.fill(this.width - (this.width / 3) + 20, 25, this.width - 2,
                30, 0xAAC0C0C0);*/
    }
}
