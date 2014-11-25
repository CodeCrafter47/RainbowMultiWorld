package codecrafter47.multiworld.manager;

import WrapperObjects.PluginHelper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.WorldConfiguration;
import joebkt.*;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WorldManager {

	Field m_worldStorageInterface;
	Method generateTerrain;
	Field playerList;

	@SneakyThrows
	public WorldManager() {
		m_worldStorageInterface = MinecraftServer.class.getDeclaredField("m_worldStorageInterface");
		m_worldStorageInterface.setAccessible(true);
		generateTerrain = MinecraftServer.class.getDeclaredMethod("generateTerrain", int.class);
		generateTerrain.setAccessible(true);
		playerList = MinecraftServer.class.getDeclaredField("playerList");
		playerList.setAccessible(true);
	}

	@SneakyThrows
	public void loadWorld(int id){
		if(isLoaded(id)){
			PluginMultiWorld.getInstance().getLogger().warn("tried to load world " + id + " but was already loaded");
			return;
		}
		_WorldRegistration entry = _WorldMaster.GetRegistrationFromDimension(id);
		int dimenForWorld = entry.dimension;
		WorldConfiguration configuration = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(dimenForWorld);
		String fileWorldName = "CustomWorld_" + entry.name;
		System.out.println(String.format("Initializing Custom World \'%s\' as dimension \'%d\' w/seed %d...", new Object[] { entry.name, Integer.valueOf(entry.dimension), Long.valueOf(entry.settings.seed) }));
		MinecraftServer server = MinecraftServer.getServer();
		entry.internal_loadedWorldIdx = server.worldServers.length;
		IDataManager dataManager = ((WorldStorageInterface)m_worldStorageInterface.get(server)).createNewWorldStorage(fileWorldName, true);
		LevelType tgtLevelType = PluginHelper.TranslateLevelType(entry.settings.levelType);
		WorldSettings ws = new WorldSettings(entry.settings.seed, GameMode.SURVIVAL, entry.settings.generateStructures, server.getServerIsHardcore(), tgtLevelType);
		ws.setInnerName(configuration.getWorldGeneratorOptions());
		WorldData worldData = dataManager.getDefaultWorldDataMaybe();
		if (worldData == null) {
			worldData = new WorldData(ws, fileWorldName);
			System.out.println(String.format("- New WorldData, seed %d", worldData.getSeed()));
		}

		IntegerCoordinates customSpawn = new IntegerCoordinates(0, 100, 0);
		if (tgtLevelType == LevelType.flat) {
			customSpawn = new IntegerCoordinates(0, 6, 0);
		}

		worldData.setSpawnCoordinates(customSpawn);
		worldData.setLevelType(tgtLevelType);
		worldData.setDifficulty(Difficulty.PEACEFUL);
		worldData.setGenStructures(entry.settings.generateStructures);
		worldData.dimensionIdx = dimenForWorld = server.worldServers.length;

		// make worldservers array bigger
		WorldServer[] servers = new WorldServer[server.worldServers.length + 1];
		for (int i = 0; i < server.worldServers.length; i++) {
			WorldServer wserver = server.worldServers[i];
			servers[i] = wserver;
		}
		WorldServer myWorld;//  = (WorldServer) (new AlternateDimensionWorld(server, dataManager, dimenForWorld, server.worldServers[0], server.methodProfiler)).prepareWorldAndReturnObject();
		myWorld = (WorldServer)(new WorldServer(server, dataManager, worldData, dimenForWorld, server.methodProfiler)).prepareWorldAndReturnObject();
		myWorld.initializeLevel(ws);
		servers[dimenForWorld] = myWorld;
		server.worldServers = servers;

		long[][]ll = new long[server.tick100PerWorld.length + 1][];
		for (int i = 0; i < server.tick100PerWorld.length; i++) {
			long[] longs = server.tick100PerWorld[i];
			ll[i] = longs;
		}
		ll[dimenForWorld] = new long[100];
		server.tick100PerWorld = ll;

		myWorld.initializeLevel(ws);

		myWorld.addEntityTrackerToNotifyList(new ServerWorldEntityTracker(server, myWorld));

		myWorld.dimensionSetAtCreate = dimenForWorld;

		((PlayerList)playerList.get(server)).setWorldServerList(server.worldServers);

		myWorld.setTwoBools(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());

		generateTerrain.invoke(server, dimenForWorld);
	}

	public List<Integer> getWorlds(){
		List<Integer> list = new ArrayList<>();
		//list.add(-1);
		//list.add(0);
		//list.add(1);
		for (_WorldRegistration worldReg : _WorldMaster.worldRegs) {
			list.add(worldReg.dimension);
		}
		return list;
	}

	public String getName(int id){
		return _WorldMaster.GetWorldNameFromDimension(id);
	}

	public boolean isLoaded(int id){
		if(id < 2)return true;
		for (_WorldRegistration worldReg : _WorldMaster.worldRegs) {
			if(worldReg.dimension == id){
				return worldReg.internal_loadedWorldIdx > 0;
			}
		}
		return false;
	}
}
