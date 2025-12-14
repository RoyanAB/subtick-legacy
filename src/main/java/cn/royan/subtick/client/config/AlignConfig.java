package cn.royan.subtick.client.config;

import cn.royan.subtick.utils.Translations;
import com.google.common.collect.ImmutableList;
import malilib.config.value.BaseOptionListConfigValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;

import java.util.HashMap;
import java.util.Map;

public class AlignConfig extends BaseOptionListConfigValue {
	public static final AlignConfig
		TOP_LEFT = new AlignConfig(0, 0, "top_left"),
		TOP = new AlignConfig(1, 0, "top"),
		TOP_RIGHT = new AlignConfig(2, 0, "top_right"),
		LEFT = new AlignConfig(0, 1, "left"),
		CENTER = new AlignConfig(1, 1, "center"),
		RIGHT = new AlignConfig(2, 1, "right"),
		BOTTOM_LEFT = new AlignConfig(0, 2, "bottom_left"),
		BOTTOM = new AlignConfig(1, 2, "bottom"),
		BOTTOM_RIGHT = new AlignConfig(2, 2, "bottom_right");

	public static final ImmutableList<AlignConfig> VALUES = ImmutableList.of(TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);

	private final int x, y;
	private final String translationKey;

	private static final Map<String, AlignConfig> byString = new HashMap<>();

	public AlignConfig(int x, int y, String key) {
		super(key, key);
		this.x = x;
		this.y = y;
		translationKey = key;
	}

	static {
		byString.put("top_left", TOP_LEFT);
		byString.put("top", TOP);
		byString.put("top_right", TOP_RIGHT);
		byString.put("left", LEFT);
		byString.put("center", CENTER);
		byString.put("right", RIGHT);
		byString.put("bottom_left", BOTTOM_LEFT);
		byString.put("bottom", BOTTOM);
		byString.put("bottom_right", BOTTOM_RIGHT);
	}

	public int getX(int w) {
		return (int) (x * ((getScaledWindowHeight() - w) / 2));
	}

	public int getY(int h) {
		return (int) (y * ((getScaledWindowWidth() - h) / 2));
	}

	public static double getScaledWindowWidth() {
		Window sr = new Window(Minecraft.getInstance());
		return sr.getScaledWidth();
	}

	public static double getScaledWindowHeight() {
		Window sr = new Window(Minecraft.getInstance());
		return sr.getScaledHeight();
	}

	@Override
	public String getDisplayName() {
		return Translations.tr("subtick.client.align." + translationKey);
	}
}
