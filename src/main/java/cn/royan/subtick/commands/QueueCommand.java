package cn.royan.subtick.commands;

import cn.royan.subtick.SubtickMod;
import cn.royan.subtick.interfaces.IQueues;
import cn.royan.subtick.queue.TickingQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QueueCommand extends AbstractCommand {
	@Override
	public String getName() {
		return "queueStep";
	}

	@Override
	public String getUsage(CommandSource commandSource) {
		return this.getName() + " <option>";
	}

	@Override
	public void run(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings) throws CommandException {
		if (strings.length == 0) {
			throw new IncorrectUsageException(getUsage(commandSource));
		}

		if (strings.length == 1 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			step(commandSource, strings[0], "", 1, SubtickMod.settings.subtickDefaultRange, false);
			return;
		}

		if (strings.length == 2 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			if ("force".equalsIgnoreCase(strings[1]))
				step(commandSource, strings[0], "", 1, SubtickMod.settings.subtickDefaultRange, true);
			step(commandSource, strings[0], "", parseInt(strings[1], 1), SubtickMod.settings.subtickDefaultRange, false);
			return;
		}

		if (strings.length == 3 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			Set<String> modes = TickingQueue.byCommandKey(strings[0]).getModes();
			if ("force".equalsIgnoreCase(strings[2]))
				step(commandSource, strings[0], "", parseInt(strings[1], 1), SubtickMod.settings.subtickDefaultRange, true);
			if (modes.contains(strings[2]))
				step(commandSource, strings[0], strings[2], parseInt(strings[1], 1), SubtickMod.settings.subtickDefaultRange, false);
			return;
		}

		if (strings.length == 4 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			if ("force".equalsIgnoreCase(strings[3]))
				step(commandSource, strings[0], strings[2], parseInt(strings[1], 1), SubtickMod.settings.subtickDefaultRange, true);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List<String> getSuggestions(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings, @Nullable BlockPos blockPos) {
		if (strings.length == 1) {
			return suggestMatching(strings, TickingQueue.commandKeys);
		} else if (strings.length == 2) {
			return suggestMatching(strings, "1", "force");
		} else if (strings.length == 3) {
			List<String> modes = new ArrayList<>();
			if (Arrays.asList(TickingQueue.commandKeys).contains(strings[0]))
				modes.addAll(TickingQueue.byCommandKey(strings[0]).getModes());
			modes.add("force");
			return suggestMatching(strings, modes);
		} else if (strings.length == 4) {
			return suggestMatching(strings, "force");
		}
		return Collections.emptyList();
	}

	private static void step(CommandSource c, String commandKey, String modeKey, int count, int range, boolean force) {
		IQueues.get(c).schedule(c, TickingQueue.byCommandKey(commandKey), modeKey, count, new BlockPos(c.getCommandSourcePos()), range, force);
	}
}

