package mypals.ml.features.batterCommands;


import net.minecraft.nbt.NbtCompound;

public class DataModifyCapture {
    private static final ThreadLocal<NbtCompound> originalNbt = new ThreadLocal<>();

    public static void setOriginalNbt(NbtCompound nbt) {
        originalNbt.set(nbt);
    }

    public static NbtCompound getOriginalNbt() {
        return originalNbt.get();
    }
}
