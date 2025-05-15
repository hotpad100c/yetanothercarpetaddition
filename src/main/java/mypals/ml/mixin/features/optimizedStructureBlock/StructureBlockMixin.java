package mypals.ml.mixin.features.optimizedStructureBlock;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Mixin(StructureBlockBlockEntity.class)
public class StructureBlockMixin extends BlockEntity {
    @Shadow
    private StructureBlockMode mode;

    @Shadow
    private BlockPos offset;

    @Shadow
    private Vec3i size;

    public StructureBlockMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapMethod(method = "detectStructureSize")
    public boolean detectStructureSize(Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.optimizedStructureBlock) {
            return original.call();
        } else {
            if (this.mode != StructureBlockMode.SAVE) {
                return false;
            }

            BlockBox blockBox = new BlockBox(this.getPos());

            forEachChunkInCube(this.pos, 80, chunkPos -> {
                this.world.getChunk(chunkPos.x, chunkPos.z).getBlockEntities().forEach((pos, be) -> {
                    if (be instanceof StructureBlockBlockEntity) {
                        blockBox.encompass(pos);
                    }
                });
            });

            int dx = blockBox.getMaxX() - blockBox.getMinX();
            int dy = blockBox.getMaxY() - blockBox.getMinY();
            int dz = blockBox.getMaxZ() - blockBox.getMinZ();

            if (dx > 1 && dy > 1 && dz > 1) {
                this.offset = new BlockPos(
                        blockBox.getMinX() - this.getPos().getX() + 1,
                        blockBox.getMinY() - this.getPos().getY() + 1,
                        blockBox.getMinZ() - this.getPos().getZ() + 1
                );
                this.size = new Vec3i(dx - 1, dy - 1, dz - 1);

                this.markDirty();
                BlockState blockState = this.world.getBlockState(this.getPos());
                this.world.updateListeners(this.getPos(), blockState, blockState, 3);

                return true;
            }

            return false;
        }
    }

    @Unique
    private static void forEachChunkInCube(BlockPos center, int radius, Consumer<ChunkPos> action) {

        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = center.getY() - radius;
        int maxY = center.getY() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                action.accept(new ChunkPos(chunkX, chunkZ));
            }
        }
    }
}
