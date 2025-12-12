package cn.royan.subtick.queue.queues;

import cn.royan.subtick.interfaces.ServerWorldInterface;
import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import cn.royan.subtick.utils.TickingMode;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.server.world.ScheduledTick;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Iterator;

public class ScheduledTickQueue extends TickingQueue {
	private static final TickingMode INDEX = new TickingMode("Tile Tick", "Tile Ticks");
	private static final TickingMode PRIORITY = new TickingMode("Tile Tick Priority", "Tile Tick Priorities");
	private static final HashMap<String, TickingMode> mode = new HashMap<>();

	public ScheduledTickQueue() {
		super(mode, INDEX, TickPhase.TILE_TICK, "tileTick");
	}

	@Override
	public void start(ServerWorld level) {
		super.start(level);
		((ServerWorldInterface) level).startScheduledTick();
		queue.clear();
		for (ScheduledTick scheduledTick : level.scheduledTicksThisTick)
			queue.add(new QueueElement(scheduledTick));
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		int executed_steps = 0;
		int success_steps = 0;
		while (success_steps < count && !level.scheduledTicksThisTick.isEmpty()) {

			Iterator<ScheduledTick> iterator = level.scheduledTicksThisTick.iterator();

			if (iterator.hasNext()) {
				ScheduledTick scheduledTick = iterator.next();
				iterator.remove();
				if (level.isAreaLoaded(scheduledTick.pos.add(0, 0, 0), scheduledTick.pos.add(0, 0, 0))) {
					BlockState blockState = level.getBlockState(scheduledTick.pos);
					if (blockState.getMaterial() != Material.AIR && Block.areEqual(blockState.getBlock(), scheduledTick.getBlock())) {
						try {
							blockState.getBlock().tick(level, scheduledTick.pos, blockState, level.random);
						} catch (Throwable throwable) {
							CrashReport crashReport = CrashReport.of(throwable, "Exception while ticking a block");
							CrashReportCategory crashReportCategory = crashReport.addCategory("Block being ticked");
							CrashReportCategory.addBlockDetails(crashReportCategory, scheduledTick.pos, blockState);
							throw new CrashException(crashReport);
						}
					}
				} else {
					level.scheduleTick(scheduledTick.pos, scheduledTick.getBlock(), 0);
				}
				if (currentMode == INDEX) {
					if (rangeCheck(scheduledTick.pos, pos, range))
						success_steps++;
				} else {
					ScheduledTick nextTick = null;
					if(iterator.hasNext())
						nextTick = iterator.next();
					if (nextTick == null || nextTick.priority != scheduledTick.priority)
						success_steps++;
				}
			}


			executed_steps++;
		}
		return Triple.of(executed_steps, success_steps, exhausted = level.scheduledTicksThisTick.isEmpty());
	}

	@Override
	public void end() {
		this.level.scheduledTicksThisTick.clear();
	}

	static {
		mode.put("index", INDEX);
		mode.put("priority", PRIORITY);
	}
}
