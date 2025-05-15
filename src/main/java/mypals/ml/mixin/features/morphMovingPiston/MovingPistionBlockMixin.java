package mypals.ml.mixin.features.morphMovingPiston;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.*;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.morphMovingPiston;

@Mixin(PistonExtensionBlock.class)
public abstract class MovingPistionBlockMixin extends BlockWithEntity {
    protected MovingPistionBlockMixin(Settings settings) {
        super(settings);
    }

    @Unique
    PistonBlockEntity pistonBlockEntity;

    @Shadow
    @Nullable
    protected abstract PistonBlockEntity getPistonBlockEntity(BlockView world, BlockPos pos);

    @WrapMethod(method = "onUse")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, Operation<ActionResult> original) {
        if (morphMovingPiston) {
            pistonBlockEntity = this.getPistonBlockEntity(world, pos);
            return pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null ?
                    pistonBlockEntity.getPushedBlock().getBlock().onUse(pistonBlockEntity.getPushedBlock(), world, pos, player, hit) : original.call(state, world, pos, player, hit);
        }
        return original.call(state, world, pos, player, hit);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onLandedUpon(world, pistonBlockEntity.getPushedBlock(), pos, entity, fallDistance);
        else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        pistonBlockEntity = this.getPistonBlockEntity(world, entity.getSteppingPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onEntityCollision(pistonBlockEntity.getPushedBlock(), world, pos, entity);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        pistonBlockEntity = this.getPistonBlockEntity(world, entity.getSteppingPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onEntityLand(world, entity);
        else {
            super.onEntityLand(world, entity);
        }

    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            return pistonBlockEntity.getPushedBlock().getBlock().getStrongRedstonePower(pistonBlockEntity.getPushedBlock(), world, pos, direction);
        else
            return 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            return pistonBlockEntity.getPushedBlock().getBlock().getWeakRedstonePower(pistonBlockEntity.getPushedBlock(), world, pos, direction);
        else
            return 0;
    }

    /*@Override
    protected boolean emitsRedstonePower(BlockState state) {
        return morphMovingPiston;
    }*/

    @Override
    public boolean isTransparent(BlockState state) {
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston) {
            return pistonBlockEntity.getPushedBlock().getBlock().isTransparent(pistonBlockEntity.getPushedBlock());
        } else
            return true;
    }

    @WrapMethod(method = "getOutlineShape")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, Operation<VoxelShape> original) {
        if (morphMovingPiston) {
            pistonBlockEntity = this.getPistonBlockEntity(world, pos);
            return pistonBlockEntity.getCollisionShape(world, pos) != null ? pistonBlockEntity.getCollisionShape(world, pos) : VoxelShapes.empty();
        }
        return VoxelShapes.empty();
    }


    @WrapMethod(method = "getPickStack")
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, Operation<ItemStack> original) {
        if (morphMovingPiston) {
            if (getPistonBlockEntity(world, pos) != null && getPistonBlockEntity(world, pos).getPushedBlock() != null)
                return getPistonBlockEntity(world, pos).getPushedBlock().getBlock().getPickStack(world, pos, getPistonBlockEntity(world, pos).getPushedBlock());
            else return ItemStack.EMPTY;
        }
        return original.call(world, pos, state);
    }
}
