package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 23.11.14.
 */
@Mixin(net.minecraft.server.MinecraftServer.class)
public abstract class CustomWorldLoading {

	@Shadow
	public WorldServer[] worldServers;

	@Shadow
	public long[][] tick100PerWorld;

	@Shadow
	private WorldStorageInterface m_worldStorageInterface;

	@Shadow
	private boolean flagBonusChest;

	@Shadow
	public MethodProfiler methodProfiler;

	@Shadow
	private PlayerList playerList;

	@Shadow
	protected abstract void convertMap(String var1);

	@Shadow
	protected abstract void setWhatServerIsCurrentlyDoing(String var1);

	@Shadow
	public abstract boolean getServerIsHardcore();

	@Shadow
	protected abstract void setResourcePackInResourcesZip(String var1, IDataManager var2);

	@Shadow
	public abstract String getOverworldName();

	@Shadow
	public abstract boolean getFlagGenerateStructures();

	@Shadow
	public abstract GameMode getServerGamemode();

	@Shadow
	public abstract Difficulty getDifficultySetting();

	@Shadow
	protected abstract void generateTerrain(int argWorldServerIdx);

	@Shadow
	public abstract void setDifficultyForAllWorlds(Difficulty var1);

	@Overwrite
	protected void prepareWorlds(String var1, String strSetToWorld, long rndLong, LevelType var5, String strSentToBlank) {

		for (_WorldRegistration worldReg : _WorldMaster.worldRegs) {
			worldReg.internal_loadedWorldIdx = 0;
		}
		;

		this.convertMap(var1);
		this.setWhatServerIsCurrentlyDoing("menu.loadingLevel");

		this.worldServers = new WorldServer[3];
		boolean localIsServerHardcore = this.getServerIsHardcore();
		this.tick100PerWorld = new long[3][1000];

		IDataManager dataManager = this.m_worldStorageInterface.createNewWorldStorage(var1, true);
		this.setResourcePackInResourcesZip(this.getOverworldName(), dataManager);
		WorldData worldData = dataManager.getDefaultWorldDataMaybe();
		WorldSettings worldSettingsBase;
		if (worldData == null) {
			worldSettingsBase = new WorldSettings(rndLong, this.getServerGamemode(), this.getFlagGenerateStructures(), localIsServerHardcore, var5);
			worldSettingsBase.setInnerName(strSentToBlank);
			if (this.flagBonusChest) {
				worldSettingsBase.setSpawnWithItemsFlag();
			}

			worldData = new WorldData(worldSettingsBase, strSetToWorld);
		}
		else {
			worldData.setLevelName(strSetToWorld);
			worldSettingsBase = new WorldSettings(worldData);
		}

		int i;
		for (i = 0; i < this.worldServers.length; ++i) {
			WorldSettings ws = worldSettingsBase;
			int dimenForWorld = 0;
			if (i == 1) {
				dimenForWorld = -1;
			}

			if (i == 2) {
				dimenForWorld = 1;
			}

			if (i == 0) {
				this.worldServers[i] = (WorldServer) (new WorldServer((MinecraftServer) (Object) this, dataManager, worldData, dimenForWorld, this.methodProfiler)).prepareWorldAndReturnObject();
				this.worldServers[i].initializeLevel(worldSettingsBase);
			} else {
				this.worldServers[i] = (WorldServer) (new WorldServer((MinecraftServer) (Object) this, dataManager, worldData, dimenForWorld, this.methodProfiler)).prepareWorldAndReturnObject();
				this.worldServers[i].initializeLevel(ws);
			}

			this.worldServers[i].addEntityTrackerToNotifyList(new ServerWorldEntityTracker((MinecraftServer) (Object) this, this.worldServers[i]));
			this.worldServers[i].getWorldData().setGameMode(this.getServerGamemode());

			this.worldServers[i].dimensionSetAtCreate = dimenForWorld;
		}

		this.playerList.setWorldServerList(this.worldServers);
		this.setDifficultyForAllWorlds(this.getDifficultySetting());
		this.generateTerrain(0);

		PluginMultiWorld.getInstance().onItsTimeToLoadCustomWorlds();
	}
}
