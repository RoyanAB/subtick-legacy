package cn.royan.subtick.mixin.subtick.freeze;

import cn.royan.subtick.interfaces.ServerWorldInterface;
import net.minecraft.server.world.ScheduledTick;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.WorldGeneratorType;
import net.minecraft.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_scheduledtick extends World implements ServerWorldInterface {
	@Shadow
	@Final
	private TreeSet<ScheduledTick> scheduledTicksInOrder;

	@Shadow
	@Final
	private Set<ScheduledTick> scheduledTicks;

	@Shadow
	@Final
	private List<ScheduledTick> scheduledTicksThisTick;

	protected ServerWorldMixin_scheduledtick(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler, boolean isClient) {
		super(storage, data, dimension, profiler, isClient);
	}


	@Unique
	@Override
	public boolean startScheduledTick(){
		if (this.data.getGeneratorType() == WorldGeneratorType.DEBUG_ALL_BLOCK_STATES) {
			return false;
		} else {
			int i = this.scheduledTicksInOrder.size();
			if (i != this.scheduledTicks.size()) {
				throw new IllegalStateException("TickNextTick list out of synch");
			} else {
				if (i > 65536) {
					i = 65536;
				}

				for (int j = 0; j < i; ++j) {
					ScheduledTick scheduledTick = this.scheduledTicksInOrder.first();
					if (scheduledTick.time > this.data.getTime()) {
						break;
					}

					this.scheduledTicksInOrder.remove(scheduledTick);
					this.scheduledTicks.remove(scheduledTick);
					this.scheduledTicksThisTick.add(scheduledTick);
				}
			}
			return true;
		}
	}
}
