package cn.royan.subtick.client.mixin;

import cn.royan.subtick.client.ClientTickHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.Tickable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldMixin {
	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/entity/Entity;time:I",
			opcode = 181 /* PUTFIELD */
		)
	)
	public boolean disableGlobalEntityTick0(Entity instance, int value) {
		return ClientTickHandler.shouldTick() || instance instanceof PlayerEntity;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;tick()V"
		)
	)
	public boolean disableGlobalEntityTick1(Entity instance) {
		return ClientTickHandler.shouldTick() || instance instanceof PlayerEntity;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"
		)
	)
	public boolean disableRegularEntityTick(World instance, Entity entity) {
		return ClientTickHandler.shouldTick() || entity instanceof PlayerEntity;
	}

	@WrapWithCondition(
		method = "tickEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Tickable;tick()V"
		)
	)
	public boolean disableRegularBlockEntityTick(Tickable instance) {
		return !ClientTickHandler.skip_block_entities && ClientTickHandler.shouldTick();
	}
}
