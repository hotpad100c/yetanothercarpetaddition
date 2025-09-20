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

package mypals.ml.mixin.features.chokolatekiller;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonMixin extends MobEntity {
    /*@Shadow
    @Final
    public EnderDragonPart head;

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract boolean damagePart(EnderDragonPart part, DamageSource source, float amount);

    @Shadow
    protected abstract boolean parentDamage(DamageSource source, float amount);

    @Shadow
    @Final
    private PhaseManager phaseManager;

    @Shadow
    private float damageDuringSitting;
*/
    protected EnderDragonMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
/*
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == Items.COOKIE || itemStack.getItem() == Items.COCOA_BEANS) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 900, 5), player);
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                this.damagePart(this.head, this.getDamageSources().playerAttack(player), 20.0F);
                if (player == null || !player.isInCreativeMode()) {
                    itemStack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Inject(
            method = "damagePart",
            at = @At(value = "HEAD")
    )
    public void damagePart(EnderDragonPart part, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getTypeRegistryEntry() == DamageTypes.MAGIC) {
            float f = this.getHealth();
            this.parentDamage(source, amount);
            if (this.isDead() && !this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.setHealth(1.0F);
                this.phaseManager.setPhase(PhaseType.DYING);
            }

            if (this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.damageDuringSitting = this.damageDuringSitting + f - this.getHealth();
                if (this.damageDuringSitting > 0.25F * this.getMaxHealth()) {
                    this.damageDuringSitting = 0.0F;
                    this.phaseManager.setPhase(PhaseType.TAKEOFF);
                }
            }
        }
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (effect.getEffectType() == StatusEffects.POISON) {
            return super.addStatusEffect(effect, source);
        } else {
            return false;
        }
    }*/
}
