package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.manager.StorageManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created by florian on 26.11.14.
 */
@Mixin(BlockFire.class)
public abstract class MixinBlockFire {

	@Shadow
	private boolean canNeighborCatchFire(World var1, BlockPos var2) {
		return false;
	}

	@Shadow
	public abstract int tickRate(World var1);

	@Overwrite
	public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
		StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
		if(var1.provider.getDimensionType().getId() > 0 || (var1 instanceof CustomWorldServer && storageManager.getCustomConfig(((CustomWorldServer)var1).getWorldId()).getGenerationType() == GenerationType.SINGLE_BIOME) || !Blocks.PORTAL.trySpawnPortal(var1, var2)) {
			if(!var1.getBlockState(var2.down()).isFullyOpaque() && !this.canNeighborCatchFire(var1, var2)) {
				var1.setBlockToAir(var2);
			} else {
				var1.scheduleUpdate(var2, (Block) (Object) this, this.tickRate(var1) + var1.rand.nextInt(10));
			}
		}
	}
}
