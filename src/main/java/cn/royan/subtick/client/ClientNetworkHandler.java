package cn.royan.subtick.client;

import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientNetworkHandler {

	private static final Map<String, BiConsumer<LocalClientPlayerEntity, NbtElement>> dataHandlers = new HashMap<>();

	static  {
		dataHandlers.put("TickRate", (p, t) ->
		{
//			ClientTickHandler.setFreeze((CompoundTag) t);
		});

		dataHandlers.put("TickingState", (p, t) ->
		{
//			ClientTickHandler.setFreeze((CompoundTag) t);
		});

		dataHandlers.put("TickPhase", (p, t) ->
		{
//			ClientTickHandler.setPhase(new TickPhase((CompoundTag) t));
		});

		dataHandlers.put("TickPlayerActiveTimeout", (p, t) ->
		{
//			ClientTickHandler.scheduleTickStep(((NumericTag) t).getAsInt());
		});

		dataHandlers.put("Queue", (p, t) ->
		{
//			ClientTickHandler.setQueue((ListTag) t);
		});

		dataHandlers.put("QueueStep", (p, t) ->
		{
//			ClientTickHandler.queueStep((CompoundTag) t);
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
