package cn.royan.subtick.helpers;


import cn.royan.subtick.interfaces.ITickHandleable;
import cn.royan.subtick.network.ServerNetworkHandler;
import cn.royan.subtick.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.handler.CommandHandler;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerTickRateManager  {
	private static final float MIN_TICKRATE = 0.01f;
	protected float tickrate = 20.0f;
	protected long mspt = 50L;
	private final MinecraftServer server;
	/**
	 * Functional interface that listens for tickrate changes. This is
	 * implemented to allow tickrate compatibility with other mods etc.
	 */
	private final Map<String, BiConsumer<String, Float>> tickrateListeners = new HashMap<>();
	private long remainingWarpTicks = 0;
	private long tickWarpStartTime = 0;
	private long scheduledCurrentWarpTicks = 0;
	private ServerPlayerEntity warpResponsiblePlayer = null;
	private String tickWarpCallback = null;
	private CommandSource warpResponsibleSource = null;


	public ServerTickRateManager(MinecraftServer server) {
		this.server = server;
	}

	public float tickrate() {
		return tickrate;
	}

	public long mspt() {
		return mspt;
	}

	public boolean isInWarpSpeed() {
		return tickWarpStartTime != 0;
	}

	public Text requestGameToWarpSpeed(ServerPlayerEntity player, int advance, String callback, CommandSource source) {
		if (0 == advance) {
			tickWarpCallback = null;
			if (source != warpResponsibleSource) {
				warpResponsibleSource = null;
			}
			if (remainingWarpTicks > 0) {
				finishTickWarp();
				warpResponsibleSource = null;
				return Messenger.c("gi Warp interrupted");
			}
			return Messenger.c("ri No warp in progress");
		}
		if (remainingWarpTicks > 0) {
			String who = "Another player";
			if (warpResponsiblePlayer != null) {
				who = warpResponsiblePlayer.getName();
			}
			return Messenger.c("l " + who + " is already advancing time at the moment. Try later or ask them");
		}
		warpResponsiblePlayer = player;
		tickWarpStartTime = System.nanoTime();
		scheduledCurrentWarpTicks = advance;
		remainingWarpTicks = advance;
		tickWarpCallback = callback;
		warpResponsibleSource = source;
		return Messenger.c("gi Warp speed ....");
	}

	// should be private
	public void finishTickWarp() {

		long completed_ticks = scheduledCurrentWarpTicks - remainingWarpTicks;
		double milis_to_complete = System.nanoTime() - tickWarpStartTime;
		if (milis_to_complete == 0.0) {
			milis_to_complete = 1.0;
		}
		milis_to_complete /= 1000000.0;
		int tps = (int) (1000.0D * completed_ticks / milis_to_complete);
		double mspt = (milis_to_complete) / completed_ticks;
		scheduledCurrentWarpTicks = 0;
		tickWarpStartTime = 0;
		if (tickWarpCallback != null) {
			CommandHandler icommandmanager = this.warpResponsibleSource.getServer().getCommandHandler();
			try {
				icommandmanager.run(this.warpResponsibleSource, this.tickWarpCallback);
			} catch (Throwable var23) {
				if (warpResponsiblePlayer != null) {
					Messenger.m(warpResponsiblePlayer, "r Command Callback failed - unknown error: ", "rb /" + tickWarpCallback, "/" + tickWarpCallback);
				}
			}
			tickWarpCallback = null;
			warpResponsibleSource = null;
		}
		if (warpResponsiblePlayer != null) {
			Messenger.m(warpResponsiblePlayer, String.format("gi ... Time warp completed with %d tps, or %.2f mspt", tps, mspt));
			warpResponsiblePlayer = null;
		} else {
			Messenger.print_server_message(server, String.format("... Time warp completed with %d tps, or %.2f mspt", tps, mspt));
		}
		remainingWarpTicks = 0;

	}

	public boolean continueWarp() {
		if (((ITickHandleable) server).tickHandler().frozen())
		// Returning false so we don't have to run at max speed when doing nothing
		{
			return false;
		}
		if (remainingWarpTicks > 0) {
			if (remainingWarpTicks == scheduledCurrentWarpTicks) //first call after previous tick, adjust start time
			{
				tickWarpStartTime = System.nanoTime();
			}
			remainingWarpTicks -= 1;
			return true;
		} else {
			finishTickWarp();
			return false;
		}
	}


	public void setTickRate(float rate) {
		setTickRate(rate, true);
	}

	public void setTickRate(float rate, boolean update) {
		tickrate = rate;
		long msptt = (long) (1000.0 / tickrate);
		if (msptt <= 0L) {
			msptt = 1L;
			tickrate = 1000.0f;
		}
		mspt = msptt;
		if (update) {
			notifyTickrateListeners("carpet");
		}
	}

	private void tickrateChanged(String modId, float rate) {
		// Other mods might change the tickrate in a slightly
		// different way. Also allow for tickrates that don't
		// divide into 1000 here.

		if (rate < MIN_TICKRATE) {
			rate = MIN_TICKRATE;
		}

		tickrate = rate;
		mspt = (long) (1000.0f / tickrate);

		notifyTickrateListeners(modId);
	}

	private void notifyTickrateListeners(String originModId) {
		synchronized (tickrateListeners) {
			for (Map.Entry<String, BiConsumer<String, Float>> listenerEntry : tickrateListeners.entrySet()) {
				if (originModId == null || !originModId.equals(listenerEntry.getKey())) {
					listenerEntry.getValue().accept(originModId, tickrate);
				}
			}
		}
		ServerNetworkHandler.updateTickSpeedToConnectedPlayers(server);
	}

	public BiConsumer<String, Float> addTickrateListener(String modId, BiConsumer<String, Float> tickrateListener) {
		synchronized (tickrateListeners) {
			tickrateListeners.put(modId, tickrateListener);
		}
		return this::tickrateChanged;
	}
}
