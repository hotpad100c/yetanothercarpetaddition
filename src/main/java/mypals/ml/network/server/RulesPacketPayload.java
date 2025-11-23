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

package mypals.ml.network.server;

import mypals.ml.network.PacketIDs;
import mypals.ml.network.RuleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

//#if MC > 12004
import net.minecraft.network.codec.StreamCodec;
//#else
//$$ import net.minecraft.resources.ResourceLocation;
//#endif

public record RulesPacketPayload(List<RuleData> rules, String defaults) implements CustomPacketPayload {
    //#if MC >= 12006
    public static final Type<RulesPacketPayload> ID = new Type<>(PacketIDs.SYNC_RULES_ID);
    public static final StreamCodec<FriendlyByteBuf, RulesPacketPayload> CODEC = StreamCodec.ofMember(RulesPacketPayload::write, RulesPacketPayload::new);
    //#else
    //$$ public static final ResourceLocation ID = PacketIDs.SYNC_RULES_ID;
    //#endif

    public RulesPacketPayload(FriendlyByteBuf buf) {
        this(buf.readList(RuleData::new), buf.readUtf());
    }

    //#if MC < 12006
    //$$ @Override
    //#endif
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.rules(), ((buf1, value) -> value.write(buf1)));
        buf.writeUtf(this.defaults);
    }

    //#if MC >= 12006
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    //#else
    //$$ @Override
    //$$ public ResourceLocation id() {
    //$$     return ID;
    //$$ }
    //#endif
}
