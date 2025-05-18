package mypals.ml.mixin.features.gridWorldPreset;

import mypals.ml.features.GridWorldGen.FlatGridChunkGenerator;
import mypals.ml.features.GridWorldGen.GridWorldGenerator;
import net.minecraft.client.QuickPlayLogger;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.*;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldPresets.Registrar.class)
public abstract class WorldPresetsRegistrarMixin {
    @Shadow
    protected abstract void register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);

    @Shadow
    @Final
    private RegistryEntryLookup<Biome> biomeLookup;

    @Shadow
    @Final
    private RegistryEntryLookup<StructureSet> structureSetLookup;

    @Shadow
    @Final
    private RegistryEntryLookup<PlacedFeature> featureLookup;

    @Shadow
    protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Inject(method = "<init>",
            at = @At("TAIL"))
    public void bootstrap(CallbackInfo ci) {
        
        this.register(GridWorldGenerator.GRID, this.createOverworldOptions(
                new FlatGridChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(
                        this.biomeLookup, this.structureSetLookup, this.featureLookup
                ))
        ));
        System.out.println("Registered Grid World Preset");
    }
}
