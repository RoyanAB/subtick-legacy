package cn.royan.subtick.mixin;

import cn.royan.subtick.network.ServerNetworkHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.royan.subtick.network.ServerNetworkHandler.addPlayer;
import static cn.royan.subtick.network.ServerNetworkHandler.subTickPlayers;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private ServerPlayerEntity player;


	@Inject(
		method = "onDisconnect",
		at = @At(
			value = "HEAD"
		)
	)
	private void handleDisconnect(CallbackInfo ci) {
		subTickPlayers.remove(player);
	}

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void handleCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		if (packet.getChannel().equals(ServerNetworkHandler.CHANNEL)) {
			addPlayer(player);
			ci.cancel();
		}
	}
}
