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

package mypals.ml.screen.countersViewerScreen;


import java.awt.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
//#if MC >= 260100
//$$ import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
import net.minecraft.client.gui.GuiGraphics;
//#endif
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

//#if MC >= 12106
import org.joml.Matrix3x2fStack;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import org.joml.Quaternionf;
//$$ import org.joml.Vector3f;
//#endif

public class CounterViewerScreen extends Screen implements ContainerEventHandler {
    private final Map<String, Map<String, String>> data;
    private final List<String> timestamps;
    private final List<String> counterNames;
    private ConcurrentHashMap<String, List<Point>> counterPoints;
    private static Map<String, Integer> COLORS = new HashMap<>();
    private CycleButton viewModeButton;
    public static ViewMode viewMode = ViewMode.TOTAL;
    public int maxCountedValue = 0;
    public int finalMaxCount = 0;

    public enum ViewMode {
        GROWTH("screen.counterViewr.growth"),
        TOTAL("screen.counterViewr.total");

        private ViewMode(String key) {
            this.key = key;
        }

        private String key;

        public String getKey() {
            return key;
        }

    }

    private static class Point {
        final double x;
        final double y;
        final String value;
        final String time;

        Point(double x, double y, String value, String time) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.time = time;
        }
    }

    private static class Line {
        public final RenderPoint startPoint, endPoint;
        private final int color, lineWidth;
        private float lineLength, rotationAngleInDeg;

        public Line(RenderPoint start, RenderPoint end, int lineWidth, int color) {
            this.startPoint = start;
            this.endPoint = end;
            this.lineWidth = Math.abs(lineWidth);
            this.color = color;

            this.calculateLine();
        }

        public Line(int x1, int y1, int x2, int y2, int lineWidth, int color, String data, String time) {
            this(new RenderPoint(x1, y1, data, time), new RenderPoint(x2, y2, data, time), lineWidth, color);
        }

        public void render(
                //#if MC >= 12106
                Matrix3x2fStack poseStack
                //#else
                //$$ PoseStack poseStack
                //#endif
                //#if MC >= 260100
                //$$ , GuiGraphicsExtractor drawContext
                //#else
                , GuiGraphics drawContext
                //#endif
        ) {

            //#if MC >= 12106
            poseStack.pushMatrix();
            poseStack.translate(this.startPoint.x, this.startPoint.y);
            poseStack.rotate(this.rotationAngleInDeg);
            //#else
            //$$ poseStack.pushPose();
            //$$ poseStack.translate(this.startPoint.x, this.startPoint.y, 0);
            //$$ poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(this.rotationAngleInDeg), new Vector3f(0, 0, 90)));
            //#endif
            if (this.lineWidth % 2 == 0 || this.lineWidth == 1) {
                drawContext.fill(0, 0,
                        (int) this.lineLength + 1, this.lineWidth, this.color);
            } else {
                drawContext.fill(0, -this.lineWidth / 2,
                        (int) this.lineLength + 1, this.lineWidth / 2, this.color);
            }
            //#if MC >= 12106
            poseStack.popMatrix();
            //#else
            //$$ poseStack.popPose();
            //#endif
        }

        private void calculateLine() {
            final int deltaX = this.endPoint.x - this.startPoint.x;
            final int deltaY = this.startPoint.y - this.endPoint.y;

            this.lineLength = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            this.rotationAngleInDeg = (float) Math.toDegrees(Math.atan2(deltaY, deltaX)) * -1;
        }
    }

    private static class RenderPoint {
        public final int x, y;
        public String data;
        public String time;

        public RenderPoint(int x, int y, String data, String time) {
            this.x = x;
            this.y = y;
            this.data = data;
            this.time = time;
        }

        //#if MC >= 260100
        //$$ public boolean hovered(int mouseX, int mouseY, int size, GuiGraphicsExtractor drawContext, int color) {
        //#else
        public boolean hovered(int mouseX, int mouseY, int size, GuiGraphics drawContext, int color) {
        //#endif
            return (mouseX < (x + size + 1) && mouseY < (y + size + 1) && mouseX > (x - size - 1) && mouseY > (y - size - 1));
        }

        //#if MC >= 260100
        //$$ public boolean render(int size, GuiGraphicsExtractor drawContext, int color, int mouseX, int mouseY, boolean alreadyShowingTooltip) {
        //#else
        public boolean render(int size, GuiGraphics drawContext, int color, int mouseX, int mouseY, boolean alreadyShowingTooltip) {
        //#endif
            drawContext.fill(x + size + 1, y + size + 1, x - size - 1, y - size - 1, new Color(color).darker().getRGB());
            drawContext.fill(x + size, y + size, x - size, y - size, color);
            if (hovered(mouseX, mouseY, size, drawContext, color)) {
                if (alreadyShowingTooltip) return alreadyShowingTooltip;
                List<Component> tooltip = new ArrayList<>();
                List<Map.Entry<Item, Integer>> items = new ArrayList<>();
                tooltip.add(Component.literal(Component.translatable(viewMode.getKey()).getString() + ": " + data.split("\\^\\^\\^")[0]));
                tooltip.add(Component.literal("Time: " + time.substring(11, 22)));
                Arrays.stream(data.split("\\^\\^\\^")[1].split("@@")).forEach(s -> {
                    if (!s.isEmpty()) {

                        tooltip.add(Component.literal(s.split(" t")[0]));
                    }
                });

                drawContext.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);

                //PieChartRenderer.drawPieChart(drawContext, 100, 100, Float.parseFloat(data.split("\\^\\^\\^")[0]), items);

                return true;
            } else {
                return alreadyShowingTooltip;
            }
        }
    }


    public CounterViewerScreen(Map<String, Map<String, String>> data) {
        super(Component.literal("Hopper Counter Data Viwer"));
        this.data = data;
        this.timestamps = new ArrayList<>(data.keySet()).stream()
                .sorted()
                .collect(Collectors.toList());
        this.counterNames = data.values().stream()
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        this.counterPoints = computeCounterPoints();
    }

    private ConcurrentHashMap<String, List<Point>> computeCounterPoints() {
        ConcurrentHashMap<String, List<Point>> points = new ConcurrentHashMap<>();
        double maxCountedValue = 1.0;

        if (viewMode == ViewMode.TOTAL) {
            for (String counter : counterNames) {
                List<Point> counterPoints = new ArrayList<>();
                for (int i = 0; i < timestamps.size(); i++) {
                    String timestamp = timestamps.get(i);
                    Map<String, String> counters = data.get(timestamp);
                    String valueStr = counters.getOrDefault(counter, "0");
                    double value;
                    try {
                        value = Integer.parseInt(valueStr);
                    } catch (NumberFormatException e) {
                        value = 0;
                    }
                    double x = (double) i / Math.max(1, timestamps.size() - 1);
                    double y = value / maxCountedValue;
                    counterPoints.add(new Point(x, y, valueStr, timestamp));
                }
                points.put(counter, counterPoints);
            }
        } else {

            for (String counter : counterNames) {
                List<Point> counterPoints = new ArrayList<>();
                for (int i = 0; i < timestamps.size(); i++) {
                    double value = 0;
                    String currTimestamp = "";
                    if (i > 0) {
                        currTimestamp = timestamps.get(i);
                        String prevTimestamp = timestamps.get(i - 1);
                        Map<String, String> currCounters = data.get(currTimestamp);
                        Map<String, String> prevCounters = data.get(prevTimestamp);
                        try {
                            double currValue = Integer.parseInt(currCounters.getOrDefault(counter, "0").split("\\^\\^\\^")[0]);
                            double prevValue = Integer.parseInt(prevCounters.getOrDefault(counter, "0").split("\\^\\^\\^")[0]);
                            /*Instant currInstant = Instant.parse(currTimestamp);
                            Instant prevInstant = Instant.parse(prevTimestamp);
                            double deltaTime = (currInstant.toEpochMilli() - prevInstant.toEpochMilli()) / 1000.0;
                            value = deltaTime > 0 ? (currValue - prevValue) / deltaTime : 0;*/
                            value = Math.max(0, currValue - prevValue);
                        } catch (NumberFormatException | DateTimeParseException e) {
                            value = 0;
                        }
                    }
                    double x = (double) i / Math.max(1, timestamps.size() - 1);
                    double y = maxCountedValue > 0 ? value / maxCountedValue : 0;

                    String origVal = data.getOrDefault(currTimestamp, new HashMap<>()).getOrDefault(counter, "0^^^ERROR!@@");
                    String newVal = value + "^^^" + origVal.split("\\^\\^\\^")[1];

                    counterPoints.add(new Point(x, y, newVal, currTimestamp));
                }
                points.put(counter, counterPoints);
            }
        }

        maxCountedValue = points.values().stream()
                .flatMap(List::stream)
                .mapToDouble(p -> Math.abs(Double.parseDouble(p.value.split("\\^\\^\\^")[0])))
                .max()
                .orElse(1.0);
        finalMaxCount = (int) maxCountedValue;
        for (String counter : points.keySet()) {
            List<Point> counterPoints = points.get(counter);
            List<Point> normalizedPoints = new ArrayList<>();
            for (Point p : counterPoints) {
                double count = Double.parseDouble(p.value.split("\\^\\^\\^")[0]);
                double y = maxCountedValue > 0 ? count / maxCountedValue : 0;
                normalizedPoints.add(new Point(p.x, y, p.value, p.time));
            }
            points.put(counter, normalizedPoints);
        }
        return points;
    }

    @Override
    protected void init() {
        Arrays.stream(DyeColor.values()).forEach(color -> {
            int colorValue = color.getTextColor();
            COLORS.put(color.getName(), colorValue);
        });
        viewModeButton = this.addRenderableWidget(CycleButton.<ViewMode>builder(
                        viewMode -> Component.translatable(viewMode.getKey())
                //#if MC >= 12111
                        , viewMode)
                //#else
                //$$ ).withInitialValue(viewMode)
                //#endif
                .withValues(ViewMode.values())
                .create(
                        this.width / 2 - 100, this.height - 30, 100, 20,
                        Component.literal("Mode"),
                        (button, newViewMode) -> {
                            this.viewMode = newViewMode;
                            this.counterPoints = computeCounterPoints();
                        }
                ));
        this.addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> this.onClose()
        ).bounds(this.width / 2, this.height - 30, 100, 20).build());

    }

    @Override
    //#if MC >= 260100
    //$$ public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    //#else
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    //#endif
        //#if MC >= 260100
        //$$ super.extractRenderState(context, mouseX, mouseY, delta);
        //#else
        super.render(context, mouseX, mouseY, delta);
        //#endif

        //#if MC >= 12106
        Matrix3x2fStack poseStack = context.pose();
        //#else
        //$$ PoseStack poseStack = context.pose();
        //#endif

        int chartX = 40;
        int chartY = 50;
        int chartWidth = this.width - 100;
        int chartHeight = this.height - 100;
        int axisColor = 0xFFFFFFFF;
        boolean alreadyShowingTooltip = false;
        for (int i = 0; i < counterNames.size(); i++) {
            String counter = counterNames.get(i);
            List<Point> points = counterPoints.get(counter);
            int color = new Color(COLORS.getOrDefault(counter, 0xFFFFFFFF)).getRGB();

            for (int j = 1; j < points.size(); j++) {
                Point p1 = points.get(j - 1);
                Point p2 = points.get(j);
                int x1 = chartX + (int) (p1.x * chartWidth);
                int y1 = chartY + chartHeight - (int) (p1.y * chartHeight);
                int x2 = chartX + (int) (p2.x * chartWidth);
                int y2 = chartY + chartHeight - (int) (p2.y * chartHeight);
                Line l = new Line(x1, y1, x2, y2, 1, color, p2.value, p2.time);
                l.render(poseStack, context);
                alreadyShowingTooltip = l.endPoint.render(1, context, color, mouseX, mouseY, alreadyShowingTooltip);
                context.fill(x1 - 1, y1 - 1, x1 + 1, y1 + 1, color);
            }

            //#if MC >= 260100
            //$$ context.text(this.font, counter, chartWidth + chartX + 5, chartHeight - 20 - i * 7, color, true);
            //#else
            context.drawString(this.font, counter, chartWidth + chartX + 5, chartHeight - 20 - i * 7, color, true);
            //#endif
        }
        if (!timestamps.isEmpty()) {
            //#if MC >= 260100
            //$$ context.text(this.font, timestamps.getFirst().substring(11, 16), chartX, chartY + chartHeight + 10, axisColor, true);
            //#else
            context.drawString(this.font, timestamps.getFirst().substring(11, 16), chartX, chartY + chartHeight + 10, axisColor, true);
            //#endif
            //#if MC >= 260100
            //$$ context.text(this.font, timestamps.getLast().substring(11, 16), chartX + chartWidth, chartY + chartHeight + 10, axisColor, true);
            //#else
            context.drawString(this.font, timestamps.getLast().substring(11, 16), chartX + chartWidth, chartY + chartHeight + 10, axisColor, true);
            //#endif
        }
        for (int i = 0; i <= 5; i++) {
            int value = (int) ((double) i / 5 * finalMaxCount);
            int y = chartY + chartHeight - (i * chartHeight / 5);
            //#if MC >= 260100
            //$$ context.text(this.font, String.valueOf(value), chartX - 30, y - 5, axisColor, false);
            //#else
            context.drawString(this.font, String.valueOf(value), chartX - 30, y - 5, axisColor, false);
            //#endif
        }
        context.fill(chartX, chartY + chartHeight - 1, chartX + chartWidth, chartY + chartHeight, axisColor);
        context.fill(chartX, chartY, chartX + 1, chartY + chartHeight, axisColor);

        int mx = Math.min(chartX + chartWidth, Math.max(chartX, mouseX));
        int my = Math.min(chartY + chartHeight, Math.max(chartY, mouseY));

        context.fill(chartX, my - 1, chartX + chartWidth, my + 1, 0x0FFFFFFF);
        context.fill(mx - 1, chartY, mx + 1, chartY + chartHeight, 0x0FFFFFFF);
        //#if MC >= 260100
        //$$ context.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        //#else
        context.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        //#endif

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
