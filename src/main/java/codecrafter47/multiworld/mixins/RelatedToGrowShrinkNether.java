package codecrafter47.multiworld.mixins;

import joebkt.ClassRelatedToGrowShrink;

/**
* Created by florian on 25.11.14.
*/
public class RelatedToGrowShrinkNether extends ClassRelatedToGrowShrink {
	@Override
	public double f() {
		return super.f() / 8.0D;
	}

	@Override
	public double g() {
		return super.g() / 8.0D;
	}
}
