package codecrafter47.multiworld.manager;

import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.WorldConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;

import java.io.*;
import java.util.HashMap;

/**
 * Created by florian on 23.11.14.
 */
public class StorageManager {

	private PluginMultiWorld plugin;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

	public WorldConfiguration getCustomConfig(int stupidId){
		if(worldData.containsKey(stupidId)){
			return worldData.get(stupidId);
		} else {
			WorldConfiguration configuration = new WorldConfiguration();
			worldData.put(stupidId, configuration);
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

	@AllArgsConstructor
	private static class Container{
		HashMap<Integer, WorldConfiguration> worlds;
	}
}
