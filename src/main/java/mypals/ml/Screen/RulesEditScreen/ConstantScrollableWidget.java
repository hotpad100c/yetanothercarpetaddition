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

//#if MC >= 12105
import com.mojang.blaze3d.opengl.GlStateManager;
import mypals.ml.utils.adapter.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

//#if MC >= 12109
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//#endif
//#elseif MC >= 12102
//$$ import net.minecraft.client.render.RenderLayer;
//#endif
@Environment(EnvType.CLIENT)
public abstract class ConstantScrollableWidget extends AbstractWidget implements Renderable, GuiEventListener {
    //#if MC >= 12106
    private static final WidgetSprites TEXT_FIELD_TEXTURES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted"));
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.withDefaultNamespace("widget/scroller");
    //#else
    //$$ private static final ButtonTextures TEXT_FIELD_TEXTURES = new ButtonTextures(Identifier.of("minecraft","widget/text_field"), Identifier.of("minecraft","widget/text_field_highlighted"));
    //$$ private static final Identifier SCROLLER_TEXTURE = Identifier.of("minecraft","widget/scroller");
    //#endif
    private static final int PADDING = 4;
    private static final int SCROLLER_WIDTH = 8;
    private double scrollY;
    private boolean scrollbarDragged;

    public ConstantScrollableWidget(int i, int j, int k, int l, Component text) {
        super(i, j, k, l, text);
    }

    //#if MC >= 12109
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
    //#else
    //$$ public boolean mouseClicked(double mouseX, double mouseY, int button) {
    //#endif

        //#if MC >= 12109
        double mouseY = click.y();
        double mouseX = click.x();
        int button = click.button();
        //#endif

        if (!this.visible) {
            return false;
        } else {
            boolean bl = this.isWithinBounds(mouseX, mouseY);
            boolean bl2 = this.overflows() && mouseX >= (double)(this.getX() + this.width) && mouseX <= (double)(this.getX() + this.width + 8) && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + this.height);
            if (bl2 && button == 0) {
                this.scrollbarDragged = true;
                return true;
            } else {
                return bl || bl2;
            }
        }
    }

    public boolean mouseReleased(
            //#if MC >= 12109
            MouseButtonEvent click
            //#else
            //$$ double mouseX, double mouseY, int button
            //#endif
    ) {
        if (
            //#if MC >= 12109
            click.button() == 0
            //#else
            //$$ button == 0
            //#endif
        ) {
            this.scrollbarDragged = false;
        }

        return super.mouseReleased(
                //#if MC >= 12109
                click
                //#else
                //$$  mouseX, mouseY,  button
                //#endif
        );
    }

    public boolean mouseDragged(
            //#if MC >= 12109
            MouseButtonEvent click,
            //#else
            //$$ double mouseX, double mouseY
            //#endif
            //#if MC < 12109
            //$$ , int button,
            //#endif
            double deltaX, double deltaY) {
        //#if MC >= 12109
        double mousey = click.y();
        double mousex = click.x();
        //#else
        //$$ double mousey = mouseX;
        //$$ double mousex = mouseY;
        //#endif



        if (this.visible && this.isFocused() && this.scrollbarDragged) {
            if (mousey < (double)this.getY()) {
                this.setScrollY((double)0.0F);
            } else if (mousey > (double)(this.getY() + this.height)) {
                this.setScrollY((double)this.getMaxScrollY());
            } else {
                int i = this.getScrollbarThumbHeight();
                double d = (double)Math.max(1, this.getMaxScrollY() / (this.height - i));
                this.setScrollY(this.scrollY + deltaY * d);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollY(this.scrollY - verticalAmount * this.getDeltaYPerScroll());
            return true;
        }
    }

    public boolean keyPressed(
            //#if MC >= 12109
            KeyEvent keyInput
            //#else
            //$$ int keyCode, int scanCode, int modifiers
            //#endif
    ) {

        //#if MC >= 12109
        boolean bl = keyInput.key() == 265;
        boolean bl2 = keyInput.key() == 264;
        //#else
        //$$ boolean bl = keyCode == 265;
        //$$ boolean bl2 = keyCode == 264;
        //#endif


        if (bl || bl2) {
            double d = this.scrollY;
            this.setScrollY(this.scrollY + (double)(bl ? -1 : 1) * this.getDeltaYPerScroll());
            if (d != this.scrollY) {
                return true;
            }
        }

        return super.keyPressed(
                //#if MC >= 12109
                keyInput
                //#else
                //$$ keyCode, scanCode,  modifiers
                //#endif
        );
    }

    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.drawBox(context);
            context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);

            //#if MC >= 12106
            context.pose().pushMatrix();
            //#else
            //$$ context.getMatrices().push();
            //#endif

            //#if MC >= 12106
            context.pose().translate(0.0F, (float) -this.scrollY);
            //#else
            //$$ context.getMatrices().translate((double)0.0F, -this.scrollY, (double)0.0F);
            //#endif

            this.renderContents(context, mouseX, mouseY, delta);
            //#if MC >= 12106
            context.pose().popMatrix();
            //#else
            //$$ context.getMatrices().pop();
            //#endif
            context.disableScissor();
            this.renderOverlay(context);
        }
    }

    private int getScrollbarThumbHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentsHeightWithPadding()), 32, this.height);
    }

    protected void renderOverlay(GuiGraphics context) {
        if (this.overflows()) {
            this.drawScrollbar(context);
        }

    }

    protected int getPadding() {
        return 4;
    }

    protected int getPaddingDoubled() {
        return this.getPadding() * 2;
    }

    public double getScrollY() {
        return this.scrollY;
    }

    public void setScrollY(double scrollY) {
        this.scrollY = Mth.clamp(scrollY, (double)0.0F, (double)this.getMaxScrollY());
    }

    protected int getMaxScrollY() {
        return Math.max(0, this.getContentsHeightWithPadding() - (this.height - 4));
    }

    private int getContentsHeightWithPadding() {
        return this.getContentsHeight() + 4;
    }

    protected void drawBox(GuiGraphics context) {
        this.drawBox(context, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    protected void drawBox(GuiGraphics context, int x, int y, int width, int height) {
    }

    private void drawScrollbar(GuiGraphics context) {
        int i = this.getScrollbarThumbHeight();
        int j = this.getX() + this.width;
        int k = Math.max(this.getY(), (int)this.scrollY * (this.height - i) / this.getMaxScrollY() + this.getY());
        GlStateManager._enableBlend();
        context.blitSprite(
                //#if MC >= 12106
                RenderPipelines.GUI_TEXTURED,
                //#elseif MC >= 12102
                //$$ RenderLayer::getGuiTextured,
                //#endif
                SCROLLER_TEXTURE, j, k, 8, i);
        GlStateManager._disableBlend();
    }

    protected boolean isVisible(int top, int bottom) {
        return (double)bottom - this.scrollY >= (double)this.getY() && (double)top - this.scrollY <= (double)(this.getY() + this.height);
    }

    protected boolean isWithinBounds(double mouseX, double mouseY) {
        return mouseX >= (double)this.getX() && mouseX < (double)(this.getX() + this.width) && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + this.height);
    }

    protected boolean overflows() {
        return this.getContentsHeight() > this.getHeight();
    }

    public int getScrollerWidth() {
        return 8;
    }

    protected abstract int getContentsHeight();

    protected abstract double getDeltaYPerScroll();

    protected abstract void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta);
}
