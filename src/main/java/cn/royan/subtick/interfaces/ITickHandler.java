package cn.royan.subtick.interfaces;

import cn.royan.subtick.utils.TickPhase;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.world.ServerWorld;

/**
 * Stores the state of the server. Has public methods for scheduling actions.
 */
public interface ITickHandler {
	static ITickHandler get(CommandSource c) {
		return ((ITickHandleable) c.asEntity().getServer()).tickHandler();
	}

	/*
	 * Placed in a {@link com.llamalad7.mixinextras.injector.WrapWithCondition} before each tick phase. Returns whether that phase should execute.
	 */
	boolean shouldTick(ServerWorld level, int tickPhase);

	/*
	 * Called by the /tick when command.
	 */
	int when(CommandSource c);

	/*
	 * Called by the /tick freeze on command.
	 */
	int freeze(CommandSource c, int phase);

	/*
	 * Called by the /tick freeze off command.
	 */
	int unfreeze(CommandSource c);

	/*
	 * Called by the /tick freeze command.
	 */
	int toggleFreeze(CommandSource c, int phase);

	/*
	 * Called by the /tick step command.
	 */
	int step(CommandSource c, int count, int phase);

	/*
	 * Called by the /phaseStep command. Steps [count] phases.
	 */
	int phaseStep(CommandSource c, int count);

	/*
	 * Called by the /phaseStep command. Steps to the next given phase.
	 */
	int stepToPhase(CommandSource c, int phase, boolean force);

	/*
	 * Prints and returns whether the tick handler can currently tick step.
	 */
	boolean canStep(CommandSource c, int count, TickPhase phase);

	/*
	 * Returns whether the tick handler can currently tick step.
	 */
	boolean canStep(int count, TickPhase phase);

	/*
	 * Returns the IQueues for this tick handler.
	 */
//  public IQueues queues();

	/*
	 * Returns whether the handler is frozen. This is used in the ServerLevel to know if it should record scheduled block events for queue stepping highlights.
	 */
	boolean frozen();

	/*
	 * Returns the current phase.
	 */
	TickPhase currentPhase();

	/*
	 * Returns the target phase for tick stepping. This is used in queue stepping to clear client highlights.
	 */
	TickPhase targetPhase();
}
