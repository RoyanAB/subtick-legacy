package cn.royan.subtick.mixin;

import cn.royan.subtick.commands.PhaseCommand;
import cn.royan.subtick.commands.TickCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.handler.CommandRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin extends CommandRegistry {
	@Inject(
		method = "<init>",
		at = @At(
			"RETURN"
		)
	)
	private void onRegister(MinecraftServer server, CallbackInfo ci) {
		this.register(new TickCommand());
		this.register(new PhaseCommand());
	}
}
