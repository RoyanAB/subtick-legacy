package cn.royan.subtick.commands;

import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhaseCommand extends AbstractCommand {
	@Override
	public String getName() {
		return "phaseStep";
	}

	@Override
	public String getUsage(CommandSource commandSource) {
		return this.getName() + " <option>";
	}

	@Override
	public void run(MinecraftServer minecraftServer, CommandSource source, String[] strings) throws CommandException {
		if (strings.length == 0) {
			((ITickHandleable) source.getServer()).tickHandler().phaseStep(source, 1);
		}

		if (strings.length == 1) {
			if (Arrays.asList(TickPhase.commandSuggestions).contains(strings[0])) {
				((ITickHandleable) source.getServer()).tickHandler().stepToPhase(source, TickPhase.byCommandKey(strings[0]), false);
			} else {
				((ITickHandleable) source.getServer()).tickHandler().phaseStep(source, Integer.parseInt(strings[0]));
			}
		}

		if (strings.length == 2 && Arrays.asList(TickPhase.commandSuggestions).contains(strings[0])) {
			((ITickHandleable) source.getServer()).tickHandler().stepToPhase(source, TickPhase.byCommandKey(strings[0]), true);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List<String> getSuggestions(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings, @Nullable BlockPos blockPos) {
		if (strings.length == 1) {
			return suggestMatching(strings, Arrays.asList(TickPhase.commandSuggestions));
		} else if (strings.length == 2) {
			if (Arrays.asList(TickPhase.commandSuggestions).contains(strings[0])) {
				return suggestMatching(strings, Collections.singletonList("force"));
			}
		}
		return Collections.emptyList();
	}

}
