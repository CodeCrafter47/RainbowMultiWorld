package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;

/**
 * Created by florian on 28.11.14.
 */
@Mixin(joebkt.arf_PortalRelated.class)
public abstract class FixConstructedPortalType {

	@Shadow
	private WorldServer a;

	@Shadow
	public abstract boolean b(EntityGeneric var1, float var2);

	@Shadow
	public abstract boolean a(EntityGeneric var1);

	@Overwrite
	public void constructReturnPortal(EntityGeneric var1, float var2) {
		if(this.a.worldProvider.getDimenIdx() != 1 && PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.a.worldProvider.getDimenIdx()).getGenerationType() != GenerationType.END) {
			if(!this.b(var1, var2)) {
				this.a(var1);
				this.b(var1, var2);
			}
		} else {
			int var3 = MathHelper.floor_of_double(var1.xCoord);
			int var4 = MathHelper.floor_of_double(var1.yCoord) - 1;
			int var5 = MathHelper.floor_of_double(var1.zCoord);
			byte var6 = 1;
			byte var7 = 0;

			for(int var8 = -2; var8 <= 2; ++var8) {
				for(int var9 = -2; var9 <= 2; ++var9) {
					for(int var10 = -1; var10 < 3; ++var10) {
						int var11 = var3 + var9 * var6 + var8 * var7;
						int var12 = var4 + var10;
						int var13 = var5 + var9 * var7 - var8 * var6;
						boolean var14 = var10 < 0;
						this.a.setCoordToBlockState(new IntegerCoordinates(var11, var12, var13), var14? Blocks.OBSIDIAN.toBlockState():Blocks.AIR.toBlockState());
					}
				}
			}

			var1.setPositionRotation((double)var3, (double)var4, (double)var5, var1.yaw, 0.0F);
			var1.motX = var1.motY = var1.motZ = 0.0D;
		}

	}
}
