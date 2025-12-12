package cn.royan.subtick.mixin.subtick.freeze;

import cn.royan.subtick.helpers.TickHandler;
import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.interfaces.WorldInterface;
import cn.royan.subtick.utils.TickPhase;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.PortalForcer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.chunk.ChunkSource;
import net.minecraft.world.village.SavedVillageData;
import net.minecraft.world.village.VillageSiege;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements WorldInterface {

	@Unique
	private boolean tickingTime = false;

	@Unique
	private boolean tickingVillage = false;

	@Shadow
	@Final
	private MinecraftServer server;

	@Unique
	private TickHandler tickHandler() {
		return ((ITickHandleable) server).tickHandler();
	}


	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;tick()V"
		)
	)
	public boolean wrapTickWeather(World instance) {
		return tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.WEATHER);
	}

	@ModifyExpressionValue(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/Gamerules;getBoolean(Ljava/lang/String;)Z",
			ordinal = 1
		)
	)
	private boolean wrapMobSpawning(boolean original) {
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.MOB_SPAWNING)) {
			return original;
		}
		return false;
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/chunk/ChunkSource;tick()Z"
		)
	)
	public boolean wrapChunkSource(ChunkSource instance) {
		return tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.CHUNK_SOURCE);
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/WorldData;setTime(J)V"
		)
	)
	public boolean wrapWorldTimeUpdate(WorldData instance, long time) {
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.TIME)) {
			this.tickingTime = true;
			return true;
		}
		return false;
	}

	@ModifyExpressionValue(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/Gamerules;getBoolean(Ljava/lang/String;)Z",
			ordinal = 2
		)
	)
	private boolean wrapDayTimeUpdate(boolean original) {
		if (tickingTime) {
			this.tickingTime = false;
			return original;
		}
		return false;
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerWorld;doScheduledTicks(Z)Z"
		)
	)
	public boolean wrapTileTicks(ServerWorld instance, boolean flush) {
		return tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.TILE_TICK);
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerWorld;tickChunks()V"
		)
	)
	public boolean wrapChunkTicks(ServerWorld instance) {
		return tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.RANDOM_TICK);
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/village/SavedVillageData;tick()V"
		)
	)
	public boolean wrapVillages(SavedVillageData instance) {
		if (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.VILLAGE)) {
			this.tickingVillage = true;
			return true;
		}
		return false;
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/village/VillageSiege;tick()V"
		)
	)
	public boolean wrapVillageSieges(VillageSiege instance) {
		if (tickingVillage) {
			this.tickingVillage = false;
			return true;
		}
		return false;
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/PortalForcer;tick(J)V"
		)
	)
	public boolean wrapPortalRemoval(PortalForcer instance, long time) {
		return (tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.PORTAL_FORCER));
	}

	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V"
		)
	)
	public boolean wrapBlockEvents(ServerWorld instance) {
		return tickHandler().shouldTick((ServerWorld) (Object) this, TickPhase.BLOCK_EVENT);
	}
}
