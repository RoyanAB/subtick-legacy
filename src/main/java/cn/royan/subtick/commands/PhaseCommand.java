package cn.royan.subtick.commands;

import cn.royan.subtick.SubtickMod;
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
			return;
		}

		if (strings.length == 1) {
			if (Arrays.asList(TickPhase.commandSuggestions).contains(strings[0])) {
				((ITickHandleable) source.getServer()).tickHandler().stepToPhase(source, TickPhase.byCommandKey(strings[0]), false);
			} else {
				((ITickHandleable) source.getServer()).tickHandler().phaseStep(source, parseInt(strings[0], 1));
			}
			return;
		}

		if (strings.length == 2 &&
			Arrays.asList(TickPhase.commandSuggestions).contains(strings[0]) &&
			strings[1].equalsIgnoreCase("force")
		) {
			((ITickHandleable) source.getServer()).tickHandler().stepToPhase(source, TickPhase.byCommandKey(strings[0]), true);
		}
	}

	@Override
	public boolean canUse(MinecraftServer server, CommandSource source) {
		return SubtickMod.tickCommand(source, this.getName());
	}

	@Override
	public List<String> getSuggestions(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings, @Nullable BlockPos blockPos) {
		if (strings.length == 1) {
			return suggestMatching(strings, TickPhase.commandSuggestions);
		} else if (strings.length == 2) {
			if (Arrays.asList(TickPhase.commandSuggestions).contains(strings[0])) {
				return suggestMatching(strings, "force");
			}
		}
		return Collections.emptyList();
	}

}
