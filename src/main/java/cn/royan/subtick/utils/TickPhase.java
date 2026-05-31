package cn.royan.subtick.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TickPhase {
	public final int dim;
	public final int phase;

	public static TickPhase INVALID = new TickPhase(-1, -1);
	private static final List<String> dims = new ArrayList<>();

	// Tick phase order, reorder this if it changes across mc versions
	private static final List<String> commandKeys = Arrays.asList(
		"weather",
		"mobSpawning",
		"chunkSource",
		"time",
		"tileTick",
		"randomTick",
		"village",
		"portalForcer",
		"blockEvent",
		"globalEntity",
		"entity",
		"blockEntity"
//		"entityTracker"
	);
	public static final String[] commandSuggestions = commandKeys.toArray(new String[]{});

	public static final int
		WEATHER = commandKeys.indexOf("weather"),
		MOB_SPAWNING = commandKeys.indexOf("mobSpawning"),
		CHUNK_SOURCE = commandKeys.indexOf("chunkSource"),
		TIME = commandKeys.indexOf("time"),
		TILE_TICK = commandKeys.indexOf("tileTick"),
		RANDOM_TICK = commandKeys.indexOf("randomTick"),
		VILLAGE = commandKeys.indexOf("village"),
		PORTAL_FORCER = commandKeys.indexOf("portalForcer"),
		BLOCK_EVENT = commandKeys.indexOf("blockEvent"),
		GLOBAL_ENTITY = commandKeys.indexOf("globalEntity"),
		ENTITY = commandKeys.indexOf("entity"),
		BLOCK_ENTITY = commandKeys.indexOf("blockEntity");
	private static final int lastPhase = 11;
	public static final int totalPhases = 12;

	public static void reset() {
		dims.clear();
	}

	public static List<String> getDimensions() {
		return dims;
	}

	public TickPhase(ServerWorld level, int phase) {
		this.dim = dims.indexOf(level.dimension.getType().getKey());
		this.phase = phase;
	}

	public TickPhase(int dim, int phase) {
		this.dim = dim;
		this.phase = phase;
	}

	public TickPhase(NbtCompound tag) {
		this(tag.getInt("dim"), tag.getInt("phase"));
	}

	/*
	 * Gets the next tick phase, changing dimension as necessary
	 */
	public TickPhase next(ServerWorld level) {
		if (phase == lastPhase)
			return new TickPhase(dim + 1 == dims.size() ? 0 : dim + 1, 0);

		if (phase == BLOCK_EVENT && dimensionUnloaded(level))
			return new TickPhase(dim + 1 == dims.size() ? 0 : dim + 1, 0);

		return new TickPhase(dim, phase + 1);
	}

	/*
	 * Gets the next tick phase, but only in the current dimension
	 */
	public TickPhase next(int i) {
		return new TickPhase(dim, (phase + i) % totalPhases);
	}

	public boolean isLast(ServerWorld level) {
		if (dimensionUnloaded(level))
			return phase == lastPhase - 3 && dim == dims.size() - 1;
		else
			return phase == lastPhase && dim == dims.size() - 1;
	}

	public boolean isPriorTo(TickPhase phase2) {
		return dim < phase2.dim || phase < phase2.phase;
	}

	public String getPath() {
		return dims.get(dim);
	}

	public String getPhaseName() {
		return Translations.tr("subtick.tickPhase." + commandKeys.get(phase));
	}

	public static String getPhaseName(int phase) {
		return Translations.tr("subtick.tickPhase." + commandKeys.get(phase));
	}

	public static int byCommandKey(String key) {
		return commandKeys.indexOf(key);
	}

	public static void addDimension(ServerWorld level) {
		dims.add(level.dimension.getType().getKey());
	}

	private boolean dimensionUnloaded(ServerWorld level) {
		return level.players.isEmpty() && level.idleTimeout + 1 >= 300;
	}

	@Override
	public boolean equals(Object obj) {
		return this.dim == ((TickPhase) obj).dim && this.phase == ((TickPhase) obj).phase;
	}
}
