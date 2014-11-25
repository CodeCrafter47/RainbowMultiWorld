package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import WrapperObjects.PluginHelper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;

/**
 * Created by florian on 23.11.14.
 */
@Mixin(joebkt.WorldProvider.class)
public class FixBiomeGenerator {
	@Shadow
	protected World b_world;

	@Shadow
	protected OverworldBiomeGenerator biomeGenerator;

	@Shadow
	protected int dimension;

	@Shadow
	protected boolean d;

	@Shadow
	protected boolean skipNightAndDay;

	@Overwrite
	protected void determineBiomeGenerator() {
		LevelType var1 = this.b_world.getWorldData().getLevelType();
		if (var1 == LevelType.flat) {
			VillageRelated entry = VillageRelated.a_initRelated(this.b_world.getWorldData().getName());
			this.biomeGenerator = new BiomeGenerator(BiomeBase.getBiomeByIdxWithDefault(entry.a(), BiomeBase.OceanDup), 0.5F, this.dimension);
		}
		else if (var1 == LevelType.debug_all_block_states) {
			this.biomeGenerator = new BiomeGenerator(BiomeBase.Plains, 0.0F, this.dimension);
		}
		else if (this.dimension > 1) {
			GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getGenerationType();
			if (generationType == null) {
				this.biomeGenerator = new OverworldBiomeGenerator(this.b_world);
			}
			else {
				PluginMultiWorld.getInstance().getLogger().info("Injecting generationType into " + this.dimension + ": " + generationType.name());
				if (generationType == GenerationType.SINGLE_BIOME) {
					_WorldRegistration entry1 = _WorldMaster.GetRegistrationFromDimension(this.dimension);
					if (entry1 != null) {
						this.biomeGenerator = new BiomeGenerator(PluginHelper.TranslateBiomeBase(entry1.settings.biomeType), 0.0F, this.dimension);
					}
					else {
						this.biomeGenerator = new OverworldBiomeGenerator(this.b_world);
					}
				}
				else if (generationType == GenerationType.OVERWORLD) {
					this.biomeGenerator = new OverworldBiomeGenerator(this.b_world);
				}
				else if (generationType == GenerationType.NETHER) {
					this.biomeGenerator = new BiomeGenerator(BiomeBase.Hell, 0.0F, this.dimension);
					this.d = true;
					this.skipNightAndDay = true;
				}
				else if (generationType == GenerationType.END) {
					this.biomeGenerator = new BiomeGenerator(BiomeBase.TheEnd, 0.0F, this.dimension);
					this.skipNightAndDay = true;
				}
			}
		}
		else {
			this.biomeGenerator = new OverworldBiomeGenerator(this.b_world);
		}

	}
}
