package cn.royan.subtick.mixin.tick.freeze;

import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.interfaces.MinecraftServerInterface;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerInterface {

	@Unique
	private ServerTickRateManager serverTickRateManager;

	@Inject(
		method = "<init>",
		at = @At(
			"RETURN"
		)
	)
	private void onInit(CallbackInfo ci) {
		serverTickRateManager = new ServerTickRateManager((MinecraftServer) (Object) this);
	}

	@Unique
	@Override
	public ServerTickRateManager getTickRateManager() {
		return serverTickRateManager;
	}

	@Unique
	public ServerTickRateManager tickRateManager() {
		return this.getTickRateManager();
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;tickWorlds()V",
			shift = At.Shift.BEFORE,
			ordinal = 0
		)
	)
	private void onTick(CallbackInfo ci) {
		tickRateManager().tick();
	}
}
