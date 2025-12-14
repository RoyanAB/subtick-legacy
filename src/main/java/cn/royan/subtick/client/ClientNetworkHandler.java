package cn.royan.subtick.client;

import cn.royan.subtick.utils.TickPhase;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientNetworkHandler {

	private static final Map<String, BiConsumer<LocalClientPlayerEntity, NbtElement>> dataHandlers = new HashMap<>();

	static  {
		dataHandlers.put("TickRate", (p, t) ->
		{
			ClientTickHandler.setTickRate(((NbtCompound) t).getFloat("rate"));
		});

		dataHandlers.put("TickingState", (p, t) ->
		{
			ClientTickHandler.setFreeze((NbtCompound) t);
		});

		dataHandlers.put("TickPhase", (p, t) ->
		{
			ClientTickHandler.setPhase(new TickPhase((NbtCompound) t));
		});

		dataHandlers.put("TickPlayerActiveTimeout", (p, t) ->
		{
			ClientTickHandler.scheduleTickStep(((NbtInt) t).getInt());
		});

		dataHandlers.put("Queue", (p, t) ->
		{
			ClientTickHandler.setQueue((NbtList) t);
		});

		dataHandlers.put("QueueStep", (p, t) ->
		{
			ClientTickHandler.queueStep((NbtCompound) t);
		});
	}

	public static void onServerData(NbtCompound compound, LocalClientPlayerEntity player) {
		for (String key : compound.getKeys()) {
			if (dataHandlers.containsKey(key)) {
				dataHandlers.get(key).accept(player, compound.get(key));
			}
			System.out.println(compound.toString());
		}
	}
}
