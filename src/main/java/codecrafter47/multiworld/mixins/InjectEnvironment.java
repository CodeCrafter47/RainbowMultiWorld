package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import PluginReference.PluginInfo;
import WrapperObjects.Entities.PlayerWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;

/**
 * Created by florian on 25.11.14.
 */
@Mixin(joebkt.PlayerList.class)
public abstract class InjectEnvironment {

	@Shadow
	private MinecraftServer server;

	@Shadow
	public List players;

	@Shadow
	public Map mapUUIDToEntityPlayer;

	@Shadow
	abstract void setGameModeForPlayerInWorld(EntityPlayer var1, EntityPlayer var2, World var3);

	@Shadow
	public abstract void sendPlayerPacketsAboutNewDimension(EntityPlayer var1, WorldServer argWorldServer);

	@Shadow
	public abstract void changeDimensionDetailed(EntityGeneric argEnt, int argDimension, WorldServer argWorldFrom, WorldServer argWorldTo);

	@Shadow
	public abstract void handlePlayerReceivingNewChunks(EntityPlayer argPlr, WorldServer argWorld);

	@Shadow
	public abstract void sendPlayerPacketAboutInventoryChanged(EntityPlayer var1);

	@Overwrite
	public EntityPlayer respawnPlayer(EntityPlayer plr, int dimen, boolean var3) {
		if(_JoeUtils.DebugMode) {
			System.out.println("--- JKC DEBUG --- Respawning: " + plr.getName() + " to dimen=" + dimen + ", flag=" + var3);
		}

		plr.getWorldServer().getEntityTracker().removePlayer(plr);
		plr.getWorldServer().getEntityTracker().removeEntityRelated(plr);
		plr.getWorldServer().getPlayerChunkMap().removePlayer(plr);
		this.players.remove(plr);
		this.server.getWorldServerByDimension(plr.dimension).removeEntity(plr);
		IntegerCoordinates orgCoords = plr.getRespawnPosition();
		boolean fSpawnForced = plr.isSpawnForced();
		plr.dimension = dimen;
		Object var6;
		if(this.server.getDemoMode()) {
			var6 = new qk_DemoModePlayerInteractManager(this.server.getWorldServerByDimension(plr.dimension));
		} else {
			var6 = new PlayerInteractManager(this.server.getWorldServerByDimension(plr.dimension));
		}

		EntityPlayer newPlr = new EntityPlayer(this.server, this.server.getWorldServerByDimension(plr.dimension), plr.getGameProfile(), (PlayerInteractManager)var6);
		newPlr.plrConnection = plr.plrConnection;
		newPlr.a(plr, var3);
		newPlr.EntGen_setEntityID(plr.getEntityId());
		newPlr.o(plr);
		WorldServer worldServer = this.server.getWorldServerByDimension(plr.dimension);
		this.setGameModeForPlayerInWorld(newPlr, plr, worldServer);
		IntegerCoordinates var9;
		if(orgCoords != null) {
			var9 = EntityHuman.getBed(this.server.getWorldServerByDimension(plr.dimension), orgCoords, fSpawnForced);
			if(var9 != null) {
				newPlr.setPositionRotation((double)((float)var9.getX() + 0.5F), (double)((float)var9.getY() + 0.1F), (double)((float)var9.getZ() + 0.5F), 0.0F, 0.0F);
				newPlr.setRespawnPosition(orgCoords, fSpawnForced);
			} else {
				newPlr.plrConnection.sendPacket(new Packet_ChangeGameState(0, 0.0F));
			}
		}

		worldServer.cachedChunks.generateNewChunk((int)newPlr.xCoord >> 4, (int)newPlr.zCoord >> 4);

		while(!worldServer.getNearbyEntities(newPlr, newPlr.funcAppliesIfCanBePushed()).isEmpty() && newPlr.yCoord < 256.0D) {
			newPlr.setCoordsAndBox(newPlr.xCoord, newPlr.yCoord + 1.0D, newPlr.zCoord);
		}

		int sentDimension = newPlr.dimension;
		if(sentDimension > 1){
			Environment i = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(sentDimension).getEnvironment();
			if (i == codecrafter47.multiworld.api.Environment.NETHER) {
				sentDimension = -1;

			}
			else if (i == codecrafter47.multiworld.api.Environment.END) {
				sentDimension = 1;

			}
			else {
				sentDimension = 0;

			}
		}
		newPlr.plrConnection.sendPacket(new Packet_Respawn(sentDimension, newPlr.worldEnt.getDifficulty(), newPlr.worldEnt.getWorldData().getLevelType(), newPlr.playerInteractManager.getGameMode()));
		var9 = worldServer.getSpawnCoords();
		newPlr.plrConnection.teleportWithYawPitch(newPlr.xCoord, newPlr.yCoord, newPlr.zCoord, newPlr.yaw, newPlr.pitch);
		newPlr.plrConnection.sendPacket(new Packet_SpawnPosition(var9));
		newPlr.plrConnection.sendPacket(new Packet_SetExperience(newPlr.m_exp, newPlr.xpTotal, newPlr.xpLevel));
		this.sendPlayerPacketsAboutNewDimension(newPlr, worldServer);
		worldServer.getPlayerChunkMap().addPlayer(newPlr);
		worldServer.addEntity(newPlr);
		this.players.add(newPlr);
		this.mapUUIDToEntityPlayer.put(newPlr.getUUID(), newPlr);
		newPlr.setActiveContainerListener();
		newPlr.setHealth(newPlr.getHealth());

		for (Object plugin : _JoeUtils.plugins) {
			try {
				((PluginInfo)plugin).ref.onPlayerRespawn(new PlayerWrapper(newPlr));
			}
			catch (Throwable var13) {
				var13.printStackTrace();
			}
		}

		return newPlr;
	}

	@Overwrite
	public void notifyAboutChangedDimension(EntityPlayer argPlr, int argDimension) {
		int plrDimen = argPlr.dimension;
		WorldServer localWorldFrom = this.server.getWorldServerByDimension(argPlr.dimension);
		argPlr.dimension = argDimension;
		WorldServer localWorldTo = this.server.getWorldServerByDimension(argPlr.dimension);
		int packetDimen = argPlr.dimension;
		if(packetDimen > 1) {
			Environment i = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(packetDimen).getEnvironment();
			if (i == codecrafter47.multiworld.api.Environment.NETHER) {
				packetDimen = -1;

			}
			else if (i == codecrafter47.multiworld.api.Environment.END) {
				packetDimen = 1;

			}
			else {
				packetDimen = 0;

			}
		}

		argPlr.plrConnection.sendPacket(new Packet_Respawn(packetDimen, argPlr.worldEnt.getDifficulty(), argPlr.worldEnt.getWorldData().getLevelType(), argPlr.playerInteractManager.getGameMode()));
		localWorldFrom.removeEntity(argPlr);
		argPlr.dead = false;
		this.changeDimensionDetailed(argPlr, plrDimen, localWorldFrom, localWorldTo);
		this.handlePlayerReceivingNewChunks(argPlr, localWorldFrom);
		argPlr.plrConnection.teleportWithYawPitch(argPlr.xCoord, argPlr.yCoord, argPlr.zCoord, argPlr.yaw, argPlr.pitch);
		argPlr.playerInteractManager.setInnerWorld(localWorldTo);
		this.sendPlayerPacketsAboutNewDimension(argPlr, localWorldTo);
		this.sendPlayerPacketAboutInventoryChanged(argPlr);

		for (Object potionEffect : argPlr.getActivePotionEffects()) {
			argPlr.plrConnection.sendPacket(new Packet_EntityEffect(argPlr.getEntityId(), (PotionEffect)potionEffect));
		}

	}
}
