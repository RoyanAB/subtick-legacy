package cn.royan.subtick;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class SubtickMixinCanceller implements MixinCanceller {
	@Override
	public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
		return mixinClassName.contains("carpet.mixins.tick");
	}
}
