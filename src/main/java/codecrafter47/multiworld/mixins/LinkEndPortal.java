package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;

/**
 * Created by florian on 28.11.14.
 */
@Mixin(BlockEndPortal.class)
public abstract class LinkEndPortal extends BlockContainer {

	protected LinkEndPortal(Material var1) {
		super(var1);
	}

	@Overwrite
	public void whatHappensWhenTouchBlock(World var1, IntegerCoordinates var2, IBlockState var3, EntityGeneric var4) {
		if(var4.vehicle == null && var4.rider == null && !var1.isStatic) {
			if(var1.getWorldData().dimensionIdx < 2) {
				var4.handleChangeDimension(1);
			} else {
				int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(var1.getWorldData().dimensionIdx).getEndPortalTarget();
				if(PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(var1.getWorldData().dimensionIdx).getGenerationType() == GenerationType.END){
					if(var4 instanceof EntityPlayer){
						((EntityPlayer) var4).setStatAsAcquired(Achievement.theEnd2);
						var4.worldEnt.killEntity(var4);
						((EntityPlayer) var4).viewingCredits = true;
						((EntityPlayer) var4).plrConnection.sendPacket(new Packet_ChangeGameState(4, 0.0F));
					}
				} else {
					if(endPortalTarget > -2)var4.handleChangeDimension(endPortalTarget);
				}
			}
		}

	}
}
