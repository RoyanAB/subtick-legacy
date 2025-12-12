package cn.royan.subtick.mixin.subtick.freeze;

import cn.royan.subtick.interfaces.WorldInterface;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.util.Tickable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldMixin implements WorldInterface {
	@Shadow
	@Final
	public boolean isClient;

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/entity/Entity;time:I",
			opcode = 181 /* PUTFIELD */
		)
	)
	public boolean disableGlobalEntityTick0(Entity instance, int value) {
		if (this.isClient) return this.tickRateManager().shouldEntityTick(instance);
		return true;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;tick()V"
		)
	)
	public boolean disableGlobalEntityTick1(Entity instance) {
		if (this.isClient) return this.tickRateManager().shouldEntityTick(instance);
		return true;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"
		)
	)
	public boolean disableRegularEntityTick(World instance, Entity entity) {
		if (this.isClient) return this.tickRateManager().shouldEntityTick(entity);
		return true;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Tickable;tick()V"
		)
	)
	public boolean disableRegularBlockEntityTick(Tickable instance) {
		if (this.isClient) return this.tickRateManager().runsNormally();
		return true;
	}
}
