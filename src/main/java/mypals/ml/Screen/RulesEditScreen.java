package mypals.ml.Screen;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.network.RuleData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static mypals.ml.YetAnotherCarpetAdditionClient.chachedCategories;
import static mypals.ml.YetAnotherCarpetAdditionClient.chachedRules;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class RulesEditScreen extends Screen implements ParentElement {
    private static final Text CONFIGURE_TEXT = Text.translatable("gui.screen.configure");
    private static final Identifier CONFIGURE_TEXTURE = Identifier.of(MOD_ID, "textures/gui/configure.png");
    private String currentCategory = "unknown";
    private static List<RuleData> rulesInCurrentCategory = new ArrayList<>();
    private static List<CategoryEntry> categoriesInScreen = new ArrayList<>();
    public ScrollableWidget rulesScrollableWidget;
    public ScrollableWidget categoriesScrollableWidget;
    public boolean isHoveringARUle = false;
    public List<Text> currentToolTips = new ArrayList<>();

    public RulesEditScreen(Text title) {
        super(title);
    }

    public void setCurrentCategory(String category) {
        this.currentCategory = category;
        categoriesInScreen.clear();
        chachedCategories.forEach(c -> categoriesInScreen.add(new CategoryEntry(c)));
        rulesInCurrentCategory = chachedRules.stream().filter(r -> r.categories.contains(currentCategory)).toList();
    }

    @Override
    protected void init() {
        setCurrentCategory(chachedCategories.getFirst());
        this.addDrawableChild(rulesScrollableWidget = new ScrollableWidget(0, 30,
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

                isHoveringARUle = false;
                for (RuleData entry : rulesInCurrentCategory) {
                    int x = 5;
                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                    int cx = (boxWidth) / 2;
                    int cy = y + 30;

                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight;

                    context.fill(x, y, x + boxWidth, y + boxHeight, 0x19E0E0E0);

                    int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
                    context.drawBorder(x, y, boxWidth, boxHeight, borderColor);
                    StringBuilder categories = new StringBuilder();
                    for (String c : entry.categories) {
                        categories.append(c).append(" | ");
                    }
                    context.drawText(MinecraftClient.getInstance().textRenderer, entry.name + " : " + entry.value, x + 5, y + 5, 0xFFFFFF, false);
                    context.drawText(MinecraftClient.getInstance().textRenderer, entry.defaultValue, x + 5, y + 20, 0xFFFFFF, false);
                    context.drawText(MinecraftClient.getInstance().textRenderer, categories.toString(), x + 5, y + 35, 0xFFFFFF, false);
                    if (isMouseOver) {
                        List<Text> toolTips = new ArrayList<>();
                        toolTips.add(Text.of(entry.name));
                        toolTips.add(Text.of(entry.description));
                        toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.defaultValue").getString() + ": " + entry.defaultValue));
                        toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.currentValue").getString() + ": " + entry.value));
                        toolTips.add(Text.of(Text.translatable("gui.screen.tooltip.suggestions").getString() + ":"));
                        for (String c : entry.suggestions) {
                            toolTips.add(Text.of("  " + c));
                        }
                        isHoveringARUle = true;
                        currentToolTips = toolTips;
                        //YetAnotherCarpetAdditionServer.LOGGER.info("y: " + mouseY);
                    }
                    index++;
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int index = 0;


                double adjustedMouseY = mouseY + this.getScrollY();

                for (RuleData entry : rulesInCurrentCategory) {
                    /*entry.setSelected(false);
                    int x = 5;

                    int y = this.getY() + boxHeight / 4 + (boxHeight + spacing) * index;
                    if (mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight) {
                        entry.onClicked(getInstance());
                        return true;
                    }*/
                    index++;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }

            @Override
            protected void drawBox(DrawContext context, int x, int y, int width, int height) {
                context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(), 0x19000000);
                //context.fill(x, y, width, height, 0x19000000);
            }
        });
        this.addDrawableChild(categoriesScrollableWidget = new ScrollableWidget(
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
                    int cx = (boxWidth) / 2;
                    int cy = y + 30;

                    boolean isMouseOver = mouseX >= x && mouseX <= x + boxWidth && adjustedMouseY >= y && adjustedMouseY <= y + boxHeight;
                    context.fill(x, y, x + boxWidth, y + boxHeight, categoryEntry.selected ? 0xAAAAAAAA : 0x19000000);
                    int borderColor = isMouseOver ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
                    context.drawBorder(x, y, boxWidth, boxHeight, borderColor);
                    context.drawText(MinecraftClient.getInstance().textRenderer, categoryEntry.name, x + 5, y + 5, 0xFFFFFF, false);
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

            @Override
            protected void drawBox(DrawContext context, int x, int y, int width, int height) {
                context.fill(this.getX(), y, this.getX() + boxWidth + 10, this.getBottom(), 0x19000000);
            }
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return categoriesScrollableWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || rulesScrollableWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (isHoveringARUle)
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, currentToolTips, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.drawText(MinecraftClient.getInstance().textRenderer,
                currentCategory, this.width - (this.width / 3) + 20, 17, 0xFFFFFF, true);
        context.fill(this.width - (this.width / 3) + 15, 0, this.width - (this.width / 3) + 20,
                this.height, 0xAAC0C0C0);
        context.fill(this.width - (this.width / 3) + 20, 25, this.width - 2,
                30, 0xAAC0C0C0);
    }
}
