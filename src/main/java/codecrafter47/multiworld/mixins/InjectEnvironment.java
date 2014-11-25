package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import PluginReference.PluginInfo;
import WrapperObjects.Entities.PlayerWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import joebkt.*;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	private static Logger h;

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

	@Shadow
	public abstract NBTTagCompound a(EntityPlayer argPlayer);

	@Shadow
	public abstract MinecraftServer getServer();

	@Shadow
	public abstract int getMaxPlayers();

	@Shadow
	protected abstract void a(ScoreboardPacketManager var1, EntityPlayer var2);

	@Shadow
	public abstract void sendChatPacketToAllWrapper(TextObject var1);

	@Shadow
	public abstract void handleAddingPlayer(EntityPlayer var1);

	@Overwrite
	public void reportLoggedIn(PacketHandler var1, EntityPlayer plr) {
		GameProfile var3 = plr.getGameProfile();
		ry_containsServerFileAndGson var4 = this.server.getServerFileAndGSonUsedDuringLogin();
		GameProfile var5 = var4.getGameProfileFromUUID(var3.getId());
		String var6 = var5 == null?var3.getName():var5.getName();
		var4.a(var3);
		NBTTagCompound var7 = this.a(plr);
		plr.setEntityWorld(this.server.getWorldServerByDimension(plr.dimension));
		plr.playerInteractManager.setInnerWorld((WorldServer)plr.worldEnt);
		String var8 = "local";
		SocketAddress sockAddr = var1.getSocketAddr();
		if(sockAddr != null) {
			var8 = sockAddr.toString();
		}

		String ipAddr = var8;
		if(var8.startsWith("/")) {
			ipAddr = var8.substring(1);
		}

		int idxColon = ipAddr.indexOf(58);
		if(idxColon >= 0) {
			ipAddr = ipAddr.substring(0, idxColon);
		}

		String pName = plr.getName();
		long cnt = _JoeUtils.IncreaseEventCount("login." + pName).longValue();
		String msg = String.format("-- LOGIN -- %s -- %s, Login #%d, IP: -- %s --", new Object[]{plr.getName(), plr.GetSLoc().toString(), Long.valueOf(cnt), ipAddr});
		if(!MinecraftServer.getServer().isOnlineMode()) {
			UUID var10 = plr.getUUID();
			if(var10 != null) {
				String var9 = var10.toString();
			}

			System.out.println("* Offline Mode: " + pName + " w/UUID=" + var10);
			_UUIDMapper.AddMap(pName, plr.getUUID().toString());
		}

		h.info(msg);
		_AsyncLog.Log(msg);
		_JOT_OnlineTimeUtils.HandlePlayerLogin(pName);
		Iterator var101 = _JoeUtils.plugins.iterator();

		while(var101.hasNext()) {
			PluginInfo var91 = (PluginInfo)var101.next();

			try {
				var91.ref.onPlayerLogin(plr.getName(), plr.getUUID(), ipAddr);
			} catch (Throwable var29) {
				var29.printStackTrace();
			}
		}

		WorldServer var92 = this.server.getWorldServerByDimension(plr.dimension);
		WorldData var102 = var92.getWorldData();
		IntegerCoordinates var11 = var92.getSpawnCoords();
		this.setGameModeForPlayerInWorld(plr, (EntityPlayer)null, var92);
		PlayerConnection plrConnection = new PlayerConnection(this.server, var1, plr);
		plrConnection.m_ipAddress = ipAddr;
		plrConnection.m_sockAddr = sockAddr;
		int pktDimen = var92.worldProvider.getDimenIdx();
		if(pktDimen > 1){
			Environment i = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(pktDimen).getEnvironment();
			GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(pktDimen).getGenerationType();
			if (generationType == GenerationType.END) {
				pktDimen = 1;
			}
			else if (generationType == GenerationType.NETHER || i == codecrafter47.multiworld.api.Environment.NETHER) {
				pktDimen = -1;
			}
			else if (i == codecrafter47.multiworld.api.Environment.END) {
				pktDimen = 1;
			}
			else {
				pktDimen = 0;
			}
		}

		plrConnection.sendPacket(new Packet_JoinGame(plr.getEntityId(), plr.playerInteractManager.getGameMode(), var102.isHardcore(), pktDimen, var92.getDifficulty(), this.getMaxPlayers(), var102.getLevelType(), var92.getGameRules().getEntry("reducedDebugInfo")));
		plrConnection.sendPacket(new Packet_PluginMessage("MC|Brand", (new ByteData(Unpooled.buffer())).a(this.getServer().getServerModName())));
		plrConnection.sendPacket(new Packet_ServerDifficulty(var102.getDifficulty(), var102.getDifficultyLocked()));
		plrConnection.sendPacket(new Packet_SpawnPosition(var11));
		plrConnection.sendPacket(new Packet_PlayerAbilities(plr.abilities));
		plrConnection.sendPacket(new Packet_HeldItemChange(plr.inventory.idxHand));
		plr.getStatMapManager().addStatsToMap();
		plr.getStatMapManager().sendPacketAchievements(plr);
		this.a((ScoreboardPacketManager)var92.getScoreboard(), plr);
		this.server.aF();
		ChatMessage var13;
		if(!plr.getName().equalsIgnoreCase(var6)) {
			var13 = new ChatMessage("multiplayer.player.joined.renamed", new Object[]{plr.e_(), var6});
		} else {
			var13 = new ChatMessage("multiplayer.player.joined", new Object[]{plr.e_()});
		}

		var13.getStyle().a(OrgChatColor.YELLOW);
		this.sendChatPacketToAllWrapper(var13);
		this.handleAddingPlayer(plr);
		plrConnection.loginPorting = true;
		plrConnection.teleportWithYawPitch(plr.xCoord, plr.yCoord, plr.zCoord, plr.yaw, plr.pitch);
		plrConnection.loginPorting = false;
		this.sendPlayerPacketsAboutNewDimension(plr, var92);
		if(this.server.aa().length() > 0) {
			plr.a(this.server.aa(), this.server.ab());
		}

		Iterator var14 = plr.getActivePotionEffects().iterator();

		while(var14.hasNext()) {
			PotionEffect pWrap = (PotionEffect)var14.next();
			plrConnection.sendPacket(new Packet_EntityEffect(plr.getEntityId(), pWrap));
		}

		plr.setActiveContainerListener();
		if(var7 != null && var7.checkEntry("Riding", 10)) {
			EntityGeneric pWrap1 = EntityClassMapper.a(var7.getNBTCompoundByNameOrNew("Riding"), var92);
			if(pWrap1 != null) {
				pWrap1.n = true;
				var92.addEntity(pWrap1);
				plr.setVehicle(pWrap1);
				pWrap1.n = false;
			}
		}

		PlayerWrapper pWrap2 = new PlayerWrapper(plr);
		Iterator var25 = _JoeUtils.plugins.iterator();

		while(var25.hasNext()) {
			PluginInfo plugin = (PluginInfo)var25.next();

			try {
				plugin.ref.onPlayerJoin(pWrap2);
			} catch (Throwable var28) {
				var28.printStackTrace();
			}
		}

		if(_JoeUtils.RainbowSuddenDeathMode) {
			try {
				MinecraftServer.getServer().getCommandSender().executeCommand(MinecraftServer.getServer(), _JoeUtils.FullTranslate("/title " + pWrap2.getName() + " subtitle \"&bDon\'t get Hurt!\""));
				MinecraftServer.getServer().getCommandSender().executeCommand(MinecraftServer.getServer(), _JoeUtils.FullTranslate("/title " + pWrap2.getName() + " title \"&4Sudden Death On\""));
			} catch (Throwable var27) {
				;
			}
		}

	}

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
			GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(sentDimension).getGenerationType();
			if (generationType == GenerationType.END) {
				sentDimension = 1;
			}
			else if (generationType == GenerationType.NETHER || i == codecrafter47.multiworld.api.Environment.NETHER) {
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
		if(packetDimen > 1){
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
