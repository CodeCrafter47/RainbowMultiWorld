package codecrafter47.multiworld.commands;

import PluginReference.*;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.ChatPlayer;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.manager.WorldManager;
import codecrafter47.multiworld.util.ChatUtil;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by florian on 23.11.14.
 */
public class MainCommand implements MC_Command {
	PluginMultiWorld plugin;

	public MainCommand(PluginMultiWorld plugin) {
		this.plugin = plugin;
	}

	@Override public String getCommandName() {
		return "MultiWorld";
	}

	@Override public List<String> getAliases() {
		return Arrays.asList("mw", "worlds", "multiworld");
	}

	@Override public String getHelpLine(MC_Player player) {
		return "/MultiWorld";
	}

	@Override public void handleCommand(MC_Player player, String[] strings) {
		if (strings.length == 0) {
			showHelp((ChatPlayer) player);
		} else if (strings[0].equals("tp") && strings.length == 2){
			player.teleport(plugin.getServer().getWorld(Integer.valueOf(strings[1])).getSpawnLocation());
		}
		else if (strings[0].equals("create") && strings.length > 1) {
			String name = strings[1];
			MC_WorldSettings mc_worldSettings = new MC_WorldSettings();
			mc_worldSettings.generateStructures = true;
			mc_worldSettings.seed = System.currentTimeMillis();
			int id = plugin.getServer().registerWorld(name, mc_worldSettings);
			plugin.getStorageManager().getCustomConfig(id).setGenerationType(GenerationType.OVERWORLD);
			plugin.getStorageManager().saveData();
			showWorldDetails((ChatPlayer) player, id);
		}
		else if (strings[0].equals("load") && strings.length == 2) {
			int id = Integer.valueOf(strings[1]);
			plugin.getWorldManager().loadWorld(id);
			showWorldDetails((ChatPlayer) player, id);
		}
		else if (strings[0].equals("list")) {
			showWorldList((ChatPlayer) player);

		}
		else if (strings[0].equals("modify") && strings.length == 2) {
			int id = Integer.valueOf(strings[1]);
			showWorldDetails((ChatPlayer) player, id);
		}
		else if (strings[0].equals("modify") && strings.length == 3) {
			int id = Integer.valueOf(strings[1]);
			toggleFlag(player, id, strings[2]);
			showWorldDetails((ChatPlayer) player, id);
		}
		else if (strings[0].equals("modify") && strings.length == 4) {
			int id = Integer.valueOf(strings[1]);
			setFlag(id, strings[2], strings[3]);
			showWorldDetails((ChatPlayer) player, id);
		}
		else {
			showHelp((ChatPlayer) player);
		}
	}

	private void setFlag(int id, String flag, String value) {
		WorldConfiguration configuration = plugin.getStorageManager().getCustomConfig(id);
		_WorldRegistration worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
		switch (flag) {
			case "generationType":
				configuration.setGenerationType(GenerationType.valueOf(value));
				plugin.getStorageManager().saveData();
				break;
			case "levelType":
				worldRegistration.settings.levelType = MC_WorldLevelType.valueOf(value);
				_WorldMaster.SaveData();
				break;
			case "biomeType":
				worldRegistration.settings.biomeType = MC_WorldBiomeType.valueOf(value);
				_WorldMaster.SaveData();
				break;
			case "generatorOptions":
				configuration.setWorldGeneratorOptions(value);
				plugin.getStorageManager().saveData();
				break;
			case "gamemode":
				configuration.setGameMode(GameMode.valueOf(value));
				plugin.getStorageManager().saveData();
				MinecraftServer.getServer().getWorldServerByDimension(id).getWorldData().setGameMode(configuration.getGameMode());
				break;
			case "difficulty":
				configuration.setDifficulty(Difficulty.valueOf(value));
				plugin.getStorageManager().saveData();
				MinecraftServer.getServer().getWorldServerByDimension(id).getWorldData().setDifficulty(configuration.getDifficulty());
				break;
			case "environment":
				configuration.setEnvironment(Environment.valueOf(value));
				plugin.getStorageManager().saveData();
				break;
			case "seed":
				worldRegistration.settings.seed = Long.valueOf(value);
				_WorldMaster.SaveData();
				break;
			default:
				plugin.getLogger().warn("player tried to set invalid flag: " + flag + "=" + value);
		}
	}

	private void toggleFlag(MC_Player player, int id, String string) {
		WorldConfiguration configuration = plugin.getStorageManager().getCustomConfig(id);
		_WorldRegistration worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
		switch (string) {
			case "allowAnimals":
				configuration.setSpawnAnimals(!configuration.isSpawnAnimals());
				plugin.getStorageManager().saveData();
				MinecraftServer.getServer().getWorldServerByDimension(id).setTwoBools(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
				break;
			case "allowMonsters":
				configuration.setSpawnMonsters(!configuration.isSpawnMonsters());
				plugin.getStorageManager().saveData();
				MinecraftServer.getServer().getWorldServerByDimension(id).setTwoBools(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
				break;
			case "generateStructures":
				worldRegistration.settings.generateStructures = !worldRegistration.settings.generateStructures;
				_WorldMaster.SaveData();
				break;
			case "keepSpawnInMemory":
				configuration.setKeepSpawnInMemory(!configuration.isKeepSpawnInMemory());
				plugin.getStorageManager().saveData();
				break;
			case "loadOnStartup":
				configuration.setLoadOnStartup(!configuration.isLoadOnStartup());
				plugin.getStorageManager().saveData();
				break;
			case "spawn":
				configuration.setSpawn(new IntegerCoordinates(player.getLocation().getBlockX(),
						player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
				plugin.getStorageManager().saveData();
				MinecraftServer.getServer().getWorldServerByDimension(id).setWorldSpawn(configuration.getSpawn());
				break;
			default:
				plugin.getLogger().warn("player tried to toggle invalid flag: " + string);
		}
	}

	private void showHelp(ChatPlayer player) {
		// show help
		player.sendMessage(ChatUtil.parseString(
				"&6What do you want to do?\n" +
						"&b[create a world][/MultiWorld create ]{&6click to add a world.\n"
						+ "You just need to enter the name\n"
						+ "everything else will be configured later} &e| &b[manage worlds](/MultiWorld list)"));
	}

	@Override public boolean hasPermissionToUse(MC_Player player) {
		return player.hasPermission("multiworld.admin");
	}

	@Override public List<String> getTabCompletionList(MC_Player player, String[] strings) {
		return new ArrayList<>();
	}

	public void showWorldList(ChatPlayer player) {
		player.sendMessage(ChatUtil.parseString(
				"&6Worlds:                       *[(add world)][/MultiWorld create ]{&6click to add a world.\nYou just need to enter the name\neverything else will be configured later}"
		));
		for (int id : plugin.getWorldManager().getWorlds()) {
			player.sendMessage(ChatUtil.parseString(
					"    [" + (plugin.getWorldManager().isLoaded(id) ? "&a" : "&7") + plugin.getWorldManager().getName(id) + "](/MultiWorld modify " + id + "){&6click here to change world specific settings}" + (plugin.getWorldManager().isLoaded(id) ? "   &b *[(goto)](/MultiWorld tp " + id + "){teleport there}" : "")
			));
		}
	}

	public void showWorldDetails(ChatPlayer player, int id) {
		WorldManager worldManager = plugin.getWorldManager();
		// HEADER
		if (worldManager.isLoaded(id)) {
			player.sendMessage(ChatUtil.parseString("\n&6 > World: &a\"" + worldManager.getName(id) + "\" - Loaded" + "   &b *[(goto)](/MultiWorld tp " + id + "){teleport there}"));
		}
		else {
			player.sendMessage(ChatUtil.parseString("\n&6 > World: &7\"" + worldManager.getName(id) + "\" &6-&b *[(load)](/MultiWorld load " + id + ")"));
		}
		WorldConfiguration configuration = plugin.getStorageManager().getCustomConfig(id);
		// WORLD TYPE
		String options = "";
		for (GenerationType type : GenerationType.values()) {
			if (type == configuration.getGenerationType()) {
				options += "&a&l" + type.name() + " ";
			}
			else {
				options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " generationType " + type.name() + ") ";
			}
		}
		player.sendMessage(ChatUtil.parseString("&6World Type: " + options));
		// LEVEL TYPE
		_WorldRegistration worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
		if (configuration.getGenerationType() != GenerationType.NETHER && configuration.getGenerationType() != GenerationType.END) {
			options = "";
			for (MC_WorldLevelType type : MC_WorldLevelType.values()) {
				if (type == MC_WorldLevelType.UNSPECIFIED) {
					continue;
				}
				if (type == worldRegistration.settings.levelType) {
					options += "&a&l" + type.name() + " ";
				}
				else {
					options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " levelType " + type.name() + ") ";
				}
			}
			player.sendMessage(ChatUtil.parseString("&6LevelType: " + options));
		}
		// BIOME TYPE
		if (configuration.getGenerationType() == GenerationType.SINGLE_BIOME) {
			options = "";
			for (MC_WorldBiomeType type : MC_WorldBiomeType.values()) {
				if (type == MC_WorldBiomeType.UNSPECIFIED) {
					continue;
				}
				if (type == worldRegistration.settings.biomeType) {
					options += "&a&l" + type.name() + " ";
				}
				else {
					options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " biomeType " + type.name() + ") ";
				}
			}
			player.sendMessage(ChatUtil.parseString("&6BiomeType: " + options));
		}
		// GENERATOR OPTIONS
		if (configuration.getGenerationType() != GenerationType.NETHER && configuration.getGenerationType() != GenerationType.END && worldRegistration.settings.levelType == MC_WorldLevelType.FLAT) {
			player.sendMessage(ChatUtil.parseString("&6GeneratorOptions: \"" + configuration.getWorldGeneratorOptions() + "\" *&e[(edit)][/MultiWorld modify " + id + " generatorOptions " + configuration.getWorldGeneratorOptions() + " ]"));
		}
		// SEED
		player.sendMessage(ChatUtil.parseString("&6Seed: [&f" + worldRegistration.settings.seed + "][/MultiWorld modify " + id + " seed " + worldRegistration.settings.seed + "]{&6change seed\nyou need to enter a number}"));
		// ENVIRONMENT
		if (configuration.getGenerationType() == GenerationType.SINGLE_BIOME || configuration.getGenerationType() == GenerationType.OVERWORLD) {
			options = "";
			for (Environment type : Environment.values()) {
				if (type == configuration.getEnvironment()) {
					options += "&a&l" + type.name() + " ";
				}
				else {
					options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " environment " + type.name() + ") ";
				}
			}
			player.sendMessage(ChatUtil.parseString("[&6Environment:]{&6environment describes how the sky looks} " + options));
		}
		// GAMEMODE
		options = "";
		for (GameMode type : GameMode.values()) {
			if (type == GameMode.NOT_SET) {
				continue;
			}
			if (type == configuration.getGameMode()) {
				options += "&a&l" + type.name() + " ";
			}
			else {
				options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " gamemode " + type.name() + ") ";
			}
		}
		player.sendMessage(ChatUtil.parseString("&6Gamemode: " + options));
		// DIFFICULTY
		options = "";
		for (Difficulty type : Difficulty.values()) {
			if (type == configuration.getDifficulty()) {
				options += "&a&l" + type.name() + " ";
			}
			else {
				options += "&r&7[" + type.name() + "](/MultiWorld modify " + id + " difficulty " + type.name() + ") ";
			}
		}
		player.sendMessage(ChatUtil.parseString("&6Difficulty: " + options));
		// SPAWN
		player.sendMessage(ChatUtil.parseString("&6Spawn: " + configuration.getSpawn().getX()
				+ "," + configuration.getSpawn().getY() + "," + configuration.getSpawn().getZ()
				+ (((((MC_Player) player).getWorld().getDimension() == id)) ?
				("    *[(set to current position)](/MultiWorld modify " + id + " spawn)") : "")));
		// FLAGS
		player.sendMessage(ChatUtil.parseString("&6Flags: " +
				(configuration.isSpawnAnimals() ? "&a" : "&7") + "[allowAnimals](/MultiWorld modify " + id + " allowAnimals) " +
				(configuration.isSpawnMonsters() ? "&a" : "&7") + "[allowMonsters](/MultiWorld modify " + id + " allowMonsters) " +
				(worldRegistration.settings.generateStructures ? "&a" : "&7") + "[generateStructures](/MultiWorld modify " + id + " generateStructures) " +
				//				(configuration.isKeepSpawnInMemory() ? "&a" : "&7") + "[keepSpawnInMemory](/MultiWorld modify " + id + " keepSpawnInMemory) " +
				(configuration.isLoadOnStartup() ? "&a" : "&7") + "[loadOnStartup](/MultiWorld modify " + id + " loadOnStartup) " +
				""));
	}
}
