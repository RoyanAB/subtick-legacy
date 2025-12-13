package cn.royan.subtick.mixin.freeze;

import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_entity extends World implements ITickHandleable {
	protected ServerWorldMixin_entity(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler, boolean isClient) {
		super(storage, data, dimension, profiler, isClient);
	}

	/**
	 * @author AB
	 * @reason To split tickEntities method
	 */
	@Overwrite
	public void tickEntities() {
		this.profiler.push("entities");
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.GLOBAL_ENTITY)) {
			this.tickGlobalEntities();
			this.removeGlobalEntities();
		}
		this.tickPlayers();
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.ENTITY)) {
			this.tickEntity();
		}
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.BLOCK_ENTITY)) {
			this.tickBlockEntities();
		}
		this.pendingBlockEntities();
		this.profiler.pop();
	}

	@Unique
	public void tickGlobalEntities() {
		this.profiler.push("global");

		for (int i = 0; i < this.globalEntities.size(); ++i) {
			Entity entity = this.globalEntities.get(i);

			try {
				++entity.time;
				entity.tick();
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.of(throwable, "Ticking entity");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being ticked");
				if (entity == null) {
					crashReportCategory.add("Entity", "~~NULL~~");
				} else {
					entity.populateCrashReport(crashReportCategory);
				}

				throw new CrashException(crashReport);
			}

			if (entity.removed) {
				this.globalEntities.remove(i--);
			}
		}
	}

	@Unique
	public void removeGlobalEntities() {
		this.profiler.swap("remove");
		this.entities.removeAll(this.entitiesToRemove);

		for (int i = 0; i < this.entitiesToRemove.size(); ++i) {
			Entity entity = this.entitiesToRemove.get(i);
			int j = entity.chunkX;
			int k = entity.chunkZ;
			if (entity.isLoaded && this.isChunkLoadedAt(j, k, true)) {
				this.getChunkAt(j, k).removeEntity(entity);
			}
		}

		for (int i = 0; i < this.entitiesToRemove.size(); ++i) {
			this.onEntityRemoved(this.entitiesToRemove.get(i));
		}

		this.entitiesToRemove.clear();
	}

	@Unique
	public void tickEntity() {
		this.profiler.swap("regular");

		for (int i = 0; i < this.entities.size(); ++i) {
			Entity entity = this.entities.get(i);
			Entity entity2 = entity.getMount();
			if (entity2 != null) {
				if (!entity2.removed && entity2.hasPassenger(entity)) {
					continue;
				}

				entity.stopRiding();
			}

			this.profiler.push("tick");
			if (!entity.removed && !(entity instanceof ServerPlayerEntity)) {
				try {
					this.updateEntity(entity);
				} catch (Throwable throwable2) {
					CrashReport crashReport2 = CrashReport.of(throwable2, "Ticking entity");
					CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Entity being ticked");
					entity.populateCrashReport(crashReportCategory2);
					throw new CrashException(crashReport2);
				}
			}

			this.profiler.pop();
			this.profiler.push("remove");
			if (entity.removed) {
				int k = entity.chunkX;
				int l = entity.chunkZ;
				if (entity.isLoaded && this.isChunkLoadedAt(k, l, true)) {
					this.getChunkAt(k, l).removeEntity(entity);
				}

				this.entities.remove(i--);
				this.onEntityRemoved(entity);
			}

			this.profiler.pop();
		}
	}

	@Unique
	public void tickBlockEntities() {
		this.profiler.swap("blockEntities");
		if (!this.removedBlockEntities.isEmpty()) {
			this.tickingBlockEntities.removeAll(this.removedBlockEntities);
			this.blockEntities.removeAll(this.removedBlockEntities);
			this.removedBlockEntities.clear();
		}

		this.isTickingBlockEntities = true;
		Iterator<BlockEntity> iterator = this.tickingBlockEntities.iterator();

		while (iterator.hasNext()) {
			BlockEntity blockEntity = iterator.next();
			if (!blockEntity.isRemoved() && blockEntity.hasWorld()) {
				BlockPos blockPos = blockEntity.getPos();
				if (this.isChunkLoaded(blockPos) && this.worldBorder.contains(blockPos)) {
					try {
						this.profiler.push(() -> String.valueOf(BlockEntity.getKey(blockEntity.getClass())));
						((Tickable) blockEntity).tick();
						this.profiler.pop();
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
				this.blockEntities.remove(blockEntity);
				if (this.isChunkLoaded(blockEntity.getPos())) {
					this.getChunk(blockEntity.getPos()).removeBlockEntity(blockEntity.getPos());
				}
			}
		}

		this.isTickingBlockEntities = false;
	}

	@Unique
	public void pendingBlockEntities() {
		this.profiler.swap("pendingBlockEntities");
		if (!this.pendingBlockEntities.isEmpty()) {
			for (int m = 0; m < this.pendingBlockEntities.size(); ++m) {
				BlockEntity blockEntity2 = this.pendingBlockEntities.get(m);
				if (!blockEntity2.isRemoved()) {
					if (!this.blockEntities.contains(blockEntity2)) {
						this.addBlockEntity(blockEntity2);
					}

					if (this.isChunkLoaded(blockEntity2.getPos())) {
						WorldChunk worldChunk = this.getChunk(blockEntity2.getPos());
						BlockState blockState = worldChunk.getBlockState(blockEntity2.getPos());
						worldChunk.setBlockEntity(blockEntity2.getPos(), blockEntity2);
						this.notifyBlockChanged(blockEntity2.getPos(), blockState, blockState, 3);
					}
				}
			}

			this.pendingBlockEntities.clear();
		}

		this.profiler.pop();
	}
}
