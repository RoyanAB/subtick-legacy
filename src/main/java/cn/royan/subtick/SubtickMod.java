package cn.royan.subtick;

import cn.royan.subtick.network.ServerNetworkHandler;
import cn.royan.subtick.utils.Translations;
import net.minecraft.server.MinecraftServer;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.lifecycle.api.server.MinecraftServerEvents;
import net.ornithemc.osl.networking.api.server.ServerConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtickMod implements ModInitializer {

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("Example Mod");

	@Override
	public void init() {
		ServerConnectionEvents.LOGIN.register((a, b) -> {
			ServerNetworkHandler.validCarpetPlayers.add(b);
		});
		MinecraftServerEvents.READY.register((a) -> {
			Translations.updateLanguage("en_us");
		});
	}
}
