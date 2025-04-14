package mypals.ml.settings;

import carpet.CarpetSettings;
import carpet.api.settings.Rule;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import carpet.api.settings.Validators;
import carpet.utils.Messenger;
import static carpet.api.settings.RuleCategory.*;

public class YetAnotherCarpetAdditionRules {
	public static final String YACA = "YetAnotherCarpetAddition";
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
}