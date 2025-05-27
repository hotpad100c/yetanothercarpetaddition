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

package mypals.ml.mixin.network;

import com.google.common.collect.ImmutableMap;
import mypals.ml.network.client.RequestCountersPayload;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.network.server.CountersPacketPayload;
import mypals.ml.network.server.RulesPacketPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;


@Mixin(CustomPayloadC2SPacket.class)
public abstract class CustomPayloadC2SPacketMixin {
    @Mutable
    @Shadow @Final private static Map<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>> ID_TO_READER;

    static {
        ID_TO_READER = ImmutableMap.<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>>builder()
                .putAll(ID_TO_READER)
                .put(RulesPacketPayload.ID, RulesPacketPayload::new)
                .put(CountersPacketPayload.ID, CountersPacketPayload::new)
                .put(RequestRulesPayload.ID, RulesPacketPayload::new)
                .put(RequestCountersPayload.ID, RequestCountersPayload::new)
                .build();
    }

    @Inject(
            method = "readPayload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;readUnknownPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/UnknownCustomPayload;")
    )
    private static void readCustomPayload(Identifier identifier, PacketByteBuf buf, CallbackInfoReturnable<CustomPayload> cir) {
        if (identifier == RulesPacketPayload.ID) {
            cir.setReturnValue(new RequestRulesPayload(buf));
        } else if (identifier == CountersPacketPayload.ID) {
            cir.setReturnValue(new RequestCountersPayload(buf));
        }
    }
}
