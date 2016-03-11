package codecrafter47.multiworld.manager;

import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.util.DifficultyTypeAdapter;
import codecrafter47.multiworld.util.GameTypeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Created by florian on 23.11.14.
 */
public class StorageManager {

	private PluginMultiWorld plugin;
	private Gson gson = new GsonBuilder()
			.registerTypeAdapter(EnumDifficulty.class, new DifficultyTypeAdapter())
			.registerTypeAdapter(WorldSettings.GameType.class, new GameTypeTypeAdapter())
			.setPrettyPrinting().create();

	private HashMap<Integer, WorldConfiguration> worldData = new HashMap<>();

	public StorageManager(PluginMultiWorld plugin) {
		this.plugin = plugin;
		File store = new File(plugin.getDataFolder(), "worlds.json");
		if(store.exists()){
			try {
				worldData = gson.fromJson(new FileReader(store), Container.class).worlds;
			}
			catch (Throwable e) {
				plugin.getLogger().warn("Failed to load worlds.json", e);
			}
		}
	}

	public WorldConfiguration getCustomConfig(int worldId){
        if (worldId < 2) {
            plugin.getLogger().warn("World " + worldId + " data requested.", new Exception());
        }
		if(worldData.containsKey(worldId)){
			return worldData.get(worldId);
		} else {
			WorldConfiguration configuration = new WorldConfiguration();
			worldData.put(worldId, configuration);
			saveData();
			return configuration;
		}
	}

	public void saveData() {
		File store = new File(plugin.getDataFolder(), "worlds.json");
		if(store.exists())store.delete();
		try {
			FileWriter writer = new FileWriter(store);
			gson.toJson(new Container(worldData), writer);
			writer.flush();
			writer.close();
		}
		catch (Throwable e) {
			plugin.getLogger().warn("Failed to save worlds.json", e);
		}
	}

	private static class Container{
		HashMap<Integer, WorldConfiguration> worlds;

		@java.beans.ConstructorProperties({"worlds"})
		public Container(HashMap<Integer, WorldConfiguration> worlds) {
			this.worlds = worlds;
		}
	}
}
