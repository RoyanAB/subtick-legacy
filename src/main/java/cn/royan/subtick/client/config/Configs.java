package cn.royan.subtick.client.config;

import com.google.common.collect.ImmutableList;
import malilib.config.category.BaseConfigOptionCategory;
import malilib.config.category.ConfigOptionCategory;
import malilib.config.option.*;
import malilib.util.data.ModInfo;

import java.util.ArrayList;
import java.util.List;

public class Configs {
	public static final BooleanConfig
		SHOW_HUD = new BooleanConfig("showHUD", true, "Controls whether the hud is shown"),
		SMOOTH_CLIENTANIMATIONS = new BooleanConfig("smoothClientAnimations", false, "Smooth client animations with low tps settings");
	public static final ColorConfig
		STEPPED_BG = new ColorConfig("steppedBG", "#80000000", "Background color for things already stepped through"),
		STEPPED_TEXT = new ColorConfig("steppedText", "#FFAAAAAA", "Text color for things already stepped through"),
		STEPPED_DEPTH = new ColorConfig("steppedDepth", "#FF004040", "Text color for depth of things already stepped through"),
		STEPPING_BG = new ColorConfig("steppingBG", "#808000FF", "Background color for things being stepped through"),
		STEPPING_TEXT = new ColorConfig("steppingText", "#FFFFFFFF", "Text color for things being stepped through"),
		STEPPING_DEPTH = new ColorConfig("steppingDepth", "#FF00FFFF", "Text color for depth of things being stepped through"),
		TO_STEP_BG = new ColorConfig("toStepBG", "#80000000", "Background color for things not stepped through"),
		TO_STEP_TEXT = new ColorConfig("toStepText", "#FFFFFFFF", "Text color for things not stepped through"),
		TO_STEP_DEPTH = new ColorConfig("toStepDepth", "#FF00FFFF", "Text color for depth of things not stepped through"),
		NEW_BG = new ColorConfig("newBG", "#80808080", "Background color for newly scheduled things"),
		NEW_TEXT = new ColorConfig("newText", "#FFFFFFFF", "Text color for newly scheduled things"),
		NEW_DEPTH = new ColorConfig("newDepth", "#FF00FFFF", "Text color for depth of newly scheduled things"),
		SEPARATOR = new ColorConfig("separator", "#80FFFFFF", "Color for separating elements in the HUD"),
		POSITION = new ColorConfig("position", "#80FF0000", "Color for indicating the position in the HUD");
	public static final OptionListConfig<AlignConfig> HUD_ALIGNMENT = new OptionListConfig("hudAlignment", AlignConfig.TOP, AlignConfig.VALUES);
	public static final IntegerConfig
		HUD_OFFSET_X = new IntegerConfig("hudOffsetX", 0, "X offset for the HUD"),
		HUD_OFFSET_Y = new IntegerConfig("hudOffsetY", 0, "Y offset for the HUD"),
		MAX_QUEUE_SIZE = new IntegerConfig("maxQueueSize", 15, "Maximum number of elements in the queue HUD"),
		MAX_HIGHLIGHT_SIZE = new IntegerConfig("maxHighlightSize", 10, "Maximum number of highlighted elements in the queue HUD\nUseful to control the number of highlights when the queue is bigger than maxQueueSize");

	public static final ImmutableList<ConfigOption<?>> OPTIONS;
	public static final ConfigOptionCategory GENERAL;
	public static final List<ConfigOptionCategory> CATEGORIES = new ArrayList<>();

	public static final ModInfo MOD_INFO = new ModInfo("subtick", "Subtick");

	static {
		OPTIONS = ImmutableList.of(
			SHOW_HUD,
			SMOOTH_CLIENTANIMATIONS,
			STEPPED_BG,
			STEPPED_TEXT,
			STEPPED_DEPTH,
			STEPPING_BG,
			STEPPING_TEXT,
			STEPPING_DEPTH,
			TO_STEP_BG,
			TO_STEP_TEXT,
			TO_STEP_DEPTH,
			SEPARATOR,
			POSITION,
			HUD_ALIGNMENT,
			HUD_OFFSET_X,
			HUD_OFFSET_Y,
			MAX_QUEUE_SIZE,
			MAX_HIGHLIGHT_SIZE
		);
		GENERAL = BaseConfigOptionCategory.normal(MOD_INFO, "Options", OPTIONS);
		CATEGORIES.add(GENERAL);
	}
}
