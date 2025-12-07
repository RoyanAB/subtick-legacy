package cn.royan.subtick.queue;

import net.minecraft.util.math.BlockPos;

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


//	public QueueElement(BlockEventData be, int depth) {
//
//		this(getLabelForBlockEvent(be.getBlock(), be.getParamA(), be.getParamB()), be.getPos(), depth);
//		//#endif
//	}
//
//	public QueueElement(TickingBlockEntity be) {
//		this(be.getType(), be.getPos(), 0);
//	}
//
//	public QueueElement(Entity e) {
//		this(e.getName().getString(), e.getId(), 0, 0, 0);
//	}
//
//	public QueueElement(TickNextTickData<?> t) {
//		this(t.getType() instanceof Block block ? block.getName().getString() : ((Fluid) t.getType()).toString(), t.pos, t.priority.getValue());
//	}
//
//	public QueueElement(TickEntry<?> t) {
//		this(t.getType() instanceof Block block ? block.getName().getString() : ((Fluid) t.getType()).toString(), t.pos, t.priority.getValue());
//	}

}
