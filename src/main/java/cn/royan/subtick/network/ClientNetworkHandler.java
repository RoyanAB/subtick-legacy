package cn.royan.subtick.network;

import cn.royan.subtick.helpers.TickRateManager;
import cn.royan.subtick.interfaces.WorldInterface;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


public class ClientNetworkHandler {
	private static final Map<String, BiConsumer<LocalClientPlayerEntity, NbtElement>> dataHandlers = new HashMap<>();

	static {
		dataHandlers.put("TickRate", (p, t) -> {
			TickRateManager tickRateManager = ((WorldInterface) p.world).tickRateManager();
			tickRateManager.setTickRate(((NbtFloat) t).getFloat());
		});
		dataHandlers.put("TickingState", (p, t) -> {
			NbtCompound tickingState = (NbtCompound) t;
			TickRateManager tickRateManager = ((WorldInterface) p.world).tickRateManager();
			tickRateManager.setFrozenState(tickingState.getBoolean("is_paused"), tickingState.getBoolean("deepFreeze"));
		});
		dataHandlers.put("SuperHotState", (p, t) -> {
			TickRateManager tickRateManager = ((WorldInterface) p.world).tickRateManager();
			tickRateManager.setSuperHot(((NbtByte) t).getByte() == 1);
		});
		dataHandlers.put("TickPlayerActiveTimeout", (p, t) -> {
			TickRateManager tickRateManager = ((WorldInterface) p.world).tickRateManager();
			tickRateManager.setPlayerActiveTimeout(((NbtInt) t).getInt());
		});
	}

	public static void onServerData(NbtCompound compound, LocalClientPlayerEntity player) {
		for (String key : compound.getKeys()) {
			if (dataHandlers.containsKey(key)) {
				dataHandlers.get(key).accept(player, compound.get(key));
			}
		}
	}
}
