package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.EntityGeneric;
import joebkt.IntegerCoordinates;
import joebkt.MathHelper;
import joebkt.WorldServer;

/**
 * Created by florian on 26.11.14.
 */
@Mixin(joebkt.PlayerList.class)
public class FixCustomWorldNetherSpawnPosition {

	@Overwrite
	public void changeDimensionDetailed(EntityGeneric entity, int dimension, WorldServer fromWorld, WorldServer toWorld) {
		double xCoord = entity.xCoord;
		double zCoord = entity.zCoord;
		double factor = 8.0D;
		float yaw = entity.yaw;
		fromWorld.methodProfiler.capture("moving");
		if(entity.dimension == -1 || (entity.dimension > 2 && PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(entity.dimension).getGenerationType() == GenerationType.NETHER)) {
			// to nether
			xCoord = MathHelper.a(xCoord / factor, toWorld.getObjectRelatedToGrowShrink().b() + 16.0D, toWorld.getObjectRelatedToGrowShrink().d() - 16.0D);
			zCoord = MathHelper.a(zCoord / factor, toWorld.getObjectRelatedToGrowShrink().c() + 16.0D, toWorld.getObjectRelatedToGrowShrink().e() - 16.0D);
			entity.setPositionRotation(xCoord, entity.yCoord, zCoord, entity.yaw, entity.pitch);
			if(entity.isNotDead()) {
				fromWorld.entRelatedChunkCheck(entity, false);
			}
		} else if(entity.dimension != 1 || (entity.dimension > 2 && PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(entity.dimension).getGenerationType() == GenerationType.OVERWORLD)) {
			// from nether
			xCoord = MathHelper.a(xCoord * factor, toWorld.getObjectRelatedToGrowShrink().b() + 16.0D, toWorld.getObjectRelatedToGrowShrink().d() - 16.0D);
			zCoord = MathHelper.a(zCoord * factor, toWorld.getObjectRelatedToGrowShrink().c() + 16.0D, toWorld.getObjectRelatedToGrowShrink().e() - 16.0D);
			entity.setPositionRotation(xCoord, entity.yCoord, zCoord, entity.yaw, entity.pitch);
			if(entity.isNotDead()) {
				fromWorld.entRelatedChunkCheck(entity, false);
			}
		} else {
			// portal to end
			IntegerCoordinates spawnCoordinates = toWorld.getDefaultSpawnCoordsMaybe();

			if(spawnCoordinates == null) {
				spawnCoordinates = new IntegerCoordinates(0, 65, 0);
			}

			xCoord = (double)spawnCoordinates.getX();
			entity.yCoord = (double)spawnCoordinates.getY();
			zCoord = (double)spawnCoordinates.getZ();
			entity.setPositionRotation(xCoord, entity.yCoord, zCoord, 90.0F, 0.0F);
			if(entity.isNotDead()) {
				fromWorld.entRelatedChunkCheck(entity, false);
			}
		}

		fromWorld.methodProfiler.checkIfTakingTooLong();
		if(dimension != 1) {
			fromWorld.methodProfiler.capture("placing");
			xCoord = (double)MathHelper.restrictToRange((int)xCoord, -29999872, 29999872);
			zCoord = (double)MathHelper.restrictToRange((int)zCoord, -29999872, 29999872);
			if(entity.isNotDead()) {
				entity.setPositionRotation(xCoord, entity.yCoord, zCoord, entity.yaw, entity.pitch);
				toWorld.getPortals().constructReturnPortal(entity, yaw);
				toWorld.addEntity(entity);
				toWorld.entRelatedChunkCheck(entity, false);
			}

			fromWorld.methodProfiler.checkIfTakingTooLong();
		}

		entity.setEntityWorld(toWorld);
	}
}
