package cn.royan.subtick.client.config;

import com.google.common.collect.ImmutableList;
import malilib.gui.BaseScreen;
import malilib.gui.config.BaseConfigScreen;
import malilib.gui.config.BaseConfigTab;
import malilib.gui.config.ConfigTab;
import malilib.util.data.ModInfo;

public class GuiConfig {
	public static final ModInfo MOD_INFO = Configs.MOD_INFO;

	private static final BaseConfigTab GENERIC = new BaseConfigTab(MOD_INFO, "generic", 160, Configs.GENERAL.getConfigOptions(), GuiConfig::create);

	public static final ImmutableList<ConfigTab> CONFIG_TABS = ImmutableList.of(
		GENERIC
	);

	public static BaseScreen create() {
		// The parent screen should not be set here, to prevent infinite recursion via
		// the call to the parent's setWorldAndResolution -> initScreen -> switch tab -> etc.
		return BaseConfigScreen.withExtensionModTabs(MOD_INFO, CONFIG_TABS, GENERIC,
			"Subtick", "subtick");
	}
}
