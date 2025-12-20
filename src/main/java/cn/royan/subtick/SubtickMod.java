package cn.royan.subtick;

import cn.royan.subtick.utils.Translations;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtickMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("subtick");

	public static final String MOD_ID = "subtick";
	public static String MOD_VERSION = "unknown";
	public static String MOD_NAME = "unknown";

	public static Settings settings;

	@Override
	public void init() {
		ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
		MOD_NAME = metadata.getName();
		MOD_VERSION = metadata.getVersion().getFriendlyString();

		settings = Settings.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("subtick.json"));
		Translations.updateLanguage(settings.language);
	}
}
