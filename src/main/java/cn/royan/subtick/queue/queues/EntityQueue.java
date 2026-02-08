package cn.royan.subtick.queue.queues;

import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.queue.TickingQueue;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.entity.Entity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityQueue extends TickingQueue {

	public EntityQueue() {
		super(TickPhase.ENTITY, "entity", "Entity", "Entities");
	}

	private Iterator<Entity> iterator;

	@Override
	public void start(ServerWorld level) {
		super.start(level);
		queue.clear();
		for (Entity e : level.entities)
				queue.add(new QueueElement(e));
		List<Entity> thisTickEntity = new ArrayList<>(level.entities);
		iterator = thisTickEntity.iterator();
	}

	@Override
	public Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range) {
		int executed_steps = 0;
		int success_steps = 0;
		while (success_steps < count && iterator.hasNext()) {
			Entity entity = iterator.next();
			Entity entity2 = entity.getMount();
			if (entity2 != null) {
				if (!entity2.removed && entity2.hasPassenger(entity)) {
					executed_steps++;
					continue;
				}

				entity.stopRiding();
			}

			if (!entity.removed && !(entity instanceof ServerPlayerEntity)) {
				level.tickEntity(entity);
			}

			if (entity.removed) {
				int k = entity.chunkX;
				int l = entity.chunkZ;
				if (entity.inChunk && level.isChunkLoadedAt(k, l, true)) {
					level.getChunkAt(k, l).removeEntity(entity);
				}

				iterator.remove();
				level.notifyEntityRemoved(entity);
			}
			if (rangeCheck(entity.getCommandSourceBlockPos(), pos, range))
				success_steps++;
			executed_steps++;
		}
		return Triple.of(executed_steps, success_steps, exhausted = !iterator.hasNext());
	}

	@Override
	public void end() {

	}
}
