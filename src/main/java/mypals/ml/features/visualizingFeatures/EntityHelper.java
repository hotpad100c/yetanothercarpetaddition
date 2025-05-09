package mypals.ml.features.visualizingFeatures;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.TextDisplayEntity;
import net.minecraft.entity.decoration.BlockDisplayEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;

public class EntityHelper {
    public static NbtCompound scaleEntity(NbtCompound nbt, float scale) {
        NbtCompound scaleNbt = new NbtCompound();
        NbtCompound transformation = new NbtCompound();
        // Right rotation (identity quaternion: no rotation)
        NbtList rightRotation = new NbtList();
        rightRotation.add(NbtFloat.of(0.0f));
        rightRotation.add(NbtFloat.of(0.0f));
        rightRotation.add(NbtFloat.of(0.0f));
        rightRotation.add(NbtFloat.of(1.0f));
        transformation.put("right_rotation", rightRotation);
        // Left rotation (identity quaternion: no rotation)
        NbtList leftRotation = new NbtList();
        leftRotation.add(NbtFloat.of(0.0f));
        leftRotation.add(NbtFloat.of(0.0f));
        leftRotation.add(NbtFloat.of(0.0f));
        leftRotation.add(NbtFloat.of(1.0f));
        transformation.put("left_rotation", leftRotation);
        // Translation (no offset)
        NbtList translation = new NbtList();
        translation.add(NbtFloat.of(0.5f * scale));
        translation.add(NbtFloat.of(0.5f * scale));
        translation.add(NbtFloat.of(0.5f * scale));
        transformation.put("translation", translation);
        // Scale
        NbtList scaleList = new NbtList();
        scaleList.add(NbtFloat.of(scale));
        scaleList.add(NbtFloat.of(scale));
        scaleList.add(NbtFloat.of(scale));
        transformation.put("scale", scaleList);
        nbt.put("transformation", transformation);
        return nbt;
    }
    public static void clearVisualizersInServer(MinecraftServer server, String target){
        for (ServerWorld world : server.getWorlds()) {
            clearWorldVisualizers(world, target);
        }
    }
    public static void clearWorldVisualizers(ServerWorld world, String target) {
        if (world!= null) {
            List<DisplayEntity.TextDisplayEntity> entities = new ArrayList<>();
            Predicate<DisplayEntity.TextDisplayEntity> predicate = marker -> marker.getCommandTags().contains("hopperCooldownVisualizer");
            world.collectEntitiesByType(EntityType.TEXT_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
            
            List<DisplayEntity.BlockDisplayEntity> entities = new ArrayList<>();
            Predicate<DisplayEntity.BlockDisplayEntity> predicate = bd -> bd.getCommandTags().contains("randomTickVisualizer");
            world.collectEntitiesByType(EntityType.BLOCK_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
        }
    }
}
