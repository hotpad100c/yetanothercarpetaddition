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

package mypals.ml.settings;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RuleValidators {

    public static class MOVING_PISTON_SPEED_VALIDATOR extends Validator<Float> {

        @Override
        public Float validate(@Nullable ServerCommandSource source, CarpetRule<Float> changingRule, Float newValue, String userInput) {
            return Math.max(0.0f, Math.min(newValue, 1.0f));
        }
    }

    public static class GRID_WORLD_SETTINGS_VALIDATOR extends Validator<String> {
        static String DEFAULT_PRESET = "minecraft:white_stained_glass;minecraft:black_stained_glass;1";

        @Override
        public String validate(@Nullable ServerCommandSource source, CarpetRule<String> changingRule, String newValue, String userInput) {
            String[] parts = newValue.split(";");
            if (newValue.equals("off")) {
                return "off";
            }
            //#if MC < 12105
            //$$if (!Registries.BLOCK.containsId(Identifier.of("minecraft", parts[0].replace("minecraft:", "")))
            //$$        || !Registries.BLOCK.containsId(Identifier.of("minecraft", parts[1].replace("minecraft:", ""))
            //$$))
            //#else
            if (!Registries.BLOCK.containsId(Identifier.ofVanilla(parts[0].replace("minecraft:", "")))
                    || !Registries.BLOCK.containsId(Identifier.ofVanilla(parts[1].replace("minecraft:", ""))
            ))
            //#endif
            {
                if (source != null)
                    source.sendError(Text.of("Invalid block IDs: " + parts[0] + " or " + parts[1]));
                return DEFAULT_PRESET;
            }

            try {
                int value = Integer.parseInt(parts[2]);
                return parts[0].replace("minecraft:", "") + ";" + parts[1].replace("minecraft:", "") + ";" + Math.max(1, value);
            } catch (NumberFormatException e) {
                if (source != null)
                    source.sendError(Text.of("Invalid size value: " + parts[2] + " using default(1)."));
                return parts[0] + ";" + parts[1] + ";1";
            }
        }
    }
}
