package cn.royan.subtick.commands;

import cn.royan.subtick.Settings;
import cn.royan.subtick.interfaces.MinecraftServerInterface;
import cn.royan.subtick.helpers.ServerTickRateManager;
import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.network.ServerNetworkHandler;
import cn.royan.subtick.utils.Messenger;
import cn.royan.subtick.utils.TickPhase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TickCommand extends AbstractCommand {
    @Override
    public String getName() {
        return "tick";
    }

    @Override
    public String getUsage(CommandSource commandSource) {
        return this.getName() + " <option>";
    }

    @Override
    public void run(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings) throws CommandException {
        if (strings.length == 1) {
            String action = strings[0].toLowerCase();
            switch (action) {
                case "freeze":
                    toggleFreeze(commandSource, Settings.subtickDefaultPhase);
                    break;
                case "step":
                    step(commandSource, 1, Settings.subtickDefaultPhase);
                    break;
                case "rate":
                    queryTps(commandSource);
                    break;
                case "superhot":
                    toggleSuperHot(commandSource);
                    break;
                case "warp":
                    setWarp(commandSource, 0, null);
                    break;
                case "when":
                    freezeStatus(commandSource);
                    break;
                default:
                    break;
            }
        }

        if (strings.length == 2 && "freeze".equalsIgnoreCase(strings[0])) {
            if ("status".equalsIgnoreCase(strings[1])) {
                freezeStatus(commandSource);
            } else if ("on".equalsIgnoreCase(strings[1])) {
                setFreeze(commandSource, Settings.subtickDefaultPhase, true);
            } else if ("off".equalsIgnoreCase(strings[1])) {
                setFreeze(commandSource, Settings.subtickDefaultPhase, false);
            } else if (Arrays.asList(TickPhase.commandSuggestions).contains(strings[1]))
                setFreeze(commandSource, strings[1], true);
        }

        if (strings.length == 2 && "step".equalsIgnoreCase(strings[0])) {
            step(commandSource, MathHelper.clamp(Integer.parseInt(strings[1]), 1, 72000), Settings.subtickDefaultPhase);
        }

        if (strings.length == 2 && "rate".equalsIgnoreCase(strings[0])) {
            setTps(commandSource, MathHelper.clamp(Float.parseFloat(strings[1]), 0.1F, 500.0F));
        }

        if (strings.length == 2 && "warp".equalsIgnoreCase(strings[0])) {
            setWarp(commandSource, Math.max(Integer.parseInt(strings[1]), 1), null);
        }

        if (
                strings.length == 3 && "freeze".equalsIgnoreCase(strings[0]) &&
                        "on".equalsIgnoreCase(strings[1]) &&
                        Arrays.asList(TickPhase.commandSuggestions).contains(strings[2])
        ) {
            setFreeze(commandSource, strings[2], true);
        }

        if (
                strings.length == 3 && "step".equalsIgnoreCase(strings[0]) &&
                        Arrays.asList(TickPhase.commandSuggestions).contains(strings[2])
        ) {
            step(commandSource, MathHelper.clamp(Integer.parseInt(strings[1]), 1, 72000), strings[2]);
        }

        if (strings.length == 3 && "warp".equalsIgnoreCase(strings[0])) {
            setWarp(commandSource, Math.max(Integer.parseInt(strings[1]), 1), strings[2]);
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getSuggestions(MinecraftServer minecraftServer, CommandSource commandSource, String[] strings, @Nullable BlockPos blockPos) {
        if (strings.length == 1) {
            return suggestMatching(strings, Arrays.asList("freeze", "step", "rate", "superhot", "warp"));
        } else if (strings.length == 2) {
            if ("freeze".equalsIgnoreCase(strings[0])) {
                ArrayList<String> suggestions = new ArrayList<>(Arrays.asList(TickPhase.commandSuggestions));
                suggestions.addAll(Arrays.asList("status", "on", "off"));
                return suggestMatching(strings, suggestions);
            }
            if ("step".equalsIgnoreCase(strings[0]))
                return suggestMatching(strings, Collections.singletonList("20"));
            if ("rate".equalsIgnoreCase(strings[0]))
                return suggestMatching(strings, Collections.singletonList("20"));
            if ("warp".equalsIgnoreCase(strings[0]))
                return suggestMatching(strings, Arrays.asList("3600", "72000"));
        } else if (strings.length == 3) {
            if ("freeze".equalsIgnoreCase(strings[0]) && "on".equalsIgnoreCase(strings[1]))
                return suggestMatching(strings, Arrays.asList(TickPhase.commandSuggestions));
            if ("step".equalsIgnoreCase(strings[0]))
                return suggestMatching(strings, Arrays.asList(TickPhase.commandSuggestions));
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private static int freezeStatus(CommandSource source) {
        ((ITickHandleable) source.getServer()).tickHandler().when(source);
        return 1;
    }

    private static int setFreeze(CommandSource source, String phase, boolean freeze) {
        if (freeze)
            ((ITickHandleable) source.getServer()).tickHandler().freeze(source, TickPhase.byCommandKey(phase));
        else
            ((ITickHandleable) source.getServer()).tickHandler().unfreeze(source);
        return 1;
    }

    private static int toggleFreeze(CommandSource source, String phase) {
        ((ITickHandleable) source.getServer()).tickHandler().toggleFreeze(source, TickPhase.byCommandKey(phase));
        return 1;
    }

    private static int step(CommandSource source, int advance, String phase) {
        ((ITickHandleable) source.getServer()).tickHandler().step(source, advance, TickPhase.byCommandKey(phase));
        return 1;
    }

    // RATE & WARP
    private static int setTps(CommandSource source, float tps) {
        ServerTickRateManager trm = ((MinecraftServerInterface) source.getServer()).getTickRateManager();
        trm.setTickRate(tps, true);
        queryTps(source);
        return (int) tps;
    }

    private static int queryTps(CommandSource source) {
        ServerTickRateManager trm = ((MinecraftServerInterface) source.getServer()).getTickRateManager();

        Messenger.m(source, "w Current tps is: ", String.format("wb %.1f", trm.tickrate()));
        return (int) trm.tickrate();
    }

    private static int toggleSuperHot(CommandSource source) {
        ServerTickRateManager trm = ((MinecraftServerInterface) source.getServer()).getTickRateManager();
        trm.setSuperHot(!trm.isSuperHot());
        ServerNetworkHandler.updateSuperHotStateToConnectedPlayers(source.getServer());
        if (trm.isSuperHot()) {
            Messenger.m(source, "gi Superhot enabled");
        } else {
            Messenger.m(source, "gi Superhot disabled");
        }
        return 1;
    }

    private static int setWarp(CommandSource source, int advance, String tail_command) {
        ServerPlayerEntity player;
        if (source.asEntity() instanceof ServerPlayerEntity)
            player = ((ServerPlayerEntity) source.asEntity());
        else
            player = null; // may be null
        ServerTickRateManager trm = ((MinecraftServerInterface) source.getServer()).getTickRateManager();
        Text message = trm.requestGameToWarpSpeed(player, advance, tail_command, source);
        source.sendMessage(message);
        return 1;
    }
}
