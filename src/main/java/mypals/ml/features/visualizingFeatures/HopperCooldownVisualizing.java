package mypals.ml.features.visualizingFeatures;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class HopperCooldownVisualizing extends AbstractVisualizingManager<BlockPos, DisplayEntity.TextDisplayEntity> {
    private static final Map<BlockPos, DisplayEntity.TextDisplayEntity> visualizers = new HashMap<>();

    @Override
    protected void storeVisualizer(BlockPos key, DisplayEntity.TextDisplayEntity entity) {
        visualizers.put(key, entity);
    }

    @Override
    protected void updateVisualizerEntity(DisplayEntity.TextDisplayEntity entity, Object data) {
        if (data instanceof Integer cooldown) {
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            HashMap<String, NbtElement> textNbt = new HashMap<>();
            textNbt.put("text", NbtString.of("[" + cooldown + "]"));
            textNbt.put("color", NbtString.of(color));
            NbtCompound textComponent = new NbtCompound(textNbt);
            nbt.remove("text");
            nbt.put("text", textComponent);
            entity.readNbt(nbt);
        }
    }

    @Override
    protected DisplayEntity.TextDisplayEntity createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof Integer cooldown) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(getVisualizerTag());
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            nbt = configureCommonNbt(nbt);
            HashMap<String, NbtElement> textNbt = new HashMap<>();
            textNbt.put("text", NbtString.of("[" + cooldown + "]"));
            textNbt.put("color", NbtString.of(color));
            NbtCompound textComponent = new NbtCompound(textNbt);
            nbt.put("text", textComponent);
            entity.readNbt(nbt);
            return entity;
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        DisplayEntity.TextDisplayEntity entity = visualizers.get(key);
        if (entity != null) {
            entity.discard();
            visualizers.remove(key);
        }
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.clear();
    }

    @Override
    protected DisplayEntity.TextDisplayEntity getVisualizer(BlockPos key) {
        return visualizers.get(key) == null ? null : visualizers.get(key);
    }

    @Override
    protected String getVisualizerTag() {
        return "hopperCooldownVisualizer";
    }
    

    @Override
    public void updateVisualizer() {
    }
}