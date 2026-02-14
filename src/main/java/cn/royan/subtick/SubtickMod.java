package cn.royan.subtick;

import carpet.commands.CarpetAbstractCommand;
import cn.royan.subtick.setting.CarpetSettings;
import cn.royan.subtick.setting.Settings;
import cn.royan.subtick.utils.Translations;
import cn.royan.subtick.utils.deobfuscator.StackTraceDeobfuscator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.source.CommandSource;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtickMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("subtick");

	public static final String MOD_ID = "subtick";
	public static String MOD_VERSION = "unknown";
	public static String MOD_NAME = "unknown";

	public static final boolean hasCarpet = FabricLoader.getInstance().isModLoaded("carpet");

	public static Settings settings;

	@Override
	public void init() {
		ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
		MOD_NAME = metadata.getName();
		MOD_VERSION = metadata.getVersion().getFriendlyString();

		settings = Settings.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("subtick.json"));
		StackTraceDeobfuscator.fetchMapping();

		if (!hasCarpet) {
			Translations.getTranslationFromResourcePath(settings.language);
		}
	}

	public void preLaunch() {
		if (hasCarpet) SubtickCarpetExtension.loadExtension();
	}

	public static boolean tickCommand(CommandSource commandSource, String name) {
		if (hasCarpet)
			return CarpetAbstractCommand.canUseCommand(commandSource, CarpetSettings.tickCommand);
		return commandSource.canUseCommand(2, name);
	}

	;

	public static String subtickDefaultPhase() {
		if (hasCarpet)
			return CarpetSettings.subtickDefaultPhase;
		return settings.subtickDefaultPhase;
	}

	;

	public static String subtickTextFormat() {
		if (hasCarpet)
			return CarpetSettings.subtickTextFormat;
		return settings.subtickTextFormat;
	}

	;


	public static String subtickNumberFormat() {
		if (hasCarpet)
			return CarpetSettings.subtickNumberFormat;
		return settings.subtickNumberFormat;
	}

	;


	public static String subtickPhaseFormat() {
		if (hasCarpet)
			return CarpetSettings.subtickPhaseFormat;
		return settings.subtickPhaseFormat;
	}

	;


	public static String subtickDimensionFormat() {
		if (hasCarpet)
			return CarpetSettings.subtickDimensionFormat;
		return settings.subtickDimensionFormat;
	}

	;


	public static String subtickErrorFormat() {
		if (hasCarpet)
			return CarpetSettings.subtickErrorFormat;
		return settings.subtickErrorFormat;
	}

	;


	public static int subtickDefaultRange() {
		if (hasCarpet)
			return CarpetSettings.subtickDefaultRange;
		return settings.subtickDefaultRange;
	}

	;


	public static boolean yeetCrash() {
		if (hasCarpet)
			return CarpetSettings.yeetCrash;
		return settings.subtickYeetCrash;
	}

	;

}
