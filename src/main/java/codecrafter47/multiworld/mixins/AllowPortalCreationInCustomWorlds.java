package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;

/**
 * Created by florian on 26.11.14.
 */
@Mixin(joebkt.BlockFire.class)
public abstract class AllowPortalCreationInCustomWorlds extends BlockObject {

	protected AllowPortalCreationInCustomWorlds(Material var1) {
		super(var1);
	}

	@Shadow
	abstract boolean e(World var1, IntegerCoordinates var2);

	@Shadow
	public abstract int a(World var1);

	@Overwrite
	public void c_void(World var1, IntegerCoordinates var2, IBlockState var3) {
		if((var1.worldProvider.getDimenIdx() > 0 && (PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(var1.worldProvider.getDimenIdx()).getGenerationType() == GenerationType.END || PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(var1.worldProvider.getDimenIdx()).getGenerationType() == GenerationType.SINGLE_BIOME)) || !Blocks.portal.d(var1, var2)) {
			if(!World.canPutSomethingHere(var1, var2.getDown1()) && !this.e(var1, var2)) {
				var1.setToAir(var2);
			} else {
				var1.funcCoordBlockInt(var2, this, this.a(var1) + var1.random.nextInt(10));
			}
		}
	}
}
