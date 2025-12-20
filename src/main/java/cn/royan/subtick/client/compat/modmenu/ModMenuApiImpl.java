package cn.royan.subtick.client.compat.modmenu;

import cn.royan.subtick.client.config.GuiConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import malilib.gui.BaseScreen;

public class ModMenuApiImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (currentScreen) -> {
			BaseScreen screen = GuiConfig.create();
			screen.setParent(currentScreen);
			return screen;
		};
	}
}
