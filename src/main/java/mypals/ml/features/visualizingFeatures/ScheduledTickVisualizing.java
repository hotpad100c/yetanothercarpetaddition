package mypals.ml.features.visualizingFeatures;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduledTickVisualizing extends AbstractVisualizingManager<BlockPos, ScheduledTickVisualizing.ScheduledTickObject> {
    private static final ConcurrentHashMap<BlockPos, Map.Entry<ScheduledTickObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 100;

    public static class ScheduledTickObject {
        public long triggerTick;
        public int priority;
        public long subTickOrder;
        public String type;
        public DisplayEntity.TextDisplayEntity tickMarker;
        public String tag;

        public ScheduledTickObject(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String type, boolean isFluid, String tag) {
            this.triggerTick = triggerTick;
            this.priority = priority;
            this.subTickOrder = subTickOrder;
            this.type = type;
            this.tag = tag;
            setVisualizer(world, pos, triggerTick, priority, subTickOrder);
        }

        public void setVisualizer(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder) {
            long time = world.getTime();
            int trigger = (int) (triggerTick - time) - 1;

            if (tickMarker != null && !tickMarker.isRemoved()) {
                JsonObject textJson = new JsonObject();
                textJson.addProperty("text", "");
                JsonArray extra = new JsonArray();

                JsonObject triggerPart = new JsonObject();
                triggerPart.addProperty("text", "T:" + trigger);
                triggerPart.addProperty("color", "red");
                extra.add(triggerPart);

                JsonObject priorityPart = new JsonObject();
                priorityPart.addProperty("text", "\nP:" + priority);
                priorityPart.addProperty("color", "green");
                extra.add(priorityPart);

                JsonObject subTickPart = new JsonObject();
                subTickPart.addProperty("text", "\nS:" + subTickOrder);
                subTickPart.addProperty("color", "blue");
                extra.add(subTickPart);

                NbtCompound nbt = tickMarker.writeNbt(new NbtCompound());
                nbt.putString("text", textJson.toString());
                tickMarker.readNbt(nbt);
            } else {
                tickMarker = summonText(world, pos.toCenterPos().add(0, -0.4, 0), trigger, priority, subTickOrder);
            }
        }

        public void removeVisualizer() {
            if (tickMarker != null) {
                tickMarker.discard();
            }
        }

        private DisplayEntity.TextDisplayEntity summonText(ServerWorld world, Vec3d pos, int trigger, int priority, long subTickOrder) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);

            JsonObject textJson = new JsonObject();
            textJson.addProperty("text", "");
            JsonArray extra = new JsonArray();

            JsonObject triggerPart = new JsonObject();
            triggerPart.addProperty("text", "T:" + trigger);
            triggerPart.addProperty("color", "red");
            extra.add(triggerPart);

            JsonObject priorityPart = new JsonObject();
            priorityPart.addProperty("text", "\nP:" + priority);
            priorityPart.addProperty("color", "green");
            extra.add(priorityPart);

            JsonObject subTickPart = new JsonObject();
            subTickPart.addProperty("text", "\nS:" + subTickOrder);
            subTickPart.addProperty("color", "blue");
            extra.add(subTickPart);

            textJson.add("extra", extra);

            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.putString("billboard", "center");
            nbt.putString("text", textJson.toString());
            nbt.putByte("see_through", (byte) 1);
            //nbt.putInt("background", 0x00000000);
            entity.readNbt(nbt);

            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            return entity;
        }
    }

    @Override
    protected void storeVisualizer(BlockPos key, ScheduledTickObject entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerWorld) entity.tickMarker.getWorld())));
    }

    @Override
    protected void updateVisualizerEntity(ScheduledTickObject marker, Object data) {
        if (data instanceof Object[] tickData && marker.tickMarker != null && !marker.tickMarker.isRemoved()) {
            long triggerTick = (long) tickData[0];
            int priority = (int) tickData[1];
            long subTickOrder = (long) tickData[2];

            long time = marker.tickMarker.getWorld().getTime();
            int trigger = (int) (triggerTick - time) - 1;

            JsonObject textJson = new JsonObject();
            textJson.addProperty("text", "");
            JsonArray extra = new JsonArray();

            JsonObject triggerPart = new JsonObject();
            triggerPart.addProperty("text", "T:" + trigger);
            triggerPart.addProperty("color", "red");
            extra.add(triggerPart);

            JsonObject priorityPart = new JsonObject();
            priorityPart.addProperty("text", "\nP:" + priority);
            priorityPart.addProperty("color", "green");
            extra.add(priorityPart);

            JsonObject subTickPart = new JsonObject();
            subTickPart.addProperty("text", "\nS:" + subTickOrder);
            subTickPart.addProperty("color", "blue");
            extra.add(subTickPart);
            textJson.add("extra", extra);
            
            NbtCompound nbt = marker.tickMarker.writeNbt(new NbtCompound());
            nbt.putString("text", textJson.toString());
            marker.tickMarker.readNbt(nbt);
        }
    }

    @Override
    protected ScheduledTickObject createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof Object[] tickData) {
            long triggerTick = (long) tickData[0];
            int priority = (int) tickData[1];
            long subTickOrder = (long) tickData[2];
            String type = (String) tickData[3];
            boolean isFluid = (boolean) tickData[4];
            BlockPos blockPos = BlockPos.ofFloored(pos);
            return new ScheduledTickObject(world, blockPos, triggerTick, priority, subTickOrder, type, isFluid, getVisualizerTag());
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<ScheduledTickObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected ScheduledTickObject getVisualizer(BlockPos key) {
        Map.Entry<ScheduledTickObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    protected String getVisualizerTag() {
        return "scheduledTickVisualizer";
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.values().forEach(entry -> entry.getKey().removeVisualizer());
        visualizers.clear();
    }

    @Override
    public void updateVisualizer() {

    }

    public void setVisualizer(ServerWorld world, BlockPos pos, long triggerTick, int priority, long subTickOrder, String content, boolean isFluid) {
        Object[] data = new Object[]{triggerTick, priority, subTickOrder, content, isFluid};
        setVisualizer(world, pos, pos.toCenterPos(), data);
    }
}