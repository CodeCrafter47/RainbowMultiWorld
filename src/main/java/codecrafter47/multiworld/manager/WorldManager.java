package codecrafter47.multiworld.manager;

import PluginReference.MC_Location;
import PluginReference.MC_World;
import PluginReference.MC_WorldLevelType;
import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.WorldConfiguration;
import codecrafter47.multiworld._WorldMaster;
import joebkt._WorldRegistration;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.projectrainbow._DiwUtils;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.MinecraftServer.getCurrentTimeMillis;
import static org.projectrainbow.launch.Bootstrap.logger;

public class WorldManager {

	@SneakyThrows
	public void loadWorld(int id){
		if(isLoaded(id)){
			PluginMultiWorld.getInstance().getLogger().warn("tried to load world " + id + " but was already loaded");
			return;
		}
		_WorldRegistration entry = _WorldMaster.GetRegistrationFromDimension(id);
		WorldConfiguration configuration = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(id);
		String fileWorldName = "CustomWorld_" + entry.name;
		System.out.println(String.format("Initializing Custom World \'%s\' as dimension \'%d\' w/seed %d...", new Object[] { entry.name, Integer.valueOf(entry.dimension), Long.valueOf(entry.settings.seed) }));
		MinecraftServer server = _DiwUtils.getMinecraftServer();
		entry.internal_loadedWorldIdx = server.worldServers.length;
		ISaveHandler dataManager = server.getActiveAnvilConverter().getSaveLoader(fileWorldName, true);
		WorldType tgtLevelType = translateLevelType(entry.settings.levelType);
		WorldSettings ws = new WorldSettings(entry.settings.seed, configuration.getGameMode(), entry.settings.generateStructures, server.isHardcore(), tgtLevelType);
		ws.setGeneratorOptions(configuration.getWorldGeneratorOptions());
		WorldInfo worldData = dataManager.loadWorldInfo();
		if (worldData == null) {
			worldData = new WorldInfo(ws, fileWorldName);
			System.out.println(String.format("- New WorldData, seed %d", worldData.getSeed()));
		} else {
			// apply world settings anyway
			worldData.populateFromWorldSettings(ws);
		}

		BlockPos spawn = configuration.getSpawn();
		if (spawn != null)worldData.setSpawn(spawn);
		worldData.setTerrainType(tgtLevelType);
		worldData.setDifficulty(configuration.getDifficulty());
		worldData.setMapFeaturesEnabled(entry.settings.generateStructures);
		int loadedIdx = server.worldServers.length;

		// make worldservers array bigger
		WorldServer[] servers = new WorldServer[server.worldServers.length + 1];
		for (int i = 0; i < server.worldServers.length; i++) {
			WorldServer wserver = server.worldServers[i];
			servers[i] = wserver;
		}
		WorldServer myWorld = (WorldServer)(new CustomWorldServer(server, dataManager, worldData, id, server.theProfiler)).init();

		servers[loadedIdx] = myWorld;
		server.worldServers = servers;

		long[][]ll = new long[server.timeOfLastDimensionTick.length + 1][];
		for (int i = 0; i < server.timeOfLastDimensionTick.length; i++) {
			long[] longs = server.timeOfLastDimensionTick[i];
			ll[i] = longs;
		}
		ll[loadedIdx] = new long[100];
		server.timeOfLastDimensionTick = ll;

		myWorld.initialize(ws);

		myWorld.addEventListener(new ServerWorldEventHandler(server, myWorld));

		myWorld.setAllowedSpawnTypes(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());

        if (configuration.isKeepSpawnInMemory()) {
            loadSpawnChunks(id);
        }

		if (configuration.getSpawn() == null) {
			MC_Location spawnLocation = ((MC_World) myWorld).getSpawnLocation();
            configuration.setSpawn(new BlockPos(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()));
		}

		PluginMultiWorld.getInstance().getHookManager().callWorldLoadedHooks((MC_World) myWorld);
	}

    protected void loadSpawnChunks(int worldId) {
        int var5 = 0;
        logger.info("Preparing start region for level " + worldId);
        WorldServer var7 = _DiwUtils.getMinecraftServer().worldServerForDimension(worldId);
        BlockPos var8 = var7.getSpawnPoint();
        long var9 = getCurrentTimeMillis();

        for(int var11 = -192; var11 <= 192 && _DiwUtils.getMinecraftServer().isServerRunning(); var11 += 16) {
            for(int var12 = -192; var12 <= 192 && _DiwUtils.getMinecraftServer().isServerRunning(); var12 += 16) {
                long var13 = getCurrentTimeMillis();
                if(var13 - var9 > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", var5 * 100 / 625);
                    var9 = var13;
                }

                ++var5;
                var7.getChunkProvider().loadChunk(var8.getX() + var11 >> 4, var8.getZ() + var12 >> 4);
            }
        }
    }

    protected void outputPercentRemaining(String var1, int var2) {
        logger.info(var1 + ": " + var2 + "%");
    }

	public static WorldType translateLevelType(MC_WorldLevelType levelType) {
		switch (levelType) {
			case FLAT:
				return WorldType.FLAT;
			case LARGE_BIOMES:
				return WorldType.LARGE_BIOMES;
			case AMPLIFIED:
				return WorldType.AMPLIFIED;
			case UNSPECIFIED:
			case DEFAULT:
			default:
				return WorldType.DEFAULT;
		}
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
		return _DiwUtils.getMinecraftServer().worldServerForDimension(id) instanceof CustomWorldServer;
	}
}
