package cn.royan.subtick;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import cn.royan.subtick.commands.PhaseCommand;
import cn.royan.subtick.commands.QueueCommand;
import cn.royan.subtick.commands.TickCommand;
import cn.royan.subtick.utils.Translations;
import cn.royan.subtick.utils.deobfuscator.StackTraceDeobfuscator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.handler.CommandRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SubtickMod implements CarpetExtension {
	public static final Logger LOGGER = LogManager.getLogger("subtick");

	public static final String MOD_ID = "subtick";
	public static String MOD_VERSION = "unknown";
	public static String MOD_NAME = "unknown";

	@Override
	public String version() {
		return MOD_ID;
	}

	public static void loadExtension() {
		// add to carpet's extension list
		CarpetServer.manageExtension(new SubtickMod());
	}

	@Override
	public void onGameStarted() {
		// let carpet handle the settings
		ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
		MOD_NAME = metadata.getName();
		MOD_VERSION = metadata.getVersion().getFriendlyString();
		StackTraceDeobfuscator.fetchMapping();
		CarpetServer.settingsManager.parseSettingsClass(SubtickSettings.class);
	}

	@Override
	public void registerCommands(CommandRegistry registry) {
		// register commands here
		registry.register(new TickCommand());
		registry.register(new PhaseCommand());
		registry.register(new QueueCommand());
	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		return Translations.getTranslationFromResourcePath(lang);
	}
}
