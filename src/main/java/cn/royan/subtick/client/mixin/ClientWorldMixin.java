package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientTickHandler;
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
	private void onTick(CallbackInfo ci)
	{
		ClientTickHandler.onTick((ClientWorld) (Object)this);
	}
}
