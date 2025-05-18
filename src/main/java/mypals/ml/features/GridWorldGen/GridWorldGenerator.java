package mypals.ml.features.GridWorldGen;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;

import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class GridWorldGenerator {
    public static final RegistryKey<WorldPreset> GRID = GridWorldGenerator.of("grid");

    public static RegistryKey<WorldPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of(MOD_ID, id));
    }

    public static void init() {

    }

}
