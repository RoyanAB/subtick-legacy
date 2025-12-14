package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.render.LevelRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
		method = "render(IFJ)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/render/GameRenderer;newCamPitch:Z"
		)
	)
	private void render(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci)
	{
		LevelRenderer.render(tickDelta);
	}
}
