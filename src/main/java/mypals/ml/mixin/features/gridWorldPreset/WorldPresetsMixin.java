package mypals.ml.mixin.features.gridWorldPreset;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.features.GridWorldGen.FlatGridChunkGenerator;
import mypals.ml.features.GridWorldGen.GridWorldGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Optional;

import static net.minecraft.world.gen.WorldPresets.DEBUG_ALL_BLOCK_STATES;
import static net.minecraft.world.gen.WorldPresets.FLAT;
import static net.minecraft.world.gen.WorldPresets.DEFAULT;

@Mixin(WorldPresets.class)
public class WorldPresetsMixin {
    /*@WrapMethod(method = "getWorldPreset")
    private static Optional<RegistryKey<WorldPreset>> getWorldPreset(DimensionOptionsRegistryHolder registry, Operation<Optional<RegistryKey<WorldPreset>>> original) {
        return registry.getOrEmpty(DimensionOptions.OVERWORLD).flatMap(overworld -> switch (overworld.chunkGenerator()) {
            case FlatGridChunkGenerator flatGridChunkGenerator -> Optional.of(GridWorldGenerator.GRID);
            case FlatChunkGenerator flatChunkGenerator -> Optional.of(FLAT);
            case DebugChunkGenerator debugChunkGenerator -> Optional.of(DEBUG_ALL_BLOCK_STATES);
            case NoiseChunkGenerator noiseChunkGenerator -> Optional.of(DEFAULT);
            default -> Optional.empty();
        });
    }*/

}
