/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.mixin.features.morphMovingPiston;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
//#if MC >= 12102
import org.spongepowered.asm.mixin.Unique;
//#endif

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.morphMovingPiston;

@Mixin(MovingPistonBlock.class)
public abstract class MovingPistionBlockMixin extends BaseEntityBlock {
    protected MovingPistionBlockMixin(Properties settings) {
        super(settings);
    }

    //#if MC >= 12102
    @Unique
    PistonMovingBlockEntity pistonBlockEntity;
    //#endif

    @Shadow
    @Nullable
    protected abstract PistonMovingBlockEntity getBlockEntity(BlockGetter world, BlockPos pos);

    @WrapMethod(method = "useWithoutItem")
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                              //#if MC <= 12004
                              //$$ Hand hand,
                              //#endif
                              BlockHitResult hit, Operation<InteractionResult> original) {
        if (morphMovingPiston) {
            //#if MC < 12102
            //$$ PistonBlockEntity
            //#endif
                pistonBlockEntity = this.getBlockEntity(world, pos);
            return pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null ?
                    pistonBlockEntity.getMovedState().getBlock().useWithoutItem(pistonBlockEntity.getMovedState(), world, pos, player,
                            //#if MC <= 12004
                            //$$ hand,
                            //#endif
                            hit) : original.call(state, world, pos, player, hit);
        }
        return original.call(state, world, pos, player,
                //#if MC <= 12004
                //$$ hand,
                //#endif
                hit);
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity,
                             //#if MC < 12105
                             //$$ float fallDistance
                             //#else
                             double fallDistance
                             //#endif
    ) {
        //#if MC < 12102
        //$$ PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null && morphMovingPiston)
            pistonBlockEntity.getMovedState().getBlock().fallOn(world, pistonBlockEntity.getMovedState(), pos, entity, fallDistance);
        else {
            super.fallOn(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void entityInside(
            BlockState state, Level world, BlockPos pos, Entity entity
            //#if MC >= 12105
            , InsideBlockEffectApplier entityCollisionHandler
            //#endif
    ) {
        //#if MC < 12102
        //$$ PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getBlockEntity(world, entity.getOnPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null && morphMovingPiston)
            pistonBlockEntity.getMovedState().getBlock().entityInside(
                    pistonBlockEntity.getMovedState(), world, pos, entity
                    //#if MC >= 12105
                    , entityCollisionHandler
                    //#endif
            );
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter world, Entity entity) {
        //#if MC < 12102
        //$$ PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getBlockEntity(world, entity.getOnPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null && morphMovingPiston)
            pistonBlockEntity.getMovedState().getBlock().updateEntityMovementAfterFallOn(world, entity);
        else {
            super.updateEntityMovementAfterFallOn(world, entity);
        }

    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {

        PistonMovingBlockEntity pistonBlockEntity = this.getBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null && morphMovingPiston)
            return pistonBlockEntity.getMovedState().getBlock().getDirectSignal(pistonBlockEntity.getMovedState(), world, pos, direction);
        else
            return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {

        //#if MC < 12102
        //$$ PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getMovedState() != null && morphMovingPiston)
            return pistonBlockEntity.getMovedState().getBlock().getSignal(pistonBlockEntity.getMovedState(), world, pos, direction);
        else
            return 0;
    }

    /*@Override
    protected boolean emitsRedstonePower(BlockState state) {
        return morphMovingPiston;
    }*/

    //#if MC < 12102
    //$$ @Override
    //$$ public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
    //$$
        //#if MC < 12102
        //$$ PistonBlockEntity
        //#endif
    //$$         pistonBlockEntity = this.getPistonBlockEntity(world, pos);
    //$$     if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston) {
    //$$         return pistonBlockEntity.getPushedBlock().getBlock().isTransparent(pistonBlockEntity.getPushedBlock(), world, pos);
    //$$     } else
    //$$         return true;
    //$$ }
    //#endif

    @WrapMethod(method = "getShape")
    public VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        if (morphMovingPiston) {
            //#if MC < 12102
            //$$ PistonBlockEntity
            //#endif
                pistonBlockEntity = this.getBlockEntity(world, pos);
            return pistonBlockEntity.getCollisionShape(world, pos) != null ? pistonBlockEntity.getCollisionShape(world, pos) : Shapes.empty();
        }
        return Shapes.empty();
    }


    @WrapMethod(method = "getCloneItemStack")
    public ItemStack getPickStack(LevelReader world, BlockPos pos, BlockState state,
                                  //#if MC >= 12104
                                  boolean includeData,
                                  //#endif
                                  Operation<ItemStack> original) {
        if (morphMovingPiston) {
            if (getBlockEntity(world, pos) != null && getBlockEntity(world, pos).getMovedState() != null)
                return getBlockEntity(world, pos).getMovedState().getBlock().getCloneItemStack(world, pos, getBlockEntity(world, pos).getMovedState()
                        //#if MC >= 12104
                        , includeData
                        //#endif
                );
            else return ItemStack.EMPTY;
        }
        return original.call(world, pos, state
            //#if MC >= 12104
            , includeData
            //#endif
        );
    }
}
