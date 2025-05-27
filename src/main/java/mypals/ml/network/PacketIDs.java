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

import net.minecraft.util.Identifier;

import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class PacketIDs {
    public static final Identifier REQUEST_RULES_ID = Identifier.of(MOD_ID, "request_rules");
    public static final Identifier SYNC_RULES_ID = Identifier.of(MOD_ID, "sync_rules");
    public static final Identifier REQUEST_COUNTERS_DATA_ID = Identifier.of(MOD_ID, "request_counters");
    public static final Identifier SYNC_COUNTERS_DATA_ID = Identifier.of(MOD_ID, "sync_counters");
    public static final Identifier FREEZE_PACKET_ID = Identifier.of(MOD_ID, "freeze");

}
