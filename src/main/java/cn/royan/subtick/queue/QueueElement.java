package cn.royan.subtick.queue;

import net.minecraft.block.Block;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ScheduledTick;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class QueueElement {
	public final String label;
	public int x;
	public int y;
	public int z;
	public int depth;

	@Override
	public boolean equals(Object o) {
		return ((QueueElement) o).x == x && ((QueueElement) o).y == y && ((QueueElement) o).z == z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	public BlockPos blockPos() {
		return new BlockPos(x, y, z);
	}

	public QueueElement(String label, BlockPos pos, int depth) {
		this(label, pos.getX(), pos.getY(), pos.getZ(), depth);
	}

	public QueueElement(String label, int x, int y, int z, int depth) {
		this.label = label;
		this.x = x;
		this.y = y;
		this.z = z;
		this.depth = depth;
	}

	private static String getLabelForBlockEvent(Block block, int a, int b) {
		if (block instanceof PistonBaseBlock)
			return block.getName() + (a == 0 ? " |→ " : " |← ") + Direction.byId(b);

		return block.getName();
	}

	public QueueElement(BlockEvent be, int depth) {
		this(getLabelForBlockEvent(be.getBlock(), be.getData(), be.getType()), be.getPos(), depth);
	}

	public QueueElement(BlockEntity be) {
		this(be.getBlock().getName(), be.getPos(), 0);
	}

	public QueueElement(Entity e) {
		this(e.getName(), e.getNetworkId(), 0, 0, 0);
	}

	public QueueElement(ScheduledTick t) {
		this(t.getBlock().getName(), t.pos, t.priority);
	}

}
