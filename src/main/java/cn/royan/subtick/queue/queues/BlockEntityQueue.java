package cn.royan.subtick.queue.queues;

import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Iterator;

public class BlockEntityQueue extends TickingQueue {

	public BlockEntityQueue() {
		super(TickPhase.BLOCK_ENTITY, "blockEntity", "Block Entity", "Block Entities");
	}

	@Override
	public void start(ServerWorld level) {
		super.start(level);
		level.isTickingBlockEntities = true;
		if (!level.removedBlockEntities.isEmpty()) {
			level.tickingBlockEntities.removeAll(level.removedBlockEntities);
			level.blockEntities.removeAll(level.removedBlockEntities);
			level.removedBlockEntities.clear();
		}
		queue.clear();
		for(BlockEntity be : level.tickingBlockEntities)
			queue.add(new QueueElement(be));
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		int executed_steps = 0;
		int success_steps = 0;

		Iterator<BlockEntity> iterator = level.tickingBlockEntities.iterator();
		while(success_steps < count && iterator.hasNext())
		{
			BlockEntity blockEntity = iterator.next();
			BlockPos tpos = blockEntity.getPos();
			if(tpos == null)
			{
				queue.remove(new QueueElement(blockEntity));
				continue;
			}
			else if(rangeCheck(tpos, pos, range))
				success_steps ++;
			executed_steps ++;

			if (!blockEntity.isRemoved() && blockEntity.hasWorld()) {
				BlockPos blockPos = blockEntity.getPos();
				if (level.isChunkLoaded(blockPos) && level.worldBorder.contains(blockPos)) {
					try {
						((Tickable) blockEntity).tick();
					} catch (Throwable throwable2) {
						CrashReport crashReport2 = CrashReport.of(throwable2, "Ticking block entity");
						CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Block entity being ticked");
						blockEntity.populateCrashReport(crashReportCategory2);
						throw new CrashException(crashReport2);
					}
				}
			}

			if (blockEntity.isRemoved()) {
				iterator.remove();
				level.blockEntities.remove(blockEntity);
				if (level.isChunkLoaded(blockEntity.getPos())) {
					level.getChunk(blockEntity.getPos()).removeBlockEntity(blockEntity.getPos());
				}
			}
		}
		return Triple.of(executed_steps, success_steps, exhausted = !iterator.hasNext());
	}

	@Override
	public void end() {
		level.isTickingBlockEntities = false;
	}
}
