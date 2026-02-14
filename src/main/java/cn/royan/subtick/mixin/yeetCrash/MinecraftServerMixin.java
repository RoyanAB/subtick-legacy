package cn.royan.subtick.mixin.yeetCrash;

import cn.royan.subtick.SubtickMod;
import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.utils.Messenger;
import cn.royan.subtick.utils.Translations;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ITickHandleable {
	@WrapMethod(
		method = "tick"
	)
	private void yeetCrashOnTickServer(Operation<Void> original) {
		if (SubtickMod.yeetCrash()) {
			try {
				original.call();
			} catch (Throwable t) {
				Messenger.print_server_message((MinecraftServer) (Object) this, Translations.tr("subtick.feedback.queueCommand.err.crashRunning"));
			}
		} else {
			original.call();
		}
	}
}
