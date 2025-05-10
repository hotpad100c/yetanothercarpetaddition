package mypals.ml.features.visualizingFeatures;

import mypals.ml.YetAnotherCarpetAdditionServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;


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
        translation.add(NbtFloat.of(0));
        translation.add(NbtFloat.of(0));
        translation.add(NbtFloat.of(0));
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

    public static void clearVisualizersInServer(MinecraftServer server, String target) {
        for (ServerWorld world : server.getWorlds()) {
            clearWorldVisualizers(world, target);
        }
    }

    public static float mapSize(int timeLeft, int originalMax, float targetMax) {
        float mapped = timeLeft * (targetMax / (float) originalMax);
        return mapped;
    }


    public static void clearWorldVisualizers(ServerWorld world, String target) {
        if (world != null) {
            List<DisplayEntity.TextDisplayEntity> entitiesText = new ArrayList<>();
            Predicate<DisplayEntity.TextDisplayEntity> predicate = marker -> marker.getCommandTags().contains(target);
            world.collectEntitiesByType(EntityType.TEXT_DISPLAY,
                    predicate,
                    entitiesText);
            entitiesText.forEach(Entity::discard);

            List<DisplayEntity.BlockDisplayEntity> entitiesBlock = new ArrayList<>();
            Predicate<DisplayEntity.BlockDisplayEntity> predicate2 = bd -> bd.getCommandTags().contains(target);
            world.collectEntitiesByType(EntityType.BLOCK_DISPLAY,
                    predicate2,
                    entitiesBlock);
            entitiesBlock.forEach(Entity::discard);
        }
    }
}
