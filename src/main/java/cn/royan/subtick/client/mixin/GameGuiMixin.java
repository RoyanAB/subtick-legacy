package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.render.HudRenderer;
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class GameGuiMixin {
    @Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target = "Lnet/minecraft/client/gui/GameGui;renderStatusEffects(Lnet/minecraft/client/render/Window;)V"
		)
	)
    private void renderHud(float tickDelta, CallbackInfo ci) {
		HudRenderer.render();
    }
}
