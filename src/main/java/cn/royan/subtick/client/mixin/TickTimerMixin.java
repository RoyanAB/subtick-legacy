package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientTickHandler;
import net.minecraft.client.TickTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickTimer.class)
public abstract class TickTimerMixin {
	@Shadow
	private float mspt;

	@Inject(
		method = "advance",
		at = @At(
			"HEAD"
		)
	)
	public void advance(CallbackInfo ci) {
		if (false) {
			if (!ClientTickHandler.frozen) {
				this.mspt = Math.max(50.0f, ClientTickHandler.mspt);
			}
		} else
			this.mspt = 50.0f;
	}
}
