package cn.royan.subtick.interfaces;

import cn.royan.subtick.queue.TickingQueue;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/*
 * Has logic for queue stepping.
 */
public interface IQueues {
	static IQueues get(CommandSource c) {
		return ((ITickHandleable) c.getServer()).tickHandler().queues();
	}

	/*
	 * Called by the /queueStep command
	 */
	void schedule(CommandSource c, TickingQueue newQueue, String modeKey, int count, BlockPos pos, int range, boolean force);

	/*
	 * Marks the queue as dirty and needing to be ended. Called by the {@link subtick.ITickHandler} when it schedules an unfreeze or step (only if the step goes at least 1 phase forwards).
	 */
	void scheduleEnd();

	/*
	 * Called by the {@link subtick.ITickHandler} at the end of tick stepping, to ensure the queue is executed at the correct phase.
	 */
	void execute();

	/*
	 * Called by the {@link subtick.ITickHandler} before unfreezing or stepping. Ends the queue only if an end is scheduled by scheduleEnd.
	 */
	void end();

	/*
	 * Called by a mixin every time a block event is scheduled. This keeps the queue's block event queue up to date. Block events are the only queue where new elements can be added in the middle of stepping.
	 */
	void onScheduleBlockEvent(ServerWorld level, BlockEvent be);
}
