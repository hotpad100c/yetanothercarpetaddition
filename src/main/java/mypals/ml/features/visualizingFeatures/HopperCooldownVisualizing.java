package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class HopperCooldownVisualizing {
    public static Map<BlockPos, DisplayEntity.TextDisplayEntity> visualizers = new HashMap<>();

    public static void setVisualizer(ServerWorld world, BlockPos pos, int cooldown) {
        if (visualizers.containsKey(pos)) {
            DisplayEntity.TextDisplayEntity entity = visualizers.get(pos);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            String textJson = "{\"text\":\"" + String.valueOf(cooldown) + "\",\"color\":\"" + color + "\"}";
            nbt.remove("text");
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
        } else {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ());
            entity.addCommandTag("hopperCooldownVisualizer");
            world.spawnEntity(entity);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            nbt.putString("billboard", "center");
            String textJson = "{\"text\":\"" + String.valueOf(cooldown) + "\",\"color\":\"" + color + "\"}";
            nbt.putByte("see_through", (byte) 1);
            nbt.putInt("background", 0x00000000);
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
            visualizers.put(pos, entity);
        }
    }

    public static void removeVisualizer(BlockPos pos) {
        if (visualizers.containsKey(pos)) {
            DisplayEntity.TextDisplayEntity entity = visualizers.get(pos);
            entity.discard();
            visualizers.remove(pos);
        }
    }

    public static void clearVisualizers(ServerCommandSource source) {
        visualizers.clear();
        List<DisplayEntity.TextDisplayEntity> entities = new ArrayList<>();
        Predicate<DisplayEntity.TextDisplayEntity> predicate = marker -> marker.getCommandTags().contains("hopperCooldownVisualizer");

        source.getWorld().collectEntitiesByType(EntityType.TEXT_DISPLAY,
                predicate,
                entities);
        entities.forEach(Entity::discard);
    }
}
