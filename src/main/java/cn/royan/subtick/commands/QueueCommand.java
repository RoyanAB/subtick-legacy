package cn.royan.subtick.commands;

import cn.royan.subtick.Settings;
import cn.royan.subtick.interfaces.IQueues;
import cn.royan.subtick.queue.TickingQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
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
	public void run(MinecraftServer minecraftServer, CommandSource source, String[] strings) throws CommandException {

		if (strings.length == 1 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			step(source, strings[0], "", 1, Settings.subtickDefaultRange, false);
		}

		if (strings.length == 2 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			if ("force".equalsIgnoreCase(strings[1]))
				step(source, strings[0], "", 1, Settings.subtickDefaultRange, true);
			step(source, strings[0], "", Integer.parseInt(strings[1]), Settings.subtickDefaultRange, false);
		}

		if (strings.length == 3 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			Set<String> modes = TickingQueue.byCommandKey(strings[0]).getModes();
			if ("force".equalsIgnoreCase(strings[2]))
				step(source, strings[0], "", Integer.parseInt(strings[1]), Settings.subtickDefaultRange, true);
			if (modes.contains(strings[2]))
				step(source, strings[0], strings[2], Integer.parseInt(strings[1]), Settings.subtickDefaultRange, false);
		}

		if (strings.length == 4 && Arrays.asList(TickingQueue.commandKeys).contains(strings[0])) {
			if ("force".equalsIgnoreCase(strings[3]))
				step(source, strings[0], strings[2], Integer.parseInt(strings[1]), Settings.subtickDefaultRange, true);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List<String> getSuggestions(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings, @Nullable BlockPos blockPos) {
		if (strings.length == 1) {
			return suggestMatching(strings, Arrays.asList(TickingQueue.commandKeys));
		} else if (strings.length == 2) {
			return suggestMatching(strings, Collections.singletonList("1"));
		} else if (strings.length == 3) {
			List<String> modes = new ArrayList<>();
			if (Arrays.asList(TickingQueue.commandKeys).contains(strings[0]))
				modes.addAll(TickingQueue.byCommandKey(strings[0]).getModes());
			return suggestMatching(strings, modes);
		} else if (strings.length == 4) {
			return suggestMatching(strings, Collections.singletonList("force"));
		}
		return Collections.emptyList();
	}

	private static int step(CommandSource c, String commandKey, String modeKey, int count, int range, boolean force) {
		IQueues.get(c).schedule(c, TickingQueue.byCommandKey(commandKey), modeKey, count, new BlockPos(c.getSourcePos()), range, force);
		return 1;
	}
}

