package cn.royan.subtick.helpers;

import cn.royan.subtick.interfaces.ITickHandler;
import cn.royan.subtick.queue.Queues;
import cn.royan.subtick.utils.Messenger;
import cn.royan.subtick.utils.TickPhase;
import cn.royan.subtick.utils.Translations;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.world.ServerWorld;

public class TickHandler implements ITickHandler {
	private enum State {
		UNFROZEN,
		FROZEN,

		UNFREEZING,
		FREEZING,

		STEPPING
	}

  	public final Queues queues = new Queues(this);

	private State state = State.UNFROZEN;

	private CommandSource actor;
	private int remainingTicks;

	private TickPhase targetPhase = new TickPhase(0, 0);
	private TickPhase currentPhase = new TickPhase(0, 0);

	@Override
	public boolean frozen() {
		return state == State.FROZEN;
	}

	@Override
	public TickPhase currentPhase() {
		return currentPhase;
	}

	@Override
	public TickPhase targetPhase() {
		return targetPhase;
	}

//  @Override
//  public Queues queues(){return queues;}

	//public static boolean freezing(){return state == State.FREEZING;}

	public int printDebugInfo(CommandSource c) {
		Messenger.m(c, "w remainingTicks: " + remainingTicks);
		Messenger.m(c, "w targetPhase: " + targetPhase);
		Messenger.m(c, "w currentPhase: " + currentPhase);
		Messenger.m(c, "w state: " + state.name());
		Messenger.m(c, "w Queue:");
		queues.printDebugInfo(c);
		return 1;
	}

	public void reset() {
		actor = null;
		remainingTicks = 0;
		targetPhase = new TickPhase(0, 0);
		currentPhase = new TickPhase(0, 0);
		state = State.UNFROZEN;
	}

	@Override
	public int when(CommandSource c) {
		String key = "";
		switch (state) {
			case FROZEN:
				key = "tickCommand.when.frozen";
				break;
			case UNFROZEN:
				key = "tickCommand.when.unfrozen";
				break;
			case FREEZING:
				key = "tickCommand.when.freezing";
				break;
			case UNFREEZING:
				key = "tickCommand.when.unfreezing";
				break;
			case STEPPING:
				key = "tickCommand.when.stepping";
				break;
		}
		Translations.m(c, key, currentPhase);
		return 1;
	}

	@Override
	public boolean shouldTick(ServerWorld level, int tickPhase) {
		TickPhase phase = new TickPhase(level, tickPhase);

		switch (state) {
			// Unfrozen cases --------------
			case UNFROZEN:
				currentPhase = phase;
				return true;

			case FREEZING:
				if (!phase.equals(targetPhase))
					return true;

				state = State.FROZEN;
				currentPhase = phase;
				return false;

			// Frozen cases ----------------
			case FROZEN:
				queues.end();
				return false;

			case UNFREEZING:
				queues.end();
				if (!phase.equals(currentPhase))
					return false;

				state = State.UNFROZEN;
				return true;

			case STEPPING:
				queues.end();
				if (!phase.equals(currentPhase))
					return false;

				if (remainingTicks == 0 && phase.dim == targetPhase.dim) {
					if (phase.phase == targetPhase.phase) {
						//stepping = false;
						state = State.FROZEN;
						queues.execute();
						return false;
					}

					// This block will only execute if the step has to end at a phase that doesn't currently exist
					if (phase.phase > targetPhase.phase) {
						// Go 2 phases back; From entityManagment to entity
						currentPhase = new TickPhase(phase.dim, TickPhase.GLOBAL_ENTITY);
						Translations.m(actor, "tickCommand.step.err.unloaded", phase);
						return false;
					}
				}

				if (phase.isLast())
					remainingTicks--;

				advancePhase(level);
				return true;
		}
		return true;
	}

	public void advancePhase(ServerWorld level) {
		currentPhase = currentPhase.next(level);
	}

	@Override
	public int freeze(CommandSource c, int phase) {
		if (state != State.UNFROZEN) {
			Translations.m(c, "tickCommand.freeze.err");
			return 0;
		}

		state = State.FREEZING;
		TickPhase tickPhase = new TickPhase((ServerWorld) c.getSourceWorld(), phase);
		targetPhase = tickPhase;
//		ServerNetworkHandler.sendFrozen(c.getSourceWorld(), tickPhase);
		Translations.m(c, "tickCommand.freeze.success", tickPhase);
		return 1;
	}

	@Override
	public int unfreeze(CommandSource c) {
		switch (state) {
			case FROZEN:
				state = State.UNFREEZING;
				queues.scheduleEnd();
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;
			case FREEZING:
				state = State.UNFROZEN;
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;
			case STEPPING:
				state = State.UNFREEZING;
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;
			default:
				Translations.m(c, "tickCommand.unfreeze.err");
				return 0;
		}
	}

	@Override
	public int toggleFreeze(CommandSource c, int phase) {
		// unfrozen -> freeze
		// unfreezing -> error
		// frozen, freezing, stepping -> unfreeze
		switch (state) {
			case UNFROZEN:
				state = State.FREEZING;
				TickPhase tickPhase = new TickPhase((ServerWorld) c.getSourceWorld(), phase);
				targetPhase = tickPhase;
//				ServerNetworkHandler.sendFrozen(c.getSourceWorld(), tickPhase);
				Translations.m(c, "tickCommand.freeze.success", tickPhase);
				return 1;

			case UNFREEZING:
				Translations.m(c, "tickCommand.freeze.err.unfreezing");
				return 0;

			case FROZEN:
				state = State.UNFREEZING;
				queues.scheduleEnd();
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;

			case FREEZING:
				state = State.UNFROZEN;
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;

			case STEPPING:
				state = State.UNFREEZING;
//				ServerNetworkHandler.sendUnfrozen(c.getSourceWorld());
				Translations.m(c, "tickCommand.unfreeze.success");
				return 1;
		}
		return 0;
	}

	@Override
	public int step(CommandSource c, int ticks, int phase) {
		TickPhase tickPhase = new TickPhase((ServerWorld) c.getSourceWorld(), phase);
		if (!canStep(c, ticks, tickPhase)) return 0;

		if (ticks == 1)
			Translations.m(c, "tickCommand.step.success.single", tickPhase, 1);
		else
			Translations.m(c, "tickCommand.step.success.multiple", tickPhase, ticks);

		actor = c;
		state = State.STEPPING;
		remainingTicks = ticks;
		targetPhase = tickPhase;
		if (ticks != 0 || !tickPhase.equals(currentPhase)) {
			queues.scheduleEnd();
//			ServerNetworkHandler.sendTickStep(c.getSourceWorld(), ticks, tickPhase);
		}
		return 1;
	}

	@Override
	public int phaseStep(CommandSource c, int count) {
		int currentPhaseInt = currentPhase.phase;
		int phase = currentPhaseInt + count;
		int ticks = phase / TickPhase.totalPhases;
		return step(c, phase < currentPhaseInt ? ticks + 1 : ticks, phase % TickPhase.totalPhases);
	}

	@Override
	public int stepToPhase(CommandSource c, int phase, boolean force) {
		return phase < currentPhase.phase && force ?
			step(c, 1, phase) :
			step(c, 0, phase);
	}

	public int step(CommandSource c, int ticks, TickPhase phase) {
		state = State.STEPPING;
		remainingTicks = ticks;
		targetPhase = phase;
		if (ticks != 0 || !phase.equals(currentPhase)) {
			queues.scheduleEnd();
//			ServerNetworkHandler.sendTickStep(c.getSourceWorld(), ticks, phase);
		}
		return 1;
	}


	@Override
	public boolean canStep(CommandSource c, int count, TickPhase phase) {
		if (state == State.STEPPING) {
			Translations.m(c, "tickCommand.step.err.stepping");
			return false;
		}

		if (state != State.FROZEN) {
			Translations.m(c, "tickCommand.step.err.notfrozen");
			return false;
		}

		if (count == 0 && phase.isPriorTo(currentPhase)) {
			Translations.m(c, "tickCommand.step.err.backwards");
			return false;
		}

		if (queues.scheduled) {
			Translations.m(c, "tickCommand.step.err.qstepping");
			return false;
		}

		return true;
	}

	@Override
	public boolean canStep(int count, TickPhase phase)
	{
		if(state != State.FROZEN)
			return false;

		if(count == 0 && phase.isPriorTo(currentPhase))
			return false;

		if(queues.scheduled)
			return false;

		return true;
	}
}
