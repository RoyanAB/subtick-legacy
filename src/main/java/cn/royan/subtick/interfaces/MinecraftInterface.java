package cn.royan.subtick.interfaces;

import cn.royan.subtick.helpers.TickRateManager;

import java.util.Optional;

public interface MinecraftInterface {
	Optional<TickRateManager> getTickRateManager();
}
