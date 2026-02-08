package cn.royan.subtick.mixin;

import cn.royan.subtick.helpers.TickHandler;
import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.utils.TickPhase;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.EntityMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ITickHandleable {
	@Shadow
	public ServerWorld[] worlds;
	@Unique
	private TickHandler tickHandler;

	@Unique
	@Override
	public TickHandler tickHandler() {
		return this.tickHandler;
	}

	@Inject(
		method = "run",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER,
			target = "Lnet/minecraft/server/MinecraftServer;init()Z"
		)
	)
	private void addDim(CallbackInfo ci) {
		TickPhase.reset();
		for (ServerWorld world : this.worlds)
			TickPhase.addDimension(world);
	}

	@Inject(
		method = "<init>",
		at = @At(
			"RETURN"
		)
	)
	private void onInit(CallbackInfo ci) {
		tickHandler = new TickHandler((MinecraftServer) (Object) this);
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/server/MinecraftServer;ticks:I", opcode = 181 /* PUTFIELD */
		)
	)
	public boolean wrapServerTickUpdate(MinecraftServer instance, int value) {
		return !this.tickHandler().frozen();
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;saveWorlds(Z)V"
		)
	)
	public boolean wrapAutosave(MinecraftServer instance, boolean silent) {
		return !this.tickHandler().frozen();
	}

	@WrapWithCondition(
		method = "tickWorlds",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/EntityMap;tick()V"
		)
	)
	public boolean wrapEntityTracker(EntityMap instance, @Local ServerWorld serverWorld) {
		return tickHandler().shouldTick(serverWorld, TickPhase.ENTITY_TRACKER);
	}
}
