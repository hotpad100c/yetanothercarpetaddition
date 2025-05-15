package mypals.ml.features.visualizingFeatures;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractVisualizingManager<T, E> {
    protected abstract void storeVisualizer(T key, E entity);

    protected abstract void updateVisualizerEntity(E marker, Object data);

    protected abstract E createVisualizerEntity(ServerWorld world, Vec3d pos, Object data);

    protected abstract void removeVisualizerEntity(T key);

    protected static long getDeleteTick(int duration, ServerWorld world) {
        return world.getTime() + duration;
    }


    public void setVisualizer(ServerWorld world, T key, Vec3d pos, Object data) {
        E marker = getVisualizer(key);
        if (marker != null) {
            updateVisualizerEntity(marker, data);
        } else {
            marker = createVisualizerEntity(world, pos, data);
            storeVisualizer(key, marker);
        }
    }

    public void removeVisualizer(T key) {
        removeVisualizerEntity(key);
    }

    protected abstract void clearAllVisualizers();

    public void clearVisualizers(MinecraftServer server) {
        EntityHelper.clearVisualizersInServer(server, getVisualizerTag());
    }

    protected abstract E getVisualizer(T key);


    protected abstract String getVisualizerTag();


    protected NbtCompound configureCommonNbt(NbtCompound nbt) {
        nbt.putString("billboard", "center");
        nbt.putByte("see_through", (byte) 1);
        return nbt;
    }


    public abstract void updateVisualizer();
}
