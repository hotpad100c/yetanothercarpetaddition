package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ScheduledTickVisualizing {
    public static Map<BlockPos, ScheduledTickObject> visualizers = new HashMap<>();

    public static class ScheduledTickObject {
        public long triggerTick;

        public int priority;

        public long subTickOrder;
        public String type;
        public DisplayEntity.TextDisplayEntity tickMarker;

        public ScheduledTickObject(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String type, boolean isFluid) {
            this.triggerTick = triggerTick;
            this.priority = priority;
            this.subTickOrder = subTickOrder;
            this.type = type;

            long time = world.getTime();
            int trigger = (int) (triggerTick - time);
            tickMarker = summon(world, pos.toCenterPos().add(0, -0.4f, 0), "T:" + trigger,
                    "P:" + priority, "S:" + subTickOrder);
            //typeMarker = summon(world, pos.toCenterPos(), name);
        }

        public void setVisualizer(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String type, boolean isFluid) {
            long time = world.getTime();
            int trigger = (int) (triggerTick - time);

            if (tickMarker != null && !tickMarker.isRemoved()) {
                NbtCompound nbt = tickMarker.writeNbt(new NbtCompound());
                String textJson = "{\"text\":\"\",\"extra\":[" +
                        "{\"text\":\"" + String.valueOf(trigger).replace("\"", "\\\"") + "\",\"color\":\"red\"}," +
                        "{\"text\":\"\\n" + String.valueOf(priority).replace("\"", "\\\"") + "\",\"color\":\"green\"}," +
                        "{\"text\":\"\\n" + String.valueOf(subTickOrder).replace("\"", "\\\"") + "\",\"color\":\"blue\"}" +
                        "]}";
                nbt.remove("text");
                nbt.putString("text", textJson);
                tickMarker.readNbt(nbt);
            } else {
                tickMarker = summon(world, pos.toCenterPos().add(0, -0.4f, 0), "T:" + trigger,
                        "P:" + priority, "S:" + subTickOrder);
            }

        }

        public void removeVisualizer() {
            if (tickMarker != null) {
                tickMarker.discard();
            }
        }

        public static DisplayEntity.TextDisplayEntity summon(ServerWorld world, Vec3d pos, String trigger, String priority, String subTickOrder) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag("scheduledTickVisualizer");
            world.spawnEntity(entity);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.putString("billboard", "center");
            String textJson = "{\"text\":\"\",\"extra\":[" +
                    "{\"text\":\"" + trigger.replace("\"", "\\\"") + "\",\"color\":\"red\"}," +
                    "{\"text\":\"\\n" + priority.replace("\"", "\\\"") + "\",\"color\":\"green\"}," +
                    "{\"text\":\"\\n" + subTickOrder.replace("\"", "\\\"") + "\",\"color\":\"blue\"}" +
                    "]}";
            nbt.putByte("see_through", (byte) 1);
            nbt.putInt("background", 0x00000000);
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
            return entity;
        }
    }

    public static void setVisualizer(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String content, boolean isFluid) {
        if (visualizers.containsKey(pos)) {
            ScheduledTickObject visualizer = visualizers.get(pos);
            visualizer.setVisualizer(world, pos, triggerTick, priority, subTickOrder, content, false);
        } else {
            visualizers.put(pos, new ScheduledTickObject(world, pos, triggerTick, priority, subTickOrder, content, isFluid));
        }
    }

    public static void removeVisualizer(BlockPos pos) {
        if (visualizers.containsKey(pos)) {
            visualizers.get(pos).removeVisualizer();
            visualizers.remove(pos);
        }
    }

    public static void clearVisualizers(ServerCommandSource source) {
        visualizers.clear();

        List<DisplayEntity.TextDisplayEntity> entities = new ArrayList<>();
        Predicate<DisplayEntity.TextDisplayEntity> predicate = marker -> marker.getCommandTags().contains("scheduledTickVisualizer");

        source.getWorld().collectEntitiesByType(EntityType.TEXT_DISPLAY,
                predicate,
                entities);
        entities.forEach(Entity::discard);
    }
}
