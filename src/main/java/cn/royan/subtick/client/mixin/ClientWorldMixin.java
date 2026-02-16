package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientTickHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@Inject(
		method = "tick",
		at = @At(
			"TAIL"
		)
	)
	private void onTick(CallbackInfo ci) {
		ClientTickHandler.onTick((ClientWorld) (Object) this);
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/world/ClientWorld;setTime(J)V"
		)
	)
	public boolean wrapClientWorldTimeUpdate(ClientWorld instance, long l) {
		return !ClientTickHandler.frozen;
	}

	@ModifyExpressionValue(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z",
			ordinal = 0
		)
	)
	private boolean wrapClientDayTimeUpdate(boolean original) {
		if (!ClientTickHandler.frozen) {
			return original;
		}
		return false;
	}
}
