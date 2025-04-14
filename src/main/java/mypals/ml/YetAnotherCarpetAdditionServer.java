package mypals.ml;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.api.settings.RuleHelper;
import carpet.logging.LoggerRegistry;
import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.commands.YetAnotherCarpetAdditionCommands;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.translations.YetAnotherCarpetAdditionTranslations;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YetAnotherCarpetAdditionServer implements ModInitializer, CarpetExtension {
	public static final String MOD_NAME = "YetAnotherCarpetAddition";
	public static final String MOD_ID = MOD_NAME.toLowerCase();

	public static final String MOD_VERSION = "V1.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public String version(){
		return MOD_VERSION;
	}
	public String modID(){
		return MOD_ID;
	}
	public static void loadExtension(){
		CarpetServer.manageExtension(new YetAnotherCarpetAdditionServer());
	}

	@Override
	public void onInitialize() {
		loadExtension();
		StepManager.reset();
		ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID,
				(payload, context) -> {
			context.server().execute(() -> {

			});
		});
	}
	public List<RuleData> getRules(ServerWorld serverWorld){
		List<RuleData> rules = new ArrayList<>();
        CarpetServer.settingsManager.getCarpetRules().forEach(rule -> {
			if (rule instanceof RuleData) {

				rules.add(
						new RuleData(rule.name(),rule.defaultValue(),
						rule.value(), RuleHelper.translatedDescription(rule),
						rule.suggestions().stream().toList(),rule.categories().stream().toList(),
						rule.type())
				);
			}
		});
		return rules;
	}
	@Override
	public void registerLoggers(){
		LoggerRegistry.registerLoggers();
	}
	@Override
	public void onGameStarted(){
		LOGGER.info(MOD_NAME + " loaded.");
		CarpetServer.settingsManager.parseSettingsClass(YetAnotherCarpetAdditionRules.class);
	}
	@Override
	public void onServerLoaded(MinecraftServer server){

	}
	@Override
	public void onServerClosed(MinecraftServer server){

	}
	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandBuildContext)
	{
		YetAnotherCarpetAdditionCommands.register(dispatcher, commandBuildContext, CommandManager.RegistrationEnvironment.DEDICATED);
	}
	@Override
	public Map<String, String> canHasTranslations(String lang)
	{
		return YetAnotherCarpetAdditionTranslations.getTranslations(lang);
	}
}