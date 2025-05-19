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
//#if MC >= 12102
//$$ import org.spongepowered.asm.mixin.Unique;
//#endif

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.morphMovingPiston;

@Mixin(PistonExtensionBlock.class)
public abstract class MovingPistionBlockMixin extends BlockWithEntity {
    protected MovingPistionBlockMixin(Settings settings) {
        super(settings);
    }

    //#if MC >= 12102
    //$$ @Unique
    //$$ PistonBlockEntity pistonBlockEntity;
    //#endif

    @Shadow
    @Nullable
    protected abstract PistonBlockEntity getPistonBlockEntity(BlockView world, BlockPos pos);

    @WrapMethod(method = "onUse")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, Operation<ActionResult> original) {
        if (morphMovingPiston) {
            //#if MC < 12102
            PistonBlockEntity
            //#endif
                pistonBlockEntity = this.getPistonBlockEntity(world, pos);
            return pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null ?
                    pistonBlockEntity.getPushedBlock().getBlock().onUse(pistonBlockEntity.getPushedBlock(), world, pos, player, hit) : original.call(state, world, pos, player, hit);
        }
        return original.call(state, world, pos, player, hit);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        //#if MC < 12102
        PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onLandedUpon(world, pistonBlockEntity.getPushedBlock(), pos, entity, fallDistance);
        else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        //#if MC < 12102
        PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getPistonBlockEntity(world, entity.getSteppingPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onEntityCollision(pistonBlockEntity.getPushedBlock(), world, pos, entity);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        //#if MC < 12102
        PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getPistonBlockEntity(world, entity.getSteppingPos());
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            pistonBlockEntity.getPushedBlock().getBlock().onEntityLand(world, entity);
        else {
            super.onEntityLand(world, entity);
        }

    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        PistonBlockEntity pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston)
            return pistonBlockEntity.getPushedBlock().getBlock().getStrongRedstonePower(pistonBlockEntity.getPushedBlock(), world, pos, direction);
        else
            return 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        //#if MC < 12102
        PistonBlockEntity
        //#endif
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

    //#if MC < 12102
    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {

        //#if MC < 12102
        PistonBlockEntity
        //#endif
            pistonBlockEntity = this.getPistonBlockEntity(world, pos);
        if (pistonBlockEntity != null && pistonBlockEntity.getPushedBlock() != null && morphMovingPiston) {
            return pistonBlockEntity.getPushedBlock().getBlock().isTransparent(pistonBlockEntity.getPushedBlock(), world, pos);
        } else
            return true;
    }
    //#endif

    @WrapMethod(method = "getOutlineShape")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, Operation<VoxelShape> original) {
        if (morphMovingPiston) {
            //#if MC < 12102
            PistonBlockEntity
            //#endif
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
