package cn.royan.subtick.client;

import cn.royan.subtick.client.config.ConfigHandler;
import cn.royan.subtick.client.config.Configs;
import malilib.registry.Registry;
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;

public class SubtickClientMod implements ClientModInitializer {
	@Override
	public void initClient() {
		new Configs();
		Registry.CONFIG_MANAGER.registerConfigHandler(new ConfigHandler());
	}
}
