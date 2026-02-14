package cn.royan.subtick;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import cn.royan.subtick.setting.CarpetSettings;
import cn.royan.subtick.utils.Translations;
import net.minecraft.server.command.handler.CommandRegistry;

import java.util.Map;

public class SubtickCarpetExtension implements CarpetExtension {
	@Override
	public String version() {
		return SubtickMod.MOD_ID;
	}

	public static void loadExtension() {
		CarpetServer.manageExtension(new SubtickCarpetExtension());
	}

	@Override
	public void onGameStarted() {
		CarpetServer.settingsManager.parseSettingsClass(CarpetSettings.class);
	}

	@Override
	public void registerCommands(CommandRegistry registry) {

	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		return Translations.getTranslationFromResourcePath(lang);
	}
}
