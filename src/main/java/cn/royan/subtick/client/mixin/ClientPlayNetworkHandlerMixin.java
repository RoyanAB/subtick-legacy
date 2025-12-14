package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientNetworkHandler;
import cn.royan.subtick.network.ServerNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.nbt.*;
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
		if (packet.getChannel().equals(ServerNetworkHandler.CARPET_CHANNEL)) {
			int value = packet.getData().readInt();
			ClientNetworkHandler.onServerData(packet.getData().readNbtCompound(), minecraft.player);
			ci.cancel();
		}
	}
}
