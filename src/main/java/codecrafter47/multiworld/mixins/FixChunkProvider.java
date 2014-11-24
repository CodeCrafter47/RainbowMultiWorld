package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.api.WorldConfiguration;
import MultiWorld.mixin.Mixin;
import joebkt.HellRandomLevelSourceGenerator;
import joebkt.IChunkProvider;
import joebkt.RandomLevelSourceGenerator2;
import joebkt.WorldProvider;

/**
 * Created by florian on 23.11.14.
 */
@Mixin(joebkt._CustomWorld.class)
public abstract class FixChunkProvider extends WorldProvider{

	@Override
	public IChunkProvider getChunkProviderBasedOnSettings() {
		WorldConfiguration configuration = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension);
		if(configuration.getGenerationType() == GenerationType.NETHER)
			return new HellRandomLevelSourceGenerator(this.b_world, this.b_world.getWorldData().getGenStructures(), this.b_world.getSeedNumber());
		if(configuration.getGenerationType() == GenerationType.END)
			return new RandomLevelSourceGenerator2(this.b_world, this.b_world.getSeedNumber());
		return super.getChunkProviderBasedOnSettings();
	}

}
