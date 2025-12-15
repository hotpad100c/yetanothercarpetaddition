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

import carpet.api.settings.Rule;
import carpet.api.settings.Validators;
import mypals.ml.settings.RuleValidators.MOVING_PISTON_SPEED_VALIDATOR;

import static carpet.api.settings.RuleCategory.*;

public class YetAnotherCarpetAdditionRules {
    public static final String YACA = "YACA";
    public static final String TREEFARM = "treefarm";
    @Rule(
            categories = {YACA, FEATURE, CREATIVE}
    )
    public static boolean enableTickStepCounter = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean enableMountPlayers = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingWorldBorder = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingWeather = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingTime = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingBlocks = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingFluids = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingRaid = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingChunkManager = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingBlockEvents = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingDragonFight = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopCheckEntityDespawn = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingEntities = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingBlockEntities = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean updateCounter = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean stopTickingSpawners = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean enchantCommandLimitOverwrite = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean enchantCommandBypassItemType = false;
    @Rule(
            categories = {YACA, FEATURE, COMMAND}
    )
    public static boolean mergeSmartAndRegularCommandSuggestions = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean optimizedStructureBlock = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean morphMovingPiston = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL},
            validators = {MOVING_PISTON_SPEED_VALIDATOR.class}
    )
    public static float movingPistonSpeed = 0.5F;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean bedsRecordSleeperFacing = false;
    @Rule(
            categories = {YACA, FEATURE, CREATIVE}
    )
    public static boolean silenceTP = false;
    @Rule(
            categories = {YACA, FEATURE, COMMAND}
    )
    public static String commandEasyItemShadowing = "false";
    @Rule(
            categories = {YACA, FEATURE, COMMAND}
    )
    public static String commandRenameItem = "false";
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean instantSchedule = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean instantFalling = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean copyablePlayerMessages = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean moreHardCollisions = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean farlandReintroduced = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean fallingSnowLayers = false;
    @Rule(
            categories = {YACA, FEATURE, COMMAND}
    )
    public static boolean bypassModifyPlayerDataRestriction = false;
    @Rule(
            categories = {YACA, FEATURE},
            options = {"off", "500", "1000", "6000"},
            strict = false

    )
    public static String hopperCounterDataRecorder = "false";
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean bypassCrashForcibly = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean forceMaxLightLevel = false;
    @Rule(
            categories = {YACA, FEATURE, EXPERIMENTAL}
    )
    public static boolean disableLightUpdate = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean scheduledTickVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean hopperCooldownVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean randomTickVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean gameEventVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean blockEventVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean blockUpdateVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean stateUpdateVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean comparatorUpdateVisualize = false;

    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean blockEntityOrderVisualize = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean POIVisualize = false;

    @Rule(
            categories = {YACA, FEATURE,TREEFARM},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean treeGrowthObstacleVisualize = false;

    @Rule(
            categories = {YACA, FEATURE,TREEFARM},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean foliagePlacerVisualize = false;

    //#if MC < 12102 || MC > 12105
    @Rule(
            categories = {YACA, FEATURE}
    )

    public static boolean waterTNT = false;
    //#endif
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean blocksNoSelfCheck = false;

    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean blocksNoSuffocate = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean blocksPlaceAtAnywhere = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean blockNoBreakParticles = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean blocksNoHardness = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean unicodeArgumentsSupport = false;

    @Rule(
            categories = {YACA, FEATURE},
            options = {"player", "all", "false"}
    )
    public static String commandEnhance = "false";
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean bouncierSlime = false;
    @Rule(
            categories = {YACA, FEATURE, TREEFARM},
            validators = Validators.NonNegativeNumber.class,
            options = {"0.45", "1", "0",},
            strict = false
    )
    public static float bonemealSuccessProbability = 0.45F;
    @Rule(
            categories = {YACA, FEATURE, TREEFARM}
    )
    public static boolean foliageGenerate = true;
    @Rule(
            categories = {YACA, FEATURE, TREEFARM}
//            options = {"-1", "1", "0.01",}
    )
    public static float beeDecoratorProbability = -1;

    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean logInfoToChat = false;

    @Rule(
            categories = {YACA, FEATURE, CLIENT}
    )
    public static boolean copyBlockState = false;
    @Rule(
            categories = {YACA, FEATURE},
            validators = RuleValidators.CLEAR_VISUALIZER.class
    )
    public static boolean mobAIVisualize = false;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean autoDust = false;
    @Rule(
            categories = {YACA, FEATURE},
            options = {"off", "minecraft:white_stained_glass;minecraft:black_stained_glass;1", "minecraft:white_concrete;minecraft:gray_concrete;1"},
            strict = false,
            validators = {RuleValidators.GRID_WORLD_SETTINGS_VALIDATOR.class}
    )
    public static String chessboardSuperFlatSettings = "off";
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean RconOutputFix = true;
    @Rule(
            categories = {YACA, FEATURE}
    )
    public static boolean allowIllegalBlockEntities = false;
}
