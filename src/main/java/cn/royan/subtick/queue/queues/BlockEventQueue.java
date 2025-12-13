package cn.royan.subtick.queue.queues;

import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import cn.royan.subtick.utils.TickingMode;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Iterator;

public class BlockEventQueue extends TickingQueue {
	private static final TickingMode INDEX = new TickingMode("Block Event", "Block Events");
	private static final TickingMode DEPTH = new TickingMode("Block Event Depth", "Block Event Depths");
	private static final HashMap<String, TickingMode> mode = new HashMap<>();

	private int depth;
	private int remainingSteps;
	private int excutingBlockEventQueueIndex;

	public BlockEventQueue() {
		super(mode, INDEX, TickPhase.BLOCK_EVENT, "blockEvent");
	}

	public void updateQueue(ServerWorld level, BlockEvent be) {
		if (queue.add(new QueueElement(be, depth))) {
			newQueueElementsCount++;
//			ServerNetworkHandler.sendQueue(queue, spentQueue, level);
		}
		exhausted = false;
	}

	@Override
	public void start(ServerWorld level) {
		super.start(level);
		queue.clear();
		spentQueue.clear();
		for (BlockEvent be : level.blockEvents[level.nextBlockEventQueueIndex])
			queue.add(new QueueElement(be, 0));
		excutingBlockEventQueueIndex = level.nextBlockEventQueueIndex;
		depth = 0;
		remainingSteps = 0;
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		int executed_steps = 0;
		int success_steps = 0;
		while (success_steps < count && (!level.blockEvents[1].isEmpty() || !level.blockEvents[0].isEmpty())) {
			if (remainingSteps < 1) {
				excutingBlockEventQueueIndex = level.nextBlockEventQueueIndex;
				level.nextBlockEventQueueIndex ^= 1;
				remainingSteps = level.blockEvents[excutingBlockEventQueueIndex].size();
				depth++;
			}

			int size = currentMode == INDEX ? 1 : remainingSteps;
			remainingSteps -= size;
			boolean stepped = false;

			Iterator<BlockEvent> iterator = level.blockEvents[excutingBlockEventQueueIndex].iterator();
			for (int i = 0; i < size; i++) {
				BlockEvent blockEvent = iterator.next();
				iterator.remove();
				if (!queue.isEmpty())
					spentQueue.add(queue.removeFirst());

				if (!level.doBlockEvent(blockEvent)) {
					spentQueue.remove(spentQueue.size() - 1);
					continue;
				}

				level.server.getPlayerManager().sendPacket(null, blockEvent.getPos().getX(), blockEvent.getPos().getY(), blockEvent.getPos().getZ(), 64.0F, level.dimension.getType().getId(), new BlockEventS2CPacket(blockEvent.getPos(), blockEvent.getBlock(), blockEvent.getType(), blockEvent.getData()));

				if (rangeCheck(blockEvent.getPos(), pos, range))
					stepped = true;
				executed_steps++;
			}
			if (stepped)
				success_steps++;
		}
		return Triple.of(executed_steps, success_steps, exhausted = (level.blockEvents[1].isEmpty() && level.blockEvents[0].isEmpty()));
	}

	@Override
	public void end() {

	}

	static {
		mode.put("index", INDEX);
		mode.put("depth", DEPTH);
	}
}
