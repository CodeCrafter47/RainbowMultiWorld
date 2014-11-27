package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import PluginReference.MC_EventInfo;
import PluginReference.MC_Location;
import PluginReference.PluginInfo;
import WrapperObjects.Entities.EntityWrapper;
import WrapperObjects.Entities.PlayerWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;

/**
 * Created by florian on 27.11.14.
 */
@Mixin(WrapperObjects.Entities.PlayerWrapper.class)
public class FixCrossDimensionTeleport extends EntityWrapper {

	@Shadow
	public EntityPlayer plr;

	public FixCrossDimensionTeleport(EntityGeneric argEnt) {
		super(argEnt);
	}

	@Overwrite
	@SneakyThrows
	public void teleport(MC_Location loc) {
		if (loc != null) {
			plr.setVehicle(null);
			_SerializableLocation sloc = new _SerializableLocation(loc.x, loc.y, loc.z, loc.dimension, loc.yaw, loc.pitch);
			if (loc.dimension != this.plr.dimension) {
				// this.plr.handleChangeDimension(loc.dimension, false); no no no
				int newDimen = loc.dimension;
				MC_EventInfo ei = new MC_EventInfo();
				PlayerWrapper pWrap = new PlayerWrapper(this.plr);

				for (Object var2 : _JoeUtils.plugins) {
					try {
						((PluginInfo) var2).ref.onAttemptPlayerChangeDimension(pWrap, newDimen, ei);
					}
					catch (Throwable var8) {
						var8.printStackTrace();
					}
				}

				if (_JoeUtils.DebugMode) {
					String var21 = String.format("JKC DEBUG: --- DimensionChange (%s) to --- (%s) --- %s @ %s", new Object[] { _JoeUtils.GetDimensionName(plr.dimension), _JoeUtils.GetDimensionName(newDimen), plr.getName(), _JoeUtils.GetEntityLocationDescription(plr) });
					System.out.println(var21);
				}

				PlayerList playerList = plr.b_server.getThePlayerList();
				//playerList.notifyAboutChangedDimension(plr, newDimen);
				{
					int plrDimen = plr.dimension;
					WorldServer localWorldFrom = MinecraftServer.getServer().getWorldServerByDimension(plr.dimension);
					plr.dimension = newDimen;
					WorldServer localWorldTo = MinecraftServer.getServer().getWorldServerByDimension(plr.dimension);
					int packetDimen = plr.dimension;
					if (packetDimen > 1) {
						Environment i = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(packetDimen).getEnvironment();
						GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(packetDimen).getGenerationType();
						if (generationType == GenerationType.END) {
							packetDimen = 1;
						}
						else if (generationType == GenerationType.NETHER || i == codecrafter47.multiworld.api.Environment.NETHER) {
							packetDimen = -1;
						}
						else if (i == codecrafter47.multiworld.api.Environment.END) {
							packetDimen = 1;
						}
						else {
							packetDimen = 0;
						}
					}

					plr.plrConnection.sendPacket(new Packet_Respawn(packetDimen, plr.worldEnt.getDifficulty(), plr.worldEnt.getWorldData().getLevelType(), plr.playerInteractManager.getGameMode()));
					localWorldFrom.removeEntity(plr);
					plr.dead = false;
					//playerList.changeDimensionDetailed(plr, plrDimen, localWorldFrom, localWorldTo);
					{
						plr.setPositionRotation(loc.x + 0.5, loc.y + 0.5, loc.z + 0.5, loc.yaw, loc.pitch);
						if (plr.isNotDead()) {
							localWorldFrom.entRelatedChunkCheck(plr, false);
						}
						localWorldTo.addEntity(plr);
						localWorldTo.entRelatedChunkCheck(plr, false);

						plr.setEntityWorld(localWorldTo);
					}
					playerList.handlePlayerReceivingNewChunks(plr, localWorldFrom);
					plr.plrConnection.teleportWithYawPitch(plr.xCoord, plr.yCoord, plr.zCoord, plr.yaw, plr.pitch);
					plr.playerInteractManager.setInnerWorld(localWorldTo);
					playerList.sendPlayerPacketsAboutNewDimension(plr, localWorldTo);
					playerList.sendPlayerPacketAboutInventoryChanged(plr);

					for (Object potionEffect : plr.getActivePotionEffects()) {
						plr.plrConnection.sendPacket(new Packet_EntityEffect(plr.getEntityId(), (PotionEffect) potionEffect));
					}
				}
				plr.lastSentExp = -1;
				Field bK = Class.forName("joebkt.EntityPlayer").getDeclaredField("bK");
				bK.setAccessible(true);
				bK.set(plr, -1.0F);
				Field bL = Class.forName("joebkt.EntityPlayer").getDeclaredField("bL");
				bL.setAccessible(true);
				bL.set(plr, -1);
			} else {
				this.plr.TeleportSLoc(sloc);
			}
		}
	}

}
