package mypals.ml.features.visualizingFeatures;

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
}
