package cn.royan.subtick.network;

import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.interfaces.MinecraftServerInterface;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class ServerNetworkHandler {

	public static final String CARPET_CHANNEL = "carpet:hello";

	public static final Set<ServerPlayerEntity> validCarpetPlayers = new HashSet<>();

	public static void updateTickSpeedToConnectedPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : validCarpetPlayers) {
			player.networkHandler.sendPacket(DataBuilder.create(player.server).withTickRate().build());
		}
	}

	public static void updateFrozenStateToConnectedPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : validCarpetPlayers) {
			player.networkHandler.sendPacket(DataBuilder.create(player.server).withFrozenState().build());
		}
	}

	public static void updateSuperHotStateToConnectedPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : validCarpetPlayers) {
			player.networkHandler.sendPacket(DataBuilder.create(player.server).withSuperHotState().build());
		}
	}

	public static void updateTickPlayerActiveTimeoutToConnectedPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : validCarpetPlayers) {
			player.networkHandler.sendPacket(DataBuilder.create(player.server).withTickPlayerActiveTimeout().build());
		}
	}

	private static class DataBuilder {
		private final NbtCompound tag;
		private final MinecraftServer server;

		private DataBuilder(MinecraftServer server) {
			tag = new NbtCompound();
			this.server = server;
		}

		private static DataBuilder create(final MinecraftServer server) {
			return new DataBuilder(server);
		}

		private DataBuilder withTickRate() {
			ServerTickRateManager trm = ((MinecraftServerInterface) server).getTickRateManager();
			tag.putFloat("TickRate", trm.tickrate());
			return this;
		}

		private DataBuilder withFrozenState() {
			ServerTickRateManager trm = ((MinecraftServerInterface) server).getTickRateManager();
			NbtCompound tickingState = new NbtCompound();
			tickingState.putBoolean("is_paused", trm.gameIsPaused());
			tickingState.putBoolean("deepFreeze", trm.deeplyFrozen());
			tag.put("TickingState", tickingState);
			return this;
		}

		private DataBuilder withSuperHotState() {
			ServerTickRateManager trm = ((MinecraftServerInterface) server).getTickRateManager();
			tag.putBoolean("SuperHotState", trm.isSuperHot());
			return this;
		}

		private DataBuilder withTickPlayerActiveTimeout() {
			ServerTickRateManager trm = ((MinecraftServerInterface) server).getTickRateManager();
			tag.putInt("TickPlayerActiveTimeout", trm.getPlayerActiveTimeout());
			return this;
		}

		private CustomPayloadS2CPacket build() {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeNbtCompound(tag);
			return new CustomPayloadS2CPacket(CARPET_CHANNEL, buf);
		}
	}
}
