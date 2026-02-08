package cn.royan.subtick.network;

import cn.royan.subtick.client.ClientNetworkHandler;
import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.interfaces.ITickHandler;
import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.utils.TickPhase;
import cn.royan.subtick.utils.Translations;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ServerNetworkHandler {

	public static final String CHANNEL = "subtick:hello";
	public static final Set<ServerPlayerEntity> subTickPlayers = new HashSet<>();

	public static void addPlayer(ServerPlayerEntity playerEntity) {
		subTickPlayers.add(playerEntity);
		ITickHandler tickHandler = ((ITickHandleable) playerEntity.getServer()).tickHandler();
		sendFrozen(playerEntity, tickHandler.frozen(), tickHandler.currentPhase());
	}

	private static boolean tryClient(ServerWorld level, NbtCompound tag) {
		if (level.server.isDedicated())
			return false;

		Minecraft minecraft = Minecraft.getInstance();
		ClientNetworkHandler.onServerData(tag, minecraft.player);
		return true;
	}

	public static void sendNbt(ServerPlayerEntity player, NbtCompound tag, CommandSource actor) {
		ServerWorld level = player.getServerWorld();
		if (tryClient(level, tag))
			return;

		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
		packetBuf.writeNbtCompound(tag);

		try {
			player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CHANNEL, packetBuf));
		} catch (IllegalArgumentException e) {
			Translations.m(actor, "queueCommand.err.packetSize");
		}
	}

	public static void sendNbt(ServerPlayerEntity player, NbtCompound tag) {
		ServerWorld level = player.getServerWorld();
		if (tryClient(level, tag))
			return;

		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
		packetBuf.writeNbtCompound(tag);

		try {
			player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CHANNEL, packetBuf));
		} catch (IllegalArgumentException e) {
		}
	}

	public static void sendNbt(ServerWorld level, NbtCompound tag, CommandSource actor) {
		if (tryClient(level, tag))
			return;

		for (ServerPlayerEntity player : subTickPlayers) {
			if (player.getServerWorld() != level) continue;

			sendNbt(player, tag, actor);
		}
	}

	public static void sendNbt(ServerWorld level, NbtCompound tag) {
		if (tryClient(level, tag))
			return;

		for (ServerPlayerEntity player : subTickPlayers) {
			if (player.getServerWorld() != level) continue;

			sendNbt(player, tag);
		}
	}

	public static void sendNbt(NbtCompound tag) {
		for (ServerPlayerEntity player : subTickPlayers) {
			sendNbt(player, tag);
		}
	}

	public static void updateTickSpeedToConnectedPlayers(MinecraftServer server) {
		ServerTickRateManager trm = ((ITickHandleable) server).tickHandler().serverTickRateManager;
		NbtCompound tag = new NbtCompound();
		NbtCompound rate = new NbtCompound();
		rate.putFloat("rate", trm.tickrate());
		tag.put("TickRate", rate);
		sendNbt(tag);
	}

	public static void sendFrozen(ServerWorld level, TickPhase tickPhase) {
		NbtCompound tag = new NbtCompound();
		NbtCompound tickingState = new NbtCompound();
		tickingState.putBoolean("is_paused", true);
		tickingState.putBoolean("deepFreeze", true);
		tickingState.putInt("phase", tickPhase.phase);
		tickingState.putInt("dim", tickPhase.dim);
		NbtList listTag = new NbtList();
		for (String dim : TickPhase.getDimensions()) {
			// Why is NBT like this? i know you can make a list of StringTags, but there seems to be no way of doing it in mojang code
			NbtCompound element = new NbtCompound();
			element.putString("d", dim);
			listTag.addElement(element);
		}
		tickingState.put("dims", listTag);
		tag.put("TickingState", tickingState);
		sendNbt(level, tag);
	}

	public static void sendFrozen(ServerPlayerEntity player, boolean frozen, TickPhase tickPhase) {
		if (!subTickPlayers.contains(player))
			return;

		NbtCompound tag = new NbtCompound();
		NbtCompound tickingState = new NbtCompound();
		if (frozen) {
			tickingState.putBoolean("is_paused", true);
			tickingState.putBoolean("deepFreeze", true);
			tickingState.putInt("phase", tickPhase.phase);
			tickingState.putInt("dim", tickPhase.dim);
		} else {
			tickingState.putBoolean("is_paused", false);
			tickingState.putBoolean("deepFreeze", false);
			tickingState.putInt("phase", -1);
			tickingState.putInt("dim", -1);
		}
		NbtList listTag = new NbtList();
		for (String dim : TickPhase.getDimensions()) {
			NbtCompound element = new NbtCompound();
			element.putString("d", dim);
			listTag.addElement(element);
		}
		tickingState.put("dims", listTag);
		tag.put("TickingState", tickingState);
		sendNbt(player, tag);
	}

	public static void sendUnfrozen(ServerWorld level) {
		NbtCompound tag = new NbtCompound();
		NbtCompound tickingState = new NbtCompound();
		tickingState.putBoolean("is_paused", false);
		tickingState.putBoolean("deepFreeze", false);
		tickingState.putInt("phase", -1);
		tickingState.putInt("dim", -1);
		tag.put("TickingState", tickingState);
		sendNbt(level, tag);
	}

	public static void sendTickStep(ServerWorld level, int ticks, TickPhase tickPhase) {
		if (ticks != 0) {
			NbtCompound tag = new NbtCompound();
			tag.putInt("TickPlayerActiveTimeout", ticks + 2);
			sendNbt(level, tag);
		}

		NbtCompound tag = new NbtCompound();
		NbtCompound phaseTag = new NbtCompound();
		phaseTag.putInt("dim", tickPhase.dim);
		phaseTag.putInt("phase", tickPhase.phase);
		tag.put("TickPhase", phaseTag);
		sendNbt(level, tag);
	}

	public static void sendQueueStep(ObjectLinkedOpenHashSet<QueueElement> queue, ArrayList<QueueElement> spentQueue, int newQueueElementsCount, int steps, ServerWorld level, CommandSource actor) {
		if (queue.isEmpty() && spentQueue.isEmpty())
			return;

		NbtCompound tag = new NbtCompound();
		NbtCompound queueTag = new NbtCompound();
		NbtList list = new NbtList();
		for (QueueElement element : spentQueue) {
			NbtCompound elementTag = new NbtCompound();
			elementTag.putString("s", element.label);
			elementTag.putInt("x", element.x);
			elementTag.putInt("y", element.y);
			elementTag.putInt("z", element.z);
			elementTag.putInt("d", element.depth);
			list.addElement(elementTag);
		}
		for (QueueElement element : queue) {
			NbtCompound elementTag = new NbtCompound();
			elementTag.putString("s", element.label);
			elementTag.putInt("x", element.x);
			elementTag.putInt("y", element.y);
			elementTag.putInt("z", element.z);
			elementTag.putInt("d", element.depth);
			list.addElement(elementTag);
		}
		queueTag.put("queue", list);
		queueTag.putInt("steps", steps);
		queueTag.putInt("newElements", newQueueElementsCount);
		tag.put("QueueStep", queueTag);
		sendNbt(level, tag, actor);
	}

	public static void sendQueue(ObjectLinkedOpenHashSet<QueueElement> queue, ArrayList<QueueElement> spentQueue, ServerWorld level) {
		if (queue.isEmpty())
			return;

		NbtCompound tag = new NbtCompound();
		NbtList list = new NbtList();
		for (QueueElement element : spentQueue) {
			NbtCompound elementTag = new NbtCompound();
			elementTag.putString("s", element.label);
			elementTag.putInt("x", element.x);
			elementTag.putInt("y", element.y);
			elementTag.putInt("z", element.z);
			elementTag.putInt("d", element.depth);
			list.addElement(elementTag);
		}
		for (QueueElement element : queue) {
			NbtCompound elementTag = new NbtCompound();
			elementTag.putString("s", element.label);
			elementTag.putInt("x", element.x);
			elementTag.putInt("y", element.y);
			elementTag.putInt("z", element.z);
			elementTag.putInt("d", element.depth);
			list.addElement(elementTag);
		}
		tag.put("Queue", list);
		sendNbt(level, tag);
	}
}
