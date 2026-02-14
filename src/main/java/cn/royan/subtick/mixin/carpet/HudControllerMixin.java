package cn.royan.subtick.mixin.carpet;

import carpet.log.framework.HudController;
import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.interfaces.ITickHandleable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HudController.class)
public class HudControllerMixin {
	@ModifyVariable(
		method = "send_tps_display",
		at = @At(
			value = "STORE"
		),
		name = "TPS",
		remap = false
	)
	private static double onSendTps(double TPS, @Local(argsOnly = true) MinecraftServer server, @Local(name = "MSPT") double MSPT) {
		ServerTickRateManager serverTickRateManager = ((ITickHandleable) server).tickHandler().serverTickRateManager;
		return 1000.0D / Math.max(serverTickRateManager.isInWarpSpeed() ? 0.0 : serverTickRateManager.mspt(), MSPT);
	}
}
