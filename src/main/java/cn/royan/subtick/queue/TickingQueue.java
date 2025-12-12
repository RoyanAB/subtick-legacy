package cn.royan.subtick.queue;

import cn.royan.subtick.queue.queues.BlockEntityQueue;
import cn.royan.subtick.queue.queues.BlockEventQueue;
import cn.royan.subtick.queue.queues.EntityQueue;
import cn.royan.subtick.queue.queues.ScheduledTickQueue;
import cn.royan.subtick.utils.TickingMode;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TickingQueue {
	// Block events can happen multiple times in the same block, but only if no equivalent block event exists in the queue.
	// To keep track of them, we have 2 queues; Block events get moved to spentQueue after ticking.
	protected final ObjectLinkedOpenHashSet<QueueElement> queue = new ObjectLinkedOpenHashSet<>();
	protected final ArrayList<QueueElement> spentQueue = new ArrayList<>();
	protected int newQueueElementsCount = 0;
	public boolean exhausted;

	protected final int phase;
	protected final String commandKey;
	protected final Map<String, TickingMode> modes;
	protected final TickingMode defaultMode;
	protected TickingMode currentMode;
	protected ServerWorld level;

	public static final TickingQueue BLOCK_TICK = new ScheduledTickQueue();
	public static final TickingQueue BLOCK_EVENT = new BlockEventQueue();
	public static final TickingQueue ENTITY = new EntityQueue();
	public static final TickingQueue BLOCK_ENTITY = new BlockEntityQueue();

	private static final ImmutableMap<String, TickingQueue> BY_COMMAND_KEY = ImmutableMap.of(
		"tileTick", BLOCK_TICK,
		"blockEvent", BLOCK_EVENT,
		"entity", ENTITY,
		"blockEntity", BLOCK_ENTITY);

	public static String[] commandKeys = new String[]{
		BLOCK_TICK.commandKey, BLOCK_EVENT.commandKey, ENTITY.commandKey, BLOCK_ENTITY.commandKey};

	public TickingQueue(int phase, String commandKey, String nameSingle, String nameMultiple) {
		this(new HashMap<>(), new TickingMode(nameSingle, nameMultiple), phase, commandKey);
	}

	public TickingQueue(Map<String, TickingMode> modes, TickingMode defaultMode, int phase, String commandKey) {
		this.modes = modes;
		this.defaultMode = defaultMode;
		this.phase = phase;
		this.commandKey = commandKey;
	}

	public static TickingQueue byCommandKey(String commandKey) {
		TickingQueue queue = BY_COMMAND_KEY.get(commandKey);
		return queue;
	}

	public Set<String> getModes() {
		return modes.keySet();
	}

	public void setMode(String key) {
		TickingMode newMode = key.equals("") ? defaultMode : modes.get(key);
		currentMode = newMode;
	}

	public int getPhase() {
		return phase;
	}

	@Deprecated
	public String getName(int count) {
		return currentMode.getName(count);
	}

	public String getName() {
		return currentMode.getName();
	}

	public String getNamePlural() {
		return currentMode.getNamePlural();
	}

	public static boolean rangeCheck(BlockPos a, BlockPos b, long range) {
		if (range == -2) return false;
		if (range == -1) return true;

		long x = a.getX() - b.getX();
		long y = a.getY() - b.getY();
		long z = a.getZ() - b.getZ();
		return x * x + y * y + z * z <= range * range;
	}

	public void sendQueueStep(CommandSource actor, int count) {
//    ServerNetworkHandler.sendQueueStep(queue, spentQueue, newQueueElementsCount, count, actor.getLevel(), actor);
		newQueueElementsCount = 0;
	}

	public boolean cantStep() {
		return exhausted;
	}

	public void start(ServerWorld level) {
		this.level = level;
	}

	// Actual count, feedback, exhausted
	public abstract Triple<Integer, Integer, Boolean> step(int count, BlockPos pos, int range);

	public void end() {
	}
}
