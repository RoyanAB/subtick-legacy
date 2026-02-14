package cn.royan.subtick.utils;

import cn.royan.subtick.SubtickMod;
import cn.royan.subtick.queue.TickingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.command.source.CommandSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class Translations {
	private static Map<String, String> translationMap;

	public static Map<String, String> getTranslationFromResourcePath(String lang) {
		InputStream langFile = Translations.class.getClassLoader().getResourceAsStream(String.format("assets/subtick/lang/%s.json", lang));
		if (langFile == null) {
			if (lang.equals("en_us"))
				return Collections.emptyMap();
			else
				getTranslationFromResourcePath("en_us");
		}
		String jsonData;
		try {
			jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		Gson gson = new GsonBuilder().setLenient().create(); // lenient allows for comments
		translationMap = gson.fromJson(jsonData, new TypeToken<Map<String, String>>() {
		}.getType());
		return translationMap;
	}

	public static String tr(String key) {
		return translationMap == null ? key : translationMap.getOrDefault(key, key);
	}

	public static String[] tr(String key, TickPhase phase, Integer n) {
		String t = (key.contains(".err") ? SubtickMod.subtickErrorFormat() : SubtickMod.subtickTextFormat()) + " ";
		String tr = t + tr(key);

		t = "\0" + t;
		if (phase != null)
			tr = tr.replace("{dim}", "\0" + dim(phase) + t).replace("{phase}", "\0" + phase(phase) + t);
		if (n != null)
			tr = tr.replace("{n}", "\0" + n(n) + t);

		return tr.split("\0");
	}

	public static String[] tr(String key, TickPhase phase) {
		String t = (key.contains(".err") ? SubtickMod.subtickErrorFormat() : SubtickMod.subtickTextFormat()) + " ";
		String tr = t + tr(key);

		t = "\0" + t;
		if (phase != null)
			tr = tr.replace("{dim}", "\0" + dim(phase) + t).replace("{phase}", "\0" + phase(phase) + t);

		return tr.split("\0");
	}


	public static String[] tr(String key, TickingQueue queue, Integer n, StackTraceElement[] stackTraceElements) {
		String t = t(key.contains(".err"));
		String tr = t + tr(key);

		t = "\0" + t;
		if (queue != null) {
			tr = tr.replace("{queue}", "\0" + queue(queue) + t);
			tr = tr.replace("{queues}", "\0" + queues(queue) + t);
		}
		if (n != null)
			tr = tr.replace("{n}", "\0" + n(n) + t);
		if (stackTraceElements != null)
			tr = tr.replace("{stack}", "\0" + stack(stackTraceElements) + t);

		return tr.split("\0");
	}

	public static void m(CommandSource source, String key) {
		Messenger.m(source, t(key.contains(".err")) + tr("subtick.feedback." + key));
	}

	public static void m(CommandSource source, String key, TickPhase phase) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, phase));
	}

	public static void m(CommandSource source, String key, TickPhase phase, int n) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, phase, n));
	}

	public static void m(CommandSource source, String key, TickingQueue queue) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, queue, null, null));
	}

	public static void m(CommandSource source, String key, TickingQueue queue, int n) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, queue, n, null));
	}

	public static void m(CommandSource source, String key, TickingQueue queue, StackTraceElement[] stackTraceElements) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, queue, null, stackTraceElements));
	}

	public static String t(boolean err) {
		return err ? SubtickMod.subtickErrorFormat() + " " : SubtickMod.subtickTextFormat() + " ";
	}

	public static String n(int x) {
		return SubtickMod.subtickNumberFormat() + " " + x;
	}

	public static String queue(TickingQueue queue) {
		return SubtickMod.subtickPhaseFormat() + " " + queue.getName();
	}

	public static String queues(TickingQueue queue) {
		return SubtickMod.subtickPhaseFormat() + " " + queue.getNamePlural();
	}

	public static String phase(TickPhase phase) {
		return SubtickMod.subtickPhaseFormat() + " " + phase.getPhaseName();
	}

	public static String dim(TickPhase phase) {
		String path = phase.getPath();
		return SubtickMod.subtickDimensionFormat() + " " + path.substring(0, 1).toUpperCase() + path.substring(1)
			+ "\0^" + SubtickMod.subtickDimensionFormat() + " " + path;
	}

	public static String stack(StackTraceElement[] stackTraceElements) {
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < stackTraceElements.length; i++) {
			sb.append(stackTraceElements[i].toString()).append("\n");
		}
		return SubtickMod.subtickErrorFormat() + " StackTrace"
			+ "\0^" + "w " + sb;
	}
}
