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

package mypals.ml.network.client;

import mypals.ml.network.PacketIDs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record  RequestRulesPayload(String lang) implements CustomPayload {
    public static final Id<RequestRulesPayload> ID = new CustomPayload.Id<>(PacketIDs.REQUEST_RULES_ID);
    public static final PacketCodec<PacketByteBuf, RequestRulesPayload> CODEC = PacketCodec.of(
            (value, buf) ->
                    buf.writeString(value.lang)
            ,buf -> new RequestRulesPayload(
                    buf.readString()
            ));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
