package cn.royan.subtick.client;

import cn.royan.subtick.queue.QueueElement;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ClientBlockEntityQueue {
    private static boolean stepping;
    private static final HashSet<BlockEntity> ticked_block_entities = new HashSet<>();
    private static final HashSet<BlockPos> poses = new HashSet<>();

    private static void start(ClientWorld level) {
        if (!level.pendingBlockEntities.isEmpty()) {
            level.tickingBlockEntities.addAll(level.pendingBlockEntities);
            level.pendingBlockEntities.clear();
        }
    }

    public static void addPos(QueueElement element) {
        poses.add(element.blockPos());
    }

    public static void step(ClientWorld level) {
        if (poses.isEmpty())
            return;

        if (!stepping) {
            start(level);
            stepping = true;
            level.isTickingBlockEntities = true;
        }

        Iterator<BlockEntity> iterator = new ArrayList<>(level.tickingBlockEntities).iterator();
        while (iterator.hasNext()) {
			BlockEntity blockEntity = iterator.next();
			if (blockEntity.getPos() == null)
				continue;

			if (poses.contains(blockEntity.getPos())) {
				if (!blockEntity.isRemoved() && blockEntity.hasWorld()) {
					BlockPos blockPos = blockEntity.getPos();
					if (level.isChunkLoaded(blockPos) && level.worldBorder.contains(blockPos)) {
						((Tickable) blockEntity).tick();
						ticked_block_entities.add(blockEntity);
					}
				}

				if (blockEntity.isRemoved()) {
					iterator.remove();
					level.blockEntities.remove(blockEntity);
					if (level.isChunkLoaded(blockEntity.getPos())) {
						level.getChunk(blockEntity.getPos()).removeBlockEntity(blockEntity.getPos());
					}
				}
				break;
			}
        }
        poses.clear();
    }

    public static boolean end(ClientWorld level) {
        if (!stepping)
            return false;
        stepping = false;

        Iterator<BlockEntity> iterator = new ArrayList<>(level.tickingBlockEntities).iterator();
        while (iterator.hasNext()) {
			BlockEntity blockEntity = iterator.next();

			if (!ticked_block_entities.contains(blockEntity)) {
				if (!blockEntity.isRemoved() && blockEntity.hasWorld()) {
					BlockPos blockPos = blockEntity.getPos();
					if (level.isChunkLoaded(blockPos) && level.worldBorder.contains(blockPos)) {
						((Tickable) blockEntity).tick();
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
        }

        level.isTickingBlockEntities = false;
        ticked_block_entities.clear();
        return true;
    }
}
