package cn.royan.subtick.mixin.carpet;

import carpet.log.framework.HudController;
import carpet.utils.Messenger;
import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.helpers.TickHandler;
import cn.royan.subtick.interfaces.ITickHandleable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Locale;

@Mixin(HudController.class)
public class HudControllerMixin {
	/**
	 * @author AB
	 * @reason To fix log tps
	 */
	@Overwrite
	private static Text[] send_tps_display(MinecraftServer server) {
		TickHandler tickHandler = ((ITickHandleable) server).tickHandler();
		ServerTickRateManager serverTickRateManager = tickHandler.serverTickRateManager;
		double MSPT = MathHelper.average(server.averageTickTimes) * 1.0E-6D;
		double TPS = 1000.0D / Math.max(serverTickRateManager.isInWarpSpeed() ? 0.0 : serverTickRateManager.mspt(), MSPT);
		if (tickHandler.frozen()) {
			TPS = 0;
		}
		String color = Messenger.heatmap_color(MSPT, serverTickRateManager.mspt());
		return new Text[]{Messenger.c(
			"g TPS: ", String.format(Locale.US, "%s %.1f", color, TPS),
			"g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT))};
	}
}
