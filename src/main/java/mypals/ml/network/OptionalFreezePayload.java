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

package mypals.ml.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OptionalFreezePayload(String phase, boolean freeze) implements CustomPacketPayload {
    //#if MC >= 12006
    public static final Type<OptionalFreezePayload> ID = new Type<>(PacketIDs.FREEZE_PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, OptionalFreezePayload> CODEC = StreamCodec.ofMember(
            OptionalFreezePayload::write,
            OptionalFreezePayload::new
    );
    //#else
    //$$ public static final Identifier ID = PacketIDs.FREEZE_PACKET_ID;
    //#endif

    public OptionalFreezePayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean());
    }

    //#if MC < 12006
    //$$ @Override
    //#endif
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(phase);
        buf.writeBoolean(freeze);
    }

    //#if MC >= 12006
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    //#else
    //$$ @Override
    //$$ public Identifier id() {
    //$$     return ID;
    //$$ }
    //#endif
}
