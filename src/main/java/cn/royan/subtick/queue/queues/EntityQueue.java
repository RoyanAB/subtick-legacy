package cn.royan.subtick.queue.queues;

import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Iterator;
import java.util.List;

public class EntityQueue extends TickingQueue {

	public EntityQueue() {
		super(TickPhase.ENTITY, "entity", "Entity", "Entities");
	}
	private int tickingID = 0;

	@Override
	public void start(ServerWorld level) {
		super.start(level);
		tickingID = 0;
		queue.clear();
		for(Entity e : level.entities)
			queue.add(new QueueElement(e));
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		int executed_steps = 0;
		int success_steps = 0;
		while(success_steps < count && tickingID < level.entities.size())
		{
			Entity entity = level.entities.get(tickingID);
			Entity entity2 = entity.getMount();
			if (entity2 != null) {
				if (!entity2.removed && entity2.hasPassenger(entity)) {
					continue;
				}

				entity.stopRiding();
			}

			if (!entity.removed && !(entity instanceof ServerPlayerEntity)) {
				try {
					level.updateEntity(entity);
				} catch (Throwable throwable2) {
					CrashReport crashReport2 = CrashReport.of(throwable2, "Ticking entity");
					CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Entity being ticked");
					entity.populateCrashReport(crashReportCategory2);
					throw new CrashException(crashReport2);
				}
			}

			if (entity.removed) {
				int k = entity.chunkX;
				int l = entity.chunkZ;
				if (entity.isLoaded && level.isChunkLoadedAt(k, l, true)) {
					level.getChunkAt(k, l).removeEntity(entity);
				}

				level.entities.remove(tickingID --);
				level.onEntityRemoved(entity);
			}
			if(rangeCheck(entity.getSourceBlockPos(), pos, range))
				success_steps ++;
			executed_steps ++;
			tickingID ++;
		}
		return Triple.of(executed_steps, success_steps, exhausted = !(tickingID < level.entities.size()));
	}

	@Override
	public void end() {
		tickingID = 0;
	}
}
