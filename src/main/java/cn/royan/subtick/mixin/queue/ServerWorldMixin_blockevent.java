package cn.royan.subtick.mixin.queue;

import cn.royan.subtick.helpers.TickHandler;
import cn.royan.subtick.interfaces.ITickHandleable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin_blockevent {
	@Shadow
	@Final
	private MinecraftServer server;

	private TickHandler tickHandler() {
		return ((ITickHandleable) server).tickHandler();
	}


	@Inject(
		method = "addBlockEvent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerWorld$BlockEventQueue;add(Ljava/lang/Object;)Z"
		)
	)
	private void onAddBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci, @Local BlockEvent blockEvent) {
		if (tickHandler().frozen())
			tickHandler().queues().onScheduleBlockEvent((ServerWorld) (Object) this, blockEvent);
	}
}
