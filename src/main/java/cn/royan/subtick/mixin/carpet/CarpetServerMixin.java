package cn.royan.subtick.mixin.carpet;

import carpet.CarpetServer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.handler.CommandRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CarpetServer.class)
public class CarpetServerMixin {
	@WrapOperation(
		method = "registerCarpetCommands",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/command/handler/CommandRegistry;register(Lnet/minecraft/server/command/Command;)Lnet/minecraft/server/command/Command;"
		)
	)
	private static Command wrapRegisterCarpetCommands(CommandRegistry instance, Command command, Operation<Command> original) {
		if(command instanceof carpet.commands.TickCommand)
			return null;
		return original.call(instance, command);
	}
}
