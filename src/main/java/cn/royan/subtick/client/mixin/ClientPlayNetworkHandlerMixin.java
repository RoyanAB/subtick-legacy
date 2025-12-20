package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientNetworkHandler;
import cn.royan.subtick.network.ServerNetworkHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private Minecraft minecraft;

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void handleCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().equals(ServerNetworkHandler.CHANNEL)) {
			ClientNetworkHandler.onServerData(packet.getData().readNbtCompound(), minecraft.player);
			ci.cancel();
		}
	}

	@Inject(
		method = "handleLogin",
		at = @At(
			value = "TAIL"
		)
	)
	private void handleLogin(CallbackInfo ci) {
		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
		minecraft.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(ServerNetworkHandler.CHANNEL, packetBuf));
	}
}
