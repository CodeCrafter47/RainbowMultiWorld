package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.ClassRelatedToGrowShrink;
import joebkt.IntegerCoordinates;
import joebkt.WorldProvider;

/**
 * Created by florian on 25.11.14.
 */
@Mixin(joebkt._CustomWorld.class)
public abstract class AddWorldTypeSpecificReturnValues extends WorldProvider {

	@Override
	protected void setupSomeFloatValues() {
		if (PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType() == GenerationType.NETHER) {
			float var1 = 0.1F;

			for (int var2 = 0; var2 <= 15; ++var2) {
				float var3 = 1.0F - (float) var2 / 15.0F;
				this.f[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
			}
		}
		else {
			super.setupSomeFloatValues();
		}
	}

	@Override
	public boolean d() {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.NETHER || generationType == GenerationType.END) {
			return false;
		}
		else {
			return super.d();
		}
	}

	@Override
	public boolean isTopMostBlockGrass(int var1, int var2) {
		if (PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType() == GenerationType.NETHER) {
			return false;
		}
		else if (PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType() == GenerationType.NETHER) {
			return this.b_world.getTopMostBlock(new IntegerCoordinates(var1, 0, var2)).getMaterial().isSolid();
		}
		else {
			return super.isTopMostBlockGrass(var1, var2);
		}
	}

	@Override
	public float calcTimeRelatedFloat(long var1, float var3) {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.NETHER) {
			return 0.5F;
		}
		else if (generationType == GenerationType.END) {
			return 0.0F;
		}
		else {
			return super.calcTimeRelatedFloat(var1, var3);
		}
	}

	@Override
	public boolean isOverworld_ContainsSpawn() {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.NETHER || generationType == GenerationType.END) {
			return false;
		} else {
			return super.isOverworld_ContainsSpawn();
		}
	}

	@Override
	public IntegerCoordinates getDefaultSpawnCoordinates() {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.END) {
			return new IntegerCoordinates(100, 50, 0);
		}
		else {
			return super.getDefaultSpawnCoordinates();
		}
	}

	@Override
	public int getSeaLevel() {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.END) {
			return 50;
		}
		else {
			return super.getSeaLevel();
		}
	}

	@Override public ClassRelatedToGrowShrink r() {
		GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
		if (generationType == GenerationType.NETHER) {
			return new RelatedToGrowShrinkNether();
		}
		else {
			return super.r();
		}
	}

}
