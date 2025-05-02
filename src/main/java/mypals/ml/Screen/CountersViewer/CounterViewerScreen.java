package mypals.ml.Screen.CountersViewer;

import carpet.helpers.HopperCounter;
import mypals.ml.Screen.RulesEditScreen.CategoryEntry;
import mypals.ml.Screen.RulesEditScreen.RuleWidget;
import mypals.ml.YetAnotherCarpetAdditionClient;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.joml.Vector4f;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static mypals.ml.YetAnotherCarpetAdditionClient.*;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class CounterViewerScreen extends Screen implements ParentElement {
    private final Map<String, Map<String, String>> data;
    private final List<String> timestamps;
    private final List<String> counterNames;
    private ConcurrentHashMap<String, List<Point>> counterPoints;
    private static Map<String, Integer> COLORS = new HashMap<>();
    private CyclingButtonWidget viewModeButton;
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

        public void render(MatrixStack poseStack, DrawContext drawContext) {

            poseStack.push();
            poseStack.translate(this.startPoint.x, this.startPoint.y, 0);
            poseStack.multiply(new Quaternionf().rotationAxis((float) Math.toRadians(this.rotationAngleInDeg), new Vector3f(0, 0, 90)));
            if (this.lineWidth % 2 == 0 || this.lineWidth == 1) {
                drawContext.fill(0, 0,
                        (int) this.lineLength + 1, this.lineWidth, this.color);
            } else {
                drawContext.fill(0, -this.lineWidth / 2,
                        (int) this.lineLength + 1, this.lineWidth / 2, this.color);
            }
            poseStack.pop();
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

        public boolean hovered(int mouseX, int mouseY, int size, DrawContext drawContext, int color) {
            return (mouseX < (x + size + 1) && mouseY < (y + size + 1) && mouseX > (x - size - 1) && mouseY > (y - size - 1));
        }

        public boolean render(int size, DrawContext drawContext, int color, int mouseX, int mouseY, boolean alreadyShowingTooltip) {
            drawContext.fill(x + size + 1, y + size + 1, x - size - 1, y - size - 1, new Color(color).darker().getRGB());
            drawContext.fill(x + size, y + size, x - size, y - size, color);
            if (hovered(mouseX, mouseY, size, drawContext, color)) {
                if (alreadyShowingTooltip) return alreadyShowingTooltip;
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(Text.translatable(viewMode.getKey()).getString() + ": " + data.split("\\^\\^\\^")[0]));
                tooltip.add(Text.literal("Time: " + time.substring(11, 22)));
                Arrays.stream(data.split("\\^\\^\\^")[1].split("@@")).forEach(s -> {
                    if (!s.isEmpty()) {
                        tooltip.add(Text.literal(s.split(" t")[0]));
                    }
                });
                drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
                return true;
            } else {
                return alreadyShowingTooltip;
            }
        }
    }

    public CounterViewerScreen(Map<String, Map<String, String>> data) {
        super(Text.literal("Hopper Counter Data Viwer"));
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
                            value = deltaTime > 0 ? (currValue - prevValue) / deltaTime : 0;
*/
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
            int colorValue = color.getSignColor();
            COLORS.put(color.getName(), colorValue);
        });
        viewModeButton = this.addDrawableChild(CyclingButtonWidget.<ViewMode>builder(
                        viewMode -> Text.translatable(viewMode.getKey())
                )
                .values(ViewMode.values())
                .initially(viewMode)
                .build(
                        this.width / 2 - 100, this.height - 30, 100, 20,
                        Text.literal("Mode"),
                        (button, newViewMode) -> {
                            this.viewMode = newViewMode;
                            this.counterPoints = computeCounterPoints();
                        }
                ));
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(this.width / 2, this.height - 30, 100, 20).build());

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        MatrixStack poseStack = context.getMatrices();

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

            context.drawText(this.textRenderer, counter, chartWidth + chartX + 5, chartHeight - 20 - i * 7, color, true);
        }
        if (!timestamps.isEmpty()) {
            context.drawText(this.textRenderer, timestamps.getFirst().substring(11, 16), chartX, chartY + chartHeight + 10, axisColor, true);
            context.drawText(this.textRenderer, timestamps.getLast().substring(11, 16), chartX + chartWidth, chartY + chartHeight + 10, axisColor, true);
        }
        for (int i = 0; i <= 5; i++) {
            int value = (int) ((double) i / 5 * finalMaxCount);
            int y = chartY + chartHeight - (i * chartHeight / 5);
            context.drawText(this.textRenderer, String.valueOf(value), chartX - 30, y - 5, axisColor, false);
        }
        context.fill(chartX, chartY + chartHeight - 1, chartX + chartWidth, chartY + chartHeight, axisColor);
        context.fill(chartX, chartY, chartX + 1, chartY + chartHeight, axisColor);

        int mx = Math.min(chartX + chartWidth, Math.max(chartX, mouseX));
        int my = Math.min(chartY + chartHeight, Math.max(chartY, mouseY));

        context.fill(chartX, my - 1, chartX + chartWidth, my + 1, 0x0FFFFFFF);
        context.fill(mx - 1, chartY, mx + 1, chartY + chartHeight, 0x0FFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);

    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
