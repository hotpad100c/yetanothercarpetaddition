package mypals.ml.settings;

import carpet.api.settings.Rule;
import static carpet.api.settings.RuleCategory.*;

public class YetAnotherCarpetAdditionRules {
	public static final String YACA = "YACA";
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
}