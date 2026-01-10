package cn.royan.subtick;

import carpet.api.settings.*;

import static carpet.api.settings.RuleCategory.*;

public class SubtickSettings {
	@Rule(
		desc = "Enables /tick command",
		categories = {COMMAND, CREATIVE, FEATURE, "subtick"},
		validators = Validators.CarpetPermissionLevel.class,
		options = {"true", "false", "ops", "0", "2", "4"}
	)
	public static String tickCommand = "ops";

	@Rule(
		desc = "The default tick phase to freeze at and step to",
		options = {
			"weather",
			"mobSpawning",
			"chunkSource",
			"time",
			"tileTick",
			"randomTick",
			"village",
			"portalForcer",
			"blockEvent",
			"globalEntity",
			"entity",
			"blockEntity",
			"entityTracker"
		},
		strict = true,
		categories = "subtick"
	)
	public static String subtickDefaultPhase = "tileTick";

	@Rule(
		desc = "Text format for normal text in subtick message feedback (Uses carpet format, search for \"format(components, ...)\" in https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/api/Auxiliary.md)",
		strict = false,
		categories = "subtick"
	)
	public static String subtickTextFormat = "ig";

	@Rule(
		desc = "Text format for numbers in subtick message feedback (Uses carpet format, search for \"format(components, ...)\" in https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/api/Auxiliary.md)",
		strict = false,
		categories = "subtick"
	)
	public static String subtickNumberFormat = "iy";

	@Rule(
		desc = "Text format for phases in subtick message feedback (Uses carpet format, search for \"format(components, ...)\" in https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/api/Auxiliary.md)",
		strict = false,
		categories = "subtick"
	)
	public static String subtickPhaseFormat = "it";

	@Rule(
		desc = "Text format for dimensions in subtick message feedback (Uses carpet format, search for \"format(components, ...)\" in https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/api/Auxiliary.md)",
		strict = false,
		categories = "subtick"
	)
	public static String subtickDimensionFormat = "im";

	@Rule(
		desc = "Error text format for normal text in subtick message feedback (Uses carpet format, search for \"format(components, ...)\" in https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/api/Auxiliary.md)",
		strict = false,
		categories = "subtick"
	)
	public static String subtickErrorFormat = "ir";

	@Rule(
		desc = "Default range for queueStep",
		strict = false,
		categories = "subtick"
	)
	public static int subtickDefaultRange = 32;

	@Rule(
		desc = "Yeet Crash",
		categories = "subtick"
	)
	public static boolean yeetCrash = false;
}
