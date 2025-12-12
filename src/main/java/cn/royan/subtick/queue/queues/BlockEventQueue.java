package cn.royan.subtick.queue.queues;

import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import cn.royan.subtick.utils.TickingMode;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;

public class BlockEventQueue extends TickingQueue {
	private static final TickingMode INDEX = new TickingMode("Block Event", "Block Events");
	private static final TickingMode DEPTH = new TickingMode("Block Event Depth", "Block Event Depths");
	private static final HashMap<String, TickingMode> mode = new HashMap<>();

	public BlockEventQueue() {
		super(mode, INDEX, TickPhase.BLOCK_EVENT, "blockEvent");
	}

	@Override
	public void start(ServerWorld level) {
		super.start(level);
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		return null;
	}

	@Override
	public void end() {

	}

	static {
		mode.put("index", INDEX);
		mode.put("depth", DEPTH);
	}
}
