package mypals.ml.features.waypoint;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WaypointState extends PersistentState {
    private static final String FILE_NAME = "waypoints";
    public static final Type<WaypointState> TYPE = new Type<>(
            WaypointState::new,
            (nbt, registryLookup) -> WaypointState.fromNbt(nbt),
            null
    );

    private final Map<String, BlockPos> waypoints = new HashMap<>();

    public WaypointState() {
    }


    public static WaypointState fromNbt(NbtCompound nbt) {
        WaypointState state = new WaypointState();
        NbtCompound waypointsNbt = nbt.getCompound("waypoints");

        for (String name : waypointsNbt.getKeys()) {
            int[] posArray = waypointsNbt.getIntArray(name);
            state.waypoints.put(name, new BlockPos(posArray[0], posArray[1], posArray[2]));
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound waypointsNbt = new NbtCompound();
        for (Map.Entry<String, BlockPos> entry : waypoints.entrySet()) {
            BlockPos pos = entry.getValue();
            waypointsNbt.putIntArray(entry.getKey(), new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }

        nbt.put("waypoints", waypointsNbt);
        return nbt;
    }

    public BlockPos getWaypoint(String name) {
        return waypoints.get(name);
    }

    public void setWaypoint(String name, BlockPos pos) {
        waypoints.put(name, pos);
        markDirty();
    }

    public boolean removeWaypoint(String name) {
        if (waypoints.remove(name) != null) {
            markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getAllNames() {
        return waypoints.keySet();
    }

    public static WaypointState get(ServerWorld world) {
        return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(
                TYPE,
                FILE_NAME
        );
    }
}
