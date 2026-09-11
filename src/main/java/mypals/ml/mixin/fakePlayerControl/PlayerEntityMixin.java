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

package mypals.ml.mixin.fakePlayerControl;

import mypals.ml.features.fakePlayerControl.FakePlayerControlManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 260300
//$$ import net.minecraft.world.item.component.SwingAnimation;
//#endif

@Mixin(ServerPlayer.class)
public class PlayerEntityMixin {
    // 26.3 replaced ServerPlayer#swing(InteractionHand) (which was swing + resetAttackStrengthTicker,
    // see 26.2 byte code) with swingAndResetAttackStrength, taking the animation from the packet and
    // a flag that only controls whether the swinging player itself is notified as well.
    //#if MC >= 260300
    //$$ @Inject(method = "swingAndResetAttackStrength(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)V", at = @At("HEAD"), cancellable = true)
    //$$ public void swingHand(InteractionHand hand, SwingAnimation animation, boolean broadcastToSelf, CallbackInfo ci) {
    //$$     if (FakePlayerControlManager.binds.containsKey((ServerPlayer) (Object) this)) {
    //$$         FakePlayerControlManager.binds.get((ServerPlayer) (Object) this).getValue().swingAndResetAttackStrength(hand, animation, false);
    //$$     }
    //$$ }
    //#else
    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    public void swingHand(InteractionHand hand, CallbackInfo ci) {
        if (FakePlayerControlManager.binds.containsKey((ServerPlayer) (Object) this)) {
            FakePlayerControlManager.binds.get((ServerPlayer) (Object) this).getValue().swing(hand);
        }
    }
    //#endif
}
