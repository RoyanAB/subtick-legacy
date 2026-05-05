package cn.royan.subtick.client;

import cn.royan.subtick.client.config.Configs;
import cn.royan.subtick.client.interfaces.IEntity;
import cn.royan.subtick.client.interfaces.ITickTimer;
import cn.royan.subtick.client.render.LevelRenderer;
import cn.royan.subtick.queue.QueueElement;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientTickHandler {
	private static final Minecraft mc = Minecraft.getInstance();
	public static final List<QueueElement> queue = new CopyOnWriteArrayList<>();
	public static int newQueueElementCount = 0;
	public static final List<String> dimensions = new CopyOnWriteArrayList<>();
	public static boolean frozen;
	public static TickPhase tickPhase = TickPhase.INVALID;
	public static int queueIndex1 = 0, queueIndex2 = 0;

	private static boolean stepping;
	private static int remaining_ticks;
	public static boolean skip_block_entities;

	public static long mspt = 50L;

	private static void clearRenders() {
		LevelRenderer.clear();
		List<Entity> thisTickEntity = new ArrayList<>(mc.world.entities);
		thisTickEntity.forEach((entity) -> ((IEntity) entity).setCGlowing(false));
	}

	public static void setTickRate(float rate) {
		long msptt = (long) (1000.0 / rate);
		if (msptt <= 0L) {
			msptt = 1L;
		}
		mspt = msptt;
	}

	public static void setFreeze(NbtCompound tag) {
		if (frozen = tag.getBoolean("is_paused")) {
			try {
				((ITickTimer) mc.timer).reset();
				setPhase(new TickPhase(tag));
				NbtList listTag = (NbtList) tag.get("dims");
				dimensions.clear();
				for (NbtElement element : listTag.elements)
					dimensions.add(((NbtCompound) element).getString("d"));
			} catch (Exception e) {
				setPhase(TickPhase.INVALID);
			}
		} else {
			ClientBlockEntityQueue.end(mc.world);
			clearQueue();
			clearRenders();
		}
	}

	public static void setPhase(TickPhase phase) {
		tickPhase = phase;
		clearQueue();
		clearRenders();
		queueIndex1 = 0;
		queueIndex2 = 0;
	}

	public static synchronized void clearQueue() {
		queue.clear();
	}

	public static synchronized void setQueue(NbtList tag) {
		int l = queue.size();
		queue.clear();
		tag.elements.forEach((NbtElement t) ->
		{
			NbtCompound t1 = (NbtCompound) t;
			queue.add(new QueueElement(t1.getString("s"), t1.getInt("x"), t1.getInt("y"), t1.getInt("z"), t1.getInt("d")));
		});
		newQueueElementCount = Math.max(0, queue.size() - l);
	}

	public static synchronized void queueStep(NbtCompound tag) {
		setQueue((NbtList) tag.get("queue"));
		int steps = tag.getInt("steps");
		newQueueElementCount = tag.getInt("newElements");
		queueIndex1 = queueIndex2;
		queueIndex2 += steps;
		// out of bounds protection
		int index2 = Math.min(queueIndex2, queue.size());
		int index1 = Math.min(queueIndex1, index2);

		clearRenders();
		if (tickPhase.phase == TickPhase.ENTITY) {
			ClientWorld level = mc.world;
			level.entities.forEach((entity) -> ((IEntity) entity).setCGlowing(false));
			for (int i = queueIndex1; i < queueIndex2; i++) {
				try {
					QueueElement element = queue.get(i);
					if (rangeCheck(new BlockPos(element.x, element.y, element.z), mc.player.getCommandSourceBlockPos(), 64)) {
						if (level.getEntity(element.depth) != null)
							((IEntity) level.getEntity(element.depth)).setCGlowing(true);
					}
				} catch (Throwable e) {

				}
			}
			return;
		}

		boolean blockEntity = tickPhase.phase == TickPhase.BLOCK_ENTITY;
		boolean depth = tickPhase.phase == TickPhase.BLOCK_EVENT || tickPhase.phase == TickPhase.TILE_TICK;
		int i = 0;
		Iterator<QueueElement> iter = queue.iterator();
		while (i < index1) {
			QueueElement element = iter.next();
			LevelRenderer.addCuboidFaces(element.x, element.y, element.z, Configs.STEPPED_BG.getColor());
			if (depth)
				LevelRenderer.addLabel(++i, element.depth, element.x, element.y, element.z, Configs.STEPPED_TEXT.getColor(), Configs.STEPPED_DEPTH.getColor());
			else
				LevelRenderer.addText(String.valueOf(++i), element.x, element.y, element.z, Configs.STEPPED_TEXT.getColor());
		}
		while (i < index2) {
			QueueElement element = iter.next();
			LevelRenderer.addCuboidFaces(element.x, element.y, element.z, Configs.STEPPING_BG.getColor());
			if (depth)
				LevelRenderer.addLabel(++i, element.depth, element.x, element.y, element.z, Configs.STEPPING_TEXT.getColor(), Configs.STEPPING_DEPTH.getColor());
			else
				LevelRenderer.addText(String.valueOf(++i), element.x, element.y, element.z, Configs.STEPPING_TEXT.getColor());

			if (blockEntity)
				ClientBlockEntityQueue.addPos(element);
		}
		while (i < queue.size() - newQueueElementCount) {
			QueueElement element = iter.next();
			LevelRenderer.addCuboidFaces(element.x, element.y, element.z, Configs.TO_STEP_BG.getColor());
			if (depth)
				LevelRenderer.addLabel(++i, element.depth, element.x, element.y, element.z, Configs.TO_STEP_TEXT.getColor(), Configs.TO_STEP_DEPTH.getColor());
			else
				LevelRenderer.addText(String.valueOf(++i), element.x, element.y, element.z, Configs.TO_STEP_TEXT.getColor());
		}
		while (i < queue.size()) {
			QueueElement element = iter.next();
			LevelRenderer.addCuboidFaces(element.x, element.y, element.z, Configs.NEW_BG.getColor());
			if (depth)
				LevelRenderer.addLabel(++i, element.depth, element.x, element.y, element.z, Configs.NEW_TEXT.getColor(), Configs.NEW_DEPTH.getColor());
			else
				LevelRenderer.addText(String.valueOf(++i), element.x, element.y, element.z, Configs.NEW_TEXT.getColor());
		}
	}

	public static void scheduleTickStep(int ticks) {
		if (ticks <= 2)
			return;

		clearQueue();
		clearRenders();

		if (ClientBlockEntityQueue.end(mc.world))
			skip_block_entities = true;

		stepping = true;
		remaining_ticks = ticks;
	}

	public static boolean shouldTick() {
		return !frozen || stepping;
	}

	public static void onTick(ClientWorld level) {
		if (stepping && --remaining_ticks <= 2)
			stepping = false;

		ClientBlockEntityQueue.step(level);
		skip_block_entities = false;
	}

	public static boolean rangeCheck(BlockPos a, BlockPos b, long range) {
		if (range == -2) return false;
		if (range == -1) return true;

		long x = a.getX() - b.getX();
		long y = a.getY() - b.getY();
		long z = a.getZ() - b.getZ();
		return x * x + y * y + z * z <= range * range;
	}
}
