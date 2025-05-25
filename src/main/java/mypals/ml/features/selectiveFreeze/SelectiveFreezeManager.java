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

package mypals.ml.features.selectiveFreeze;

public class SelectiveFreezeManager {
    public boolean stopTickingWorldBorder = false;
    public boolean stopTickingWeather = false;
    public boolean stopTickingTime = false;
    public boolean stopTickingTileBlocks = false;
    public boolean stopTickingTileFluids = false;
    public boolean stopTickingTileTick = false;
    public boolean stopTickingRaid = false;
    public boolean stopTickingChunkManager = false;
    public boolean stopTickingBlockEvents = false;
    public boolean stopTickingDragonFight = false;
    public boolean stopCheckEntityDespawn = false;
    public boolean stopTickingEntities = false;
    public boolean stopTickingBlockEntities = false;
    public boolean stopTickingSpawners = false;

    public static int entitiesTickSpeed = 5;
    public static int blockEntitiesTickSpeed = 20;
    public static int blockEventTickSpeed = 20;
    public static int tileTickSpeed = 20;
    public static int tileBlockTickSpeed = 20;
    public static int tileFluidSpeed = 20;
    public static int globalTimeSpeed = 20;
}
