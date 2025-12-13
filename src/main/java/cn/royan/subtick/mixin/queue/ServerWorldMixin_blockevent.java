package cn.royan.subtick.mixin.queue;

import cn.royan.subtick.helpers.TickHandler;
import cn.royan.subtick.interfaces.ITickHandleable;
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
	public ServerWorld.BlockEventQueue[] blockEvents;

	@Shadow
	public int nextBlockEventQueueIndex;

	@Shadow
	@Final
	private MinecraftServer server;

	private TickHandler tickHandler() {
		return ((ITickHandleable) server).tickHandler();
	}


	@Inject(
		method = "addBlockEvent",
		at = @At(
			"HEAD"
		)
	)
	private void onAddBlockEvent(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
		BlockEvent blockEvent = new BlockEvent(pos, block, type, data);
		boolean add = true;
		for (BlockEvent blockEvent2 : this.blockEvents[this.nextBlockEventQueueIndex]) {
			if (blockEvent2.equals(blockEvent)) {
				add = false;
				break;
			}
		}

		if (tickHandler().frozen() && add)
			tickHandler().queues().onScheduleBlockEvent((ServerWorld) (Object) this, blockEvent);
	}
}
