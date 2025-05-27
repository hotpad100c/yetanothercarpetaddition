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

package mypals.ml.utils.render;

import carpet.helpers.HopperCounter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static carpet.helpers.HopperCounter.appropriateColor;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public class PieChartRenderer {
    private static final Map<Item, Block> DEFAULTS = Map.ofEntries(Map.entry(Items.DANDELION, Blocks.YELLOW_WOOL), Map.entry(Items.POPPY, Blocks.RED_WOOL), Map.entry(Items.BLUE_ORCHID, Blocks.LIGHT_BLUE_WOOL), Map.entry(Items.ALLIUM, Blocks.MAGENTA_WOOL), Map.entry(Items.AZURE_BLUET, Blocks.SNOW_BLOCK), Map.entry(Items.RED_TULIP, Blocks.RED_WOOL), Map.entry(Items.ORANGE_TULIP, Blocks.ORANGE_WOOL), Map.entry(Items.WHITE_TULIP, Blocks.SNOW_BLOCK), Map.entry(Items.PINK_TULIP, Blocks.PINK_WOOL), Map.entry(Items.OXEYE_DAISY, Blocks.SNOW_BLOCK), Map.entry(Items.CORNFLOWER, Blocks.BLUE_WOOL), Map.entry(Items.WITHER_ROSE, Blocks.BLACK_WOOL), Map.entry(Items.LILY_OF_THE_VALLEY, Blocks.WHITE_WOOL), Map.entry(Items.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK), Map.entry(Items.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK), Map.entry(Items.STICK, Blocks.OAK_PLANKS), Map.entry(Items.GOLD_INGOT, Blocks.GOLD_BLOCK), Map.entry(Items.IRON_INGOT, Blocks.IRON_BLOCK), Map.entry(Items.DIAMOND, Blocks.DIAMOND_BLOCK), Map.entry(Items.NETHERITE_INGOT, Blocks.NETHERITE_BLOCK), Map.entry(Items.SUNFLOWER, Blocks.YELLOW_WOOL), Map.entry(Items.LILAC, Blocks.MAGENTA_WOOL), Map.entry(Items.ROSE_BUSH, Blocks.RED_WOOL), Map.entry(Items.PEONY, Blocks.PINK_WOOL), Map.entry(Items.CARROT, Blocks.ORANGE_WOOL), Map.entry(Items.APPLE, Blocks.RED_WOOL), Map.entry(Items.WHEAT, Blocks.HAY_BLOCK), Map.entry(Items.PORKCHOP, Blocks.PINK_WOOL), Map.entry(Items.RABBIT, Blocks.PINK_WOOL), Map.entry(Items.CHICKEN, Blocks.WHITE_TERRACOTTA), Map.entry(Items.BEEF, Blocks.NETHERRACK), Map.entry(Items.ENCHANTED_GOLDEN_APPLE, Blocks.GOLD_BLOCK), Map.entry(Items.COD, Blocks.WHITE_TERRACOTTA), Map.entry(Items.SALMON, Blocks.ACACIA_PLANKS), Map.entry(Items.ROTTEN_FLESH, Blocks.BROWN_WOOL), Map.entry(Items.PUFFERFISH, Blocks.YELLOW_TERRACOTTA), Map.entry(Items.TROPICAL_FISH, Blocks.ORANGE_WOOL), Map.entry(Items.POTATO, Blocks.WHITE_TERRACOTTA), Map.entry(Items.MUTTON, Blocks.RED_WOOL), Map.entry(Items.BEETROOT, Blocks.NETHERRACK), Map.entry(Items.MELON_SLICE, Blocks.MELON), Map.entry(Items.POISONOUS_POTATO, Blocks.SLIME_BLOCK), Map.entry(Items.SPIDER_EYE, Blocks.NETHERRACK), Map.entry(Items.GUNPOWDER, Blocks.GRAY_WOOL), Map.entry(Items.TURTLE_SCUTE, Blocks.LIME_WOOL),
            //#if MC > 12006
            Map.entry(Items.ARMADILLO_SCUTE, Blocks.ANCIENT_DEBRIS),
            //#endif
            Map.entry(Items.FEATHER, Blocks.WHITE_WOOL), Map.entry(Items.FLINT, Blocks.BLACK_WOOL), Map.entry(Items.LEATHER, Blocks.SPRUCE_PLANKS), Map.entry(Items.GLOWSTONE_DUST, Blocks.GLOWSTONE), Map.entry(Items.PAPER, Blocks.WHITE_WOOL), Map.entry(Items.BRICK, Blocks.BRICKS), Map.entry(Items.INK_SAC, Blocks.BLACK_WOOL), Map.entry(Items.SNOWBALL, Blocks.SNOW_BLOCK), Map.entry(Items.WATER_BUCKET, Blocks.WATER), Map.entry(Items.LAVA_BUCKET, Blocks.LAVA), Map.entry(Items.MILK_BUCKET, Blocks.WHITE_WOOL), Map.entry(Items.CLAY_BALL, Blocks.CLAY), Map.entry(Items.COCOA_BEANS, Blocks.COCOA), Map.entry(Items.BONE, Blocks.BONE_BLOCK), Map.entry(Items.COD_BUCKET, Blocks.BROWN_TERRACOTTA), Map.entry(Items.PUFFERFISH_BUCKET, Blocks.YELLOW_TERRACOTTA), Map.entry(Items.SALMON_BUCKET, Blocks.PINK_TERRACOTTA), Map.entry(Items.TROPICAL_FISH_BUCKET, Blocks.ORANGE_TERRACOTTA), Map.entry(Items.SUGAR, Blocks.WHITE_WOOL), Map.entry(Items.BLAZE_POWDER, Blocks.GOLD_BLOCK), Map.entry(Items.ENDER_PEARL, Blocks.WARPED_PLANKS), Map.entry(Items.NETHER_STAR, Blocks.DIAMOND_BLOCK), Map.entry(Items.PRISMARINE_CRYSTALS, Blocks.SEA_LANTERN), Map.entry(Items.PRISMARINE_SHARD, Blocks.PRISMARINE), Map.entry(Items.RABBIT_HIDE, Blocks.OAK_PLANKS), Map.entry(Items.CHORUS_FRUIT, Blocks.PURPUR_BLOCK), Map.entry(Items.SHULKER_SHELL, Blocks.SHULKER_BOX), Map.entry(Items.NAUTILUS_SHELL, Blocks.BONE_BLOCK), Map.entry(Items.HEART_OF_THE_SEA, Blocks.CONDUIT), Map.entry(Items.HONEYCOMB, Blocks.HONEYCOMB_BLOCK), Map.entry(Items.NAME_TAG, Blocks.BONE_BLOCK), Map.entry(Items.TOTEM_OF_UNDYING, Blocks.YELLOW_TERRACOTTA), Map.entry(Items.TRIDENT, Blocks.PRISMARINE), Map.entry(Items.GHAST_TEAR, Blocks.WHITE_WOOL), Map.entry(Items.PHANTOM_MEMBRANE, Blocks.BONE_BLOCK), Map.entry(Items.EGG, Blocks.BONE_BLOCK), Map.entry(Items.COPPER_INGOT, Blocks.COPPER_BLOCK), Map.entry(Items.AMETHYST_SHARD, Blocks.AMETHYST_BLOCK));

    /*public static void drawPieChart(DrawContext context, int x, int y, float total, List<Map.Entry<Item, Integer>> itemQuantities) {
        RenderSystem.clear(256, IS_SYSTEM_MAC);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack matrix4fStack = context.getMatrices();
        Matrix4f transformationMatrix = matrix4fStack.peek().getPositionMatrix();
        int chartWidth = 500;
        int centerX = 100;
        int centerY = 100;

        double currentAngle = 0.0;

        for (Map.Entry<Item, Integer> entry : itemQuantities) {
            double percentage = (double) entry.getValue() / total * 100.0;
            int segments = MathHelper.floor(percentage / 4.0) + 1;
            int color = fromItem(entry.getKey(), MinecraftClient.getInstance().world.getRegistryManager());
            int outlineColor = ColorHelper.Argb.mixColor(color, -8355712);

            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(transformationMatrix, (float) centerX, (float) centerY, 0.0F).color(color);

            for (int i = segments; i >= 0; --i) {
                float angle = (float) ((currentAngle + percentage * i / segments) * Math.PI * 2.0 / 100.0);
                float xOffset = MathHelper.sin(angle) * chartWidth;
                float yOffset = MathHelper.cos(angle) * chartWidth * 0.5F;
                bufferBuilder.vertex(transformationMatrix, (float) centerX + xOffset, (float) centerY - yOffset, 0.0F).color(color);
                context.drawHorizontalLine(centerX + (int) xOffset, centerX + 5, 5, Color.white.getRGB());
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = segments; i >= 0; --i) {
                float angle = (float) ((currentAngle + percentage * i / segments) * Math.PI * 2.0 / 100.0);
                float xOffset = MathHelper.sin(angle) * chartWidth;
                float yOffset = MathHelper.cos(angle) * chartWidth * 0.5F;
                if (yOffset <= 0.0F) {
                    bufferBuilder.vertex(transformationMatrix, (float) centerX + xOffset, (float) centerY - yOffset, 0.0F).color(outlineColor);
                    bufferBuilder.vertex(transformationMatrix, (float) centerX + xOffset, (float) centerY - yOffset + 10.0F, 0.0F).color(outlineColor);
                }
            }
            BuiltBuffer builtBuffer = bufferBuilder.endNullable();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }

            currentAngle += percentage;
        }

    }*/

    /*public static int fromItem(Item item, DynamicRegistryManager registryAccess) {
        if (DEFAULTS.containsKey(item)) {
            return appropriateColor(((Block) DEFAULTS.get(item)).getDefaultMapColor().color);
        } else if (item instanceof DyeItem) {
            DyeItem dye = (DyeItem) item;
            return appropriateColor(dye.getColor().getMapColor().color);
        } else {
            Block block = null;
            Registry<Item> itemRegistry = registryAccess.get(RegistryKeys.ITEM);
            Registry<Block> blockRegistry = registryAccess.get(RegistryKeys.BLOCK);
            Identifier id = itemRegistry.getId(item);
            if (item instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) item;
                block = blockItem.getBlock();
            } else if (blockRegistry.getOrEmpty(id).isPresent()) {
                block = blockRegistry.get(id);
            }

            if (block != null) {
                if (block instanceof AbstractBannerBlock) {
                    return appropriateColor(((AbstractBannerBlock) block).getColor().getMapColor().color);
                } else {
                    return block instanceof Stainable ? appropriateColor(((Stainable) block).getColor().getMapColor().color) : appropriateColor(block.getDefaultMapColor().color);
                }
            } else {
                return 0xFFFFFFFF; // Default color if no match found
            }
        }
    }*/
}
