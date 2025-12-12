package cn.royan.subtick.utils;

import cn.royan.subtick.Settings;
import cn.royan.subtick.queue.TickingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.command.source.CommandSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Translations {
	private static Map<String, String> translationMap;

	public static Map<String, String> getTranslationFromResourcePath(String path) {
		String dataJSON;
		try {
			dataJSON = IOUtils.toString(
				Objects.requireNonNull(Translations.class.getClassLoader().getResourceAsStream(path)),
				StandardCharsets.UTF_8);
		} catch (NullPointerException | IOException e) {
			return null;
		}
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		return gson.fromJson(dataJSON, new TypeToken<LinkedHashMap<String, String>>() {
		}.getType());
	}

	public static void updateLanguage(String lang) {
		Map<String, String> translations = getTranslationFromResourcePath(String.format("assets/subtick/lang/%s.json", lang));
		translations.entrySet().removeIf(e -> e.getKey().startsWith("//"));
		if (translations.isEmpty()) {
			translationMap = null;
			return;
		}
		translationMap = translations;
	}

	public static String tr(String key) {
		return translationMap == null ? key : translationMap.getOrDefault(key, key);
	}


	public static String[] tr(String key, TickPhase phase, Integer n) {
		String t = (key.contains(".err") ? Settings.subtickErrorFormat : Settings.subtickTextFormat) + " ";
		String tr = t + tr(key);

		t = "\0" + t;
		if (phase != null)
			tr = tr.replace("{dim}", "\0" + dim(phase) + t).replace("{phase}", "\0" + phase(phase) + t);
		if (n != null)
			tr = tr.replace("{n}", "\0" + n(n) + t);

		return tr.split("\0");
	}

	public static String[] tr(String key, TickPhase phase) {
		String t = (key.contains(".err") ? Settings.subtickErrorFormat : Settings.subtickTextFormat) + " ";
		String tr = t + tr(key);

		t = "\0" + t;
		if (phase != null)
			tr = tr.replace("{dim}", "\0" + dim(phase) + t).replace("{phase}", "\0" + phase(phase) + t);

		return tr.split("\0");
	}


	public static String[] tr(String key, TickingQueue queue, Integer n) {
		String t = t(key.contains(".err"));
		String tr = t + tr(key);

		t = "\0" + t;
		if (queue != null) {
			tr = tr.replace("{queue}", "\0" + queue(queue) + t);
			tr = tr.replace("{queues}", "\0" + queues(queue) + t);
		}
		if (n != null)
			tr = tr.replace("{n}", "\0" + n(n) + t);

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
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, queue, null));
	}

	public static void m(CommandSource source, String key, TickingQueue queue, int n) {
		Messenger.m(source, (Object[]) tr("subtick.feedback." + key, queue, n));
	}

	public static String t(boolean err) {
		return err ? Settings.subtickErrorFormat + " " : Settings.subtickTextFormat + " ";
	}

	public static String n(int x) {
		return Settings.subtickNumberFormat + " " + x;
	}

	public static String queue(TickingQueue queue) {
		return Settings.subtickPhaseFormat + " " + queue.getName();
	}

	public static String queues(TickingQueue queue) {
		return Settings.subtickPhaseFormat + " " + queue.getNamePlural();
	}

	public static String phase(TickPhase phase) {
		return Settings.subtickPhaseFormat + " " + phase.getPhaseName();
	}

	public static String dim(TickPhase phase) {
		String path = phase.getPath();
		return Settings.subtickDimensionFormat + " " + path.substring(0, 1).toUpperCase() + path.substring(1)
			+ "\0^" + Settings.subtickDimensionFormat + " " + path;
	}
}
