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
import java.util.Iterator;
import java.util.List;

/**
 * Created by florian on 23.11.14.
 */
public class MainCommand implements MC_Command {
	PluginMultiWorld plugin;

	boolean requiresRestart = false;

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
		if(player == null){
			return;
		}
		if (strings.length == 0) {
			showHelp((ChatPlayer) player);
		} else if (strings[0].equals("tp") && strings.length == 2){
			Integer id = Integer.valueOf(strings[1]);
			if(plugin.getWorldManager().isLoaded(id)) {
				player.teleport(plugin.getServer().getWorld(id).getSpawnLocation());
			} else {
				((ChatPlayer)player).sendMessage(ChatUtil.parseString("&cWorld " + plugin.getWorldManager().getName(id) + " is not loaded! *&b[(load now)](/MultiWorld load " + id + ")"));
			}
		}
		else if (strings[0].equals("create") && strings.length > 1) {
			String name = strings[1];
			if(_WorldMaster.mapDimensionToWorldName.values().contains(name)){
				((ChatPlayer)player).sendMessage(ChatUtil.parseString("&cWorld " + name + " already exists"));
				return;
			}
			MC_WorldSettings mc_worldSettings = new MC_WorldSettings();
			mc_worldSettings.generateStructures = true;
			mc_worldSettings.seed = System.currentTimeMillis();
			int id = plugin.getServer().registerWorld(name, mc_worldSettings);
			plugin.getStorageManager().getCustomConfig(id).setGenerationType(GenerationType.OVERWORLD);
			plugin.getStorageManager().saveData();
			showWorldDetails((ChatPlayer) player, id);
		}
		else if (strings[0].equals("remove") && strings.length == 2){
			if(PluginMultiWorld.getInstance().getServer().unregisterWorld(_WorldMaster.GetWorldNameFromDimension(Integer.valueOf(strings[1])))){
				((ChatPlayer)player).sendMessage(ChatUtil.parseString(
						"&aSuccessfully unregistered " + _WorldMaster.GetWorldNameFromDimension(Integer.valueOf(strings[1])) + "."));
				requiresRestart = true;
			} else {
				((ChatPlayer)player).sendMessage(ChatUtil.parseString("&cThere has been an error deleting the world."));
			}
			showWorldList((ChatPlayer) player);
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
		else if (strings[0].equals("gamerule") && strings.length == 4){
			int id = Integer.valueOf(strings[1]);
			String gamerule = strings[2];
			String value = strings[3];
			MinecraftServer.getServer().getWorldServerByDimension(id).getGameRules().a(gamerule, value);
			showWorldDetails((ChatPlayer) player, id);
		}
		else if(strings[0].equals("inv") && strings.length == 1){
			showInvDetails((ChatPlayer) player);
		}
		else if(strings[0].equals("inv") && strings.length == 3 && strings[1].equals("addgroup")){
			if(!plugin.getMultiInventoryManager().getGroups().contains(strings[2]))plugin.getMultiInventoryManager().addGroup(strings[2]);
			showInvDetails((ChatPlayer) player);
		}
		else if(strings[0].equals("inv") && strings.length == 4 && strings[1].equals("setgroup")){
			plugin.getMultiInventoryManager().setGroupForWorld(getWorldByName(strings[2]), strings[3]);
			showInvDetails((ChatPlayer) player);
		}
		else {
			showHelp((ChatPlayer) player);
		}
		// RESTART WARNING
		if(requiresRestart){
			((ChatPlayer)player).sendMessage(ChatUtil.parseString("&c *\\* The server needs to be restarted in order to apply all changes."));
		}
	}

	private MC_World getWorldByName(String name){
		for(MC_World world: plugin.getServer().getWorlds()){
			if(world.getName().equals(name))return world;
		}
		throw new RuntimeException("World " + name + " does not exist!");
	}

	private void showInvDetails(ChatPlayer player) {
		player.sendMessage(ChatUtil.parseString("&6#### Inventories ####"));
		String groups = "";
		for (Iterator<String> iterator = plugin.getMultiInventoryManager().getGroups().iterator(); iterator.hasNext(); ) {
			String group = iterator.next();
			groups += group;
			if(iterator.hasNext())groups += ", ";
		}
		player.sendMessage(ChatUtil.parseString("&6Groups: &b" + groups + "   *[(+)][/MultiWorld inv addgroup name]{&6add a group}"));
		for(MC_World world: plugin.getServer().getWorlds()){
			player.sendMessage(ChatUtil.parseString("&b[" + world.getName() + ": &6" +
					plugin.getMultiInventoryManager().getWhereForWorld(world) +
					"][/MultiWorld inv setgroup " + world.getName() + " " +
					plugin.getMultiInventoryManager().getWhereForWorld(world) + "]"));
		}
	}

	private void setFlag(int id, String flag, String value) {
		WorldConfiguration configuration = plugin.getStorageManager().getCustomConfig(id);
		_WorldRegistration worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
		switch (flag) {
			case "generationType":
				configuration.setGenerationType(GenerationType.valueOf(value));
				plugin.getStorageManager().saveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
				break;
			case "levelType":
				worldRegistration.settings.levelType = MC_WorldLevelType.valueOf(value);
				_WorldMaster.SaveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
				break;
			case "biomeType":
				worldRegistration.settings.biomeType = MC_WorldBiomeType.valueOf(value);
				_WorldMaster.SaveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
				break;
			case "generatorOptions":
				configuration.setWorldGeneratorOptions(value);
				plugin.getStorageManager().saveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
				break;
			case "gamemode":
				configuration.setGameMode(GameMode.valueOf(value));
				plugin.getStorageManager().saveData();
				if(plugin.getWorldManager().isLoaded(id))
					MinecraftServer.getServer().getWorldServerByDimension(id).getWorldData().setGameMode(configuration.getGameMode());
				break;
			case "difficulty":
				configuration.setDifficulty(Difficulty.valueOf(value));
				plugin.getStorageManager().saveData();
				if(plugin.getWorldManager().isLoaded(id))
					MinecraftServer.getServer().getWorldServerByDimension(id).getWorldData().setDifficulty(configuration.getDifficulty());
				break;
			case "environment":
				configuration.setEnvironment(Environment.valueOf(value));
				plugin.getStorageManager().saveData();
				break;
			case "seed":
				worldRegistration.settings.seed = Long.valueOf(value);
				_WorldMaster.SaveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
				break;
			case "respawnWorld":
				configuration.setRespawnWorld(Integer.valueOf(value));
				plugin.getStorageManager().saveData();
			case "netherPortalTarget":
				configuration.setNetherPortalTarget(Integer.valueOf(value));
				plugin.getStorageManager().getCustomConfig(configuration.getNetherPortalTarget()).setNetherPortalTarget(id);
				plugin.getStorageManager().saveData();
				break;
			case "endPortalTarget":
				configuration.setEndPortalTarget(Integer.valueOf(value));
				plugin.getStorageManager().getCustomConfig(configuration.getEndPortalTarget()).setEndPortalTarget(id);
				plugin.getStorageManager().saveData();
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
				if(plugin.getWorldManager().isLoaded(id))
					MinecraftServer.getServer().getWorldServerByDimension(id).setTwoBools(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
				break;
			case "allowMonsters":
				configuration.setSpawnMonsters(!configuration.isSpawnMonsters());
				plugin.getStorageManager().saveData();
				if(plugin.getWorldManager().isLoaded(id))
					MinecraftServer.getServer().getWorldServerByDimension(id).setTwoBools(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
				break;
			case "generateStructures":
				worldRegistration.settings.generateStructures = !worldRegistration.settings.generateStructures;
				_WorldMaster.SaveData();
				if(plugin.getWorldManager().isLoaded(id))requiresRestart = true;
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
				if(plugin.getWorldManager().isLoaded(id))
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
						+ "&6You just need to enter the name\n"
						+ "&6everything else will be configured later} &e| &b[manage worlds](/MultiWorld list)"
						+ " &e| &b[manage inventories](/MultiWorld inv)"));
	}

	@Override public boolean hasPermissionToUse(MC_Player player) {
		return player.hasPermission("multiworld.admin");
	}

	@Override public List<String> getTabCompletionList(MC_Player player, String[] strings) {
		return new ArrayList<>();
	}

	public void showWorldList(ChatPlayer player) {
		player.sendMessage(ChatUtil.parseString(
				"&6Worlds:                       *[(add world)][/MultiWorld create ]{&6click to add a world.\n&6You just need to enter the name\n&6everything else will be configured later}"
		));
		for (int id : plugin.getWorldManager().getWorlds()) {
			player.sendMessage(ChatUtil.parseString(
					"    [" + (plugin.getWorldManager().isLoaded(id) ? "&a" : "&7") + plugin.getWorldManager().getName(id) +
							"](/MultiWorld modify " + id + "){&6click here to change world specific settings}" +
							(plugin.getWorldManager().isLoaded(id) ? "   &b *[(goto)](/MultiWorld tp " + id +
									"){teleport there}" : "   &b *[(load)](/MultiWorld load " + id + ")")
			));
		}
	}

	public void showWorldDetails(ChatPlayer player, int id) {
		WorldManager worldManager = plugin.getWorldManager();
		// HEADER
		if (worldManager.isLoaded(id)) {
			player.sendMessage(ChatUtil.parseString("\n&6 > World: &a\"" + worldManager.getName(id) + "\" - Loaded" + "   &b *[(goto)](/MultiWorld tp " + id + "){teleport there}" + "   &4 [(delete)](/MultiWorld remove " + id + "){&cDANGER: Removes the world}"));
		}
		else {
			player.sendMessage(ChatUtil.parseString("\n&6 > World: &7\"" + worldManager.getName(id) + "\" &6-&b *[(load)](/MultiWorld load " + id + ")" + "   &4 [(delete)](/MultiWorld remove " + id + "){&cDANGER: Removes the world}"));
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
		// GAMERULES
		if(plugin.getWorldManager().isLoaded(id)){
			GameRules gameRules = MinecraftServer.getServer().getWorldServerByDimension(id).getWorldData().getGameRules();
			options = "";
			for(String gamerule: gameRules.b()){
				if(gameRules.a(gamerule, EnumAnyBoolOrNumeric.BOOLEAN_VALUE)){
					// this is a boolean rule
					if(Boolean.valueOf(gameRules.a(gamerule))){
						options += "&a[" + gamerule + "](/MultiWorld gamerule " + id + " " + gamerule + " false) ";
					} else {
						options += "&7[" + gamerule + "](/MultiWorld gamerule " + id + " " + gamerule + " true) ";
					}
				} else {
					// this is free text rule
					options += "&a[" + gamerule + "=" + gameRules.a(gamerule) + "][/MultiWorld gamerule " + id + " " + gamerule + " " + gameRules.a(gamerule) + "] ";
				}
			}
		}
		player.sendMessage(ChatUtil.parseString("&6Gamerules: " + options));
		// RESPAWN WORLD
		options = "";
		for (MC_World world: plugin.getServer().getWorlds()) {
			if (world.getDimension() == configuration.getRespawnWorld()) {
				options += "&a&l" + world.getName() + " ";
			}
			else {
				options += "&r&7[" + world.getName() + "](/MultiWorld modify " + id + " respawnWorld " + world.getDimension() + ") ";
			}
		}
		player.sendMessage(ChatUtil.parseString("&6Respawn world: " + options));
		// NETHER PORTAL TARGET
		if(configuration.getGenerationType() == GenerationType.OVERWORLD) {
			options = "";
			if (-2 == configuration.getNetherPortalTarget()) {
				options += "&a&lNONE ";
			}
			else {
				options += "&r&7[NONE](/MultiWorld modify " + id + " netherPortalTarget -2) ";
			}
			for (MC_World world : plugin.getServer().getWorlds()) {
				if(world.getDimension() <= 2)continue;
				if(configuration.getGenerationType() == GenerationType.OVERWORLD && plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.NETHER)continue;
				if(configuration.getGenerationType() == GenerationType.NETHER && plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.OVERWORLD)continue;
				if (world.getDimension() == configuration.getNetherPortalTarget()) {
					options += "&a&l" + world.getName() + " ";
				}
				else {
					options += "&r&7[" + world.getName() + "](/MultiWorld modify " + id + " netherPortalTarget " + world.getDimension() + ") ";
				}
			}
			player.sendMessage(ChatUtil.parseString("&6Nether world: " + options));
		}
		// END PORTAL TARGET
		if(configuration.getGenerationType() == GenerationType.OVERWORLD) {
			options = "";
			if (-2 == configuration.getEndPortalTarget()) {
				options += "&a&lNONE ";
			}
			else {
				options += "&r&7[NONE](/MultiWorld modify " + id + " endPortalTarget -2) ";
			}
			for (MC_World world : plugin.getServer().getWorlds()) {
				if(world.getDimension() <= 2)continue;
				if(plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.END)continue;
				if (world.getDimension() == configuration.getEndPortalTarget()) {
					options += "&a&l" + world.getName() + " ";
				}
				else {
					options += "&r&7[" + world.getName() + "](/MultiWorld modify " + id + " endPortalTarget " + world.getDimension() + ") ";
				}
			}
			player.sendMessage(ChatUtil.parseString("&6End world: " + options));
		}
	}
}
