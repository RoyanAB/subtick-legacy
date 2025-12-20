package cn.royan.subtick;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {
	public String subtickDefaultPhase = "tileTick";

	public int subtickDefaultRange = 32;

	public String subtickTextFormat = "ig";

	public String subtickNumberFormat = "iy";

	public String subtickPhaseFormat = "it";

	public String subtickDimensionFormat = "im";

	public String subtickErrorFormat = "ir";

	public String language = "en_us";

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public void saveConfig(Path path) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				gson.toJson(this, writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Settings loadConfig(Path path) {
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				Settings loaded = gson.fromJson(reader, Settings.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Settings defaultSettings = new Settings();
		defaultSettings.saveConfig(path);
		return defaultSettings;
	}
}
