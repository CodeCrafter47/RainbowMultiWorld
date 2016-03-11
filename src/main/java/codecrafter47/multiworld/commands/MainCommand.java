package codecrafter47.multiworld.commands;

import PluginReference.MC_Command;
import PluginReference.MC_Player;
import PluginReference.MC_World;
import PluginReference.MC_WorldBiomeType;
import PluginReference.MC_WorldLevelType;
import PluginReference.MC_WorldSettings;
import PluginReference.RainbowUtils;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld._WorldMaster;
import codecrafter47.multiworld.api.ChatPlayer;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.manager.WorldManager;
import codecrafter47.multiworld.util.AlignmentHelper;
import codecrafter47.multiworld.util.ChatUtil;
import joebkt._WorldRegistration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import org.projectrainbow._DiwUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static codecrafter47.multiworld.util.AlignmentHelper.left;

/**
 * Created by florian on 23.11.14.
 */
public class MainCommand implements MC_Command {
    PluginMultiWorld plugin;

    boolean requiresRestart = false;

    private static String frame = "-";
    private static ChatColor backgroundColor = ChatColor.GRAY;
    private static ChatColor textColor = ChatColor.WHITE;
    private static ChatColor actionColor = ChatColor.AQUA;
    private static ChatColor highlightColor = ChatColor.YELLOW;

    public MainCommand(PluginMultiWorld plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommandName() {
        return "MultiWorld";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mw", "worlds", "multiworld");
    }

    @Override
    public String getHelpLine(MC_Player player) {
        return "/MultiWorld";
    }

    @Override
    public void handleCommand(MC_Player player, String[] strings) {
        if (player == null) {
            return;
        }
        if (strings.length == 0) {
            showHelp((ChatPlayer) player);
        } else if (strings[0].equals("tp") && strings.length == 2) {
            Integer id = Integer.valueOf(strings[1]);
            if (plugin.getWorldManager().isLoaded(id)) {
                player.teleport(plugin.getServer().getWorld(id).getSpawnLocation());
            } else {
                ((ChatPlayer) player).sendMessage(ChatUtil.parseString("&cWorld " + plugin.getWorldManager().getName(id) + " is not loaded! *&b[(load now)](/MultiWorld load " + id + ")"));
            }
        } else if (strings[0].equals("create") && strings.length > 1) {
            String name = strings[1];
            if (_WorldMaster.mapDimensionToWorldName.values().contains(name)) {
                ((ChatPlayer) player).sendMessage(ChatUtil.parseString("&cWorld " + name + " already exists"));
                return;
            }
            MC_WorldSettings mc_worldSettings = new MC_WorldSettings();
            mc_worldSettings.generateStructures = true;
            mc_worldSettings.seed = System.currentTimeMillis();
            int id = _WorldMaster.RegisterWorld(name, mc_worldSettings);
            plugin.getStorageManager().getCustomConfig(id).setGenerationType(GenerationType.OVERWORLD);
            plugin.getStorageManager().saveData();
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("remove") && strings.length == 2) {
            if (_WorldMaster.UnregisterWorld(_WorldMaster.GetWorldNameFromDimension(Integer.valueOf(strings[1])))) {
                ((ChatPlayer) player).sendMessage(ChatUtil.parseString(
                        "&aSuccessfully unregistered " + _WorldMaster.GetWorldNameFromDimension(Integer.valueOf(strings[1])) + "."));
                requiresRestart = true;
            } else {
                ((ChatPlayer) player).sendMessage(ChatUtil.parseString("&cThere has been an error deleting the world."));
            }
            showWorldList((ChatPlayer) player, 0);
        } else if (strings[0].equals("load") && strings.length == 2) {
            int id = Integer.valueOf(strings[1]);
            ((ChatPlayer) player).sendMessage(text(ChatColor.GREEN, "Loading world. Please wait..."));
            plugin.getWorldManager().loadWorld(id);
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("list")) {
            int page = 0;
            try {
                page = Integer.parseInt(strings[1]);
            } catch (Throwable ignored) {
            }
            showWorldList((ChatPlayer) player, page);

        } else if (strings[0].equals("modify") && strings.length == 2) {
            int id = Integer.valueOf(strings[1]);
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("modify") && strings.length == 3) {
            int id = Integer.valueOf(strings[1]);
            toggleFlag(player, id, strings[2]);
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("modify") && strings.length == 4) {
            int id = Integer.valueOf(strings[1]);
            setFlag(id, strings[2], strings[3]);
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("gamerule") && strings.length == 4) {
            int id = Integer.valueOf(strings[1]);
            String gamerule = strings[2];
            String value = strings[3];
            _DiwUtils.getMinecraftServer().worldServerForDimension(id).getGameRules().setOrCreateGameRule(gamerule, value);
            showWorldDetails((ChatPlayer) player, id);
        } else if (strings[0].equals("inv") && strings.length == 1) {
            showInvDetails((ChatPlayer) player);
        } else if (strings[0].equals("inv") && strings.length == 3 && strings[1].equals("addgroup")) {
            if (!plugin.getMultiInventoryManager().getGroups().contains(strings[2]))
                plugin.getMultiInventoryManager().addGroup(strings[2]);
            showInvDetails((ChatPlayer) player);
        } else if (strings[0].equals("inv") && strings.length == 4 && strings[1].equals("setgroup")) {
            plugin.getMultiInventoryManager().setGroupForWorld(getWorldByName(strings[2]), strings[3]);
            showInvDetails((ChatPlayer) player);
        } else {
            showHelp((ChatPlayer) player);
        }
    }

    private MC_World getWorldByName(String name) {
        for (MC_World world : plugin.getServer().getWorlds()) {
            if (world.getName().equals(name)) return world;
        }
        throw new RuntimeException("World " + name + " does not exist!");
    }

    private void setFlag(int id, String flag, String value) {
        WorldConfiguration configuration = null;
        _WorldRegistration worldRegistration = null;
        boolean customWorld = false;
        if (id > 1) {
            customWorld = true;
            configuration = plugin.getStorageManager().getCustomConfig(id);
            worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
        }
        switch (flag) {
            case "generationType":
                if (customWorld) {
                    configuration.setGenerationType(GenerationType.valueOf(value));
                    plugin.getStorageManager().saveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    requiresRestart = true;
                break;
            case "levelType":
                if (customWorld) {
                    worldRegistration.settings.levelType = MC_WorldLevelType.valueOf(value);
                    _WorldMaster.SaveData();
                } else {
                    MinecraftServer minecraftServer = _DiwUtils.getMinecraftServer();
                    ((DedicatedServer) minecraftServer).setProperty("level-type", value);
                    ((DedicatedServer) minecraftServer).saveProperties();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    requiresRestart = true;
                break;
            case "biomeType":
                if (customWorld) {
                    worldRegistration.settings.biomeType = MC_WorldBiomeType.valueOf(value);
                    _WorldMaster.SaveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    requiresRestart = true;
                break;
            case "generatorOptions":
                if (customWorld) {
                    configuration.setWorldGeneratorOptions(value);
                    plugin.getStorageManager().saveData();
                } else {
                    MinecraftServer minecraftServer = _DiwUtils.getMinecraftServer();
                    ((DedicatedServer) minecraftServer).setProperty("generator-settings", value);
                    ((DedicatedServer) minecraftServer).saveProperties();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    requiresRestart = true;
                break;
            case "gamemode":
                if (customWorld) {
                    configuration.setGameMode(WorldSettings.GameType.valueOf(value));
                    plugin.getStorageManager().saveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    _DiwUtils.getMinecraftServer().worldServerForDimension(id).getWorldInfo().setGameType(WorldSettings.GameType.valueOf(value));
                break;
            case "difficulty":
                if (customWorld) {
                    configuration.setDifficulty(EnumDifficulty.valueOf(value));
                    plugin.getStorageManager().saveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    _DiwUtils.getMinecraftServer().worldServerForDimension(id).getWorldInfo().setDifficulty(EnumDifficulty.valueOf(value));
                break;
            case "environment":
                if (customWorld) {
                    configuration.setEnvironment(Environment.valueOf(value));
                    plugin.getStorageManager().saveData();
                }
                break;
            case "seed":
                if (customWorld) {
                    worldRegistration.settings.seed = Long.valueOf(value);
                    _WorldMaster.SaveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    requiresRestart = true;
                break;
            case "respawnWorld":
                if (customWorld) {
                    configuration.setRespawnWorld(Integer.valueOf(value));
                    plugin.getStorageManager().saveData();
                }
            case "netherPortalTarget":
                if (customWorld) {
                    if (configuration.getNetherPortalTarget() != -2) {
                        plugin.getStorageManager().getCustomConfig(configuration.getNetherPortalTarget()).setNetherPortalTarget(-2);
                    }
                    configuration.setNetherPortalTarget(Integer.valueOf(value));
                    if (configuration.getNetherPortalTarget() != -2) {
                        plugin.getStorageManager().getCustomConfig(configuration.getNetherPortalTarget()).setNetherPortalTarget(id);
                    }
                    plugin.getStorageManager().saveData();
                }
                break;
            case "endPortalTarget":
                if (customWorld) {
                    if (configuration.getEndPortalTarget() != -2) {
                        plugin.getStorageManager().getCustomConfig(configuration.getEndPortalTarget()).setEndPortalTarget(-2);
                    }
                    configuration.setEndPortalTarget(Integer.valueOf(value));
                    if (configuration.getEndPortalTarget() != -2) {
                        plugin.getStorageManager().getCustomConfig(configuration.getEndPortalTarget()).setEndPortalTarget(id);
                    }
                    plugin.getStorageManager().saveData();
                }
                break;
            default:
                plugin.getLogger().warn("player tried to set invalid flag: " + flag + "=" + value);
        }
    }

    private void toggleFlag(MC_Player player, int id, String string) {
        boolean customWorld = id > 1;
        WorldConfiguration configuration = null;
        _WorldRegistration worldRegistration = null;
        if (customWorld) {
            configuration = plugin.getStorageManager().getCustomConfig(id);
            worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
        }
        switch (string) {
            case "allowAnimals":
                if (customWorld) {
                    configuration.setSpawnAnimals(!configuration.isSpawnAnimals());
                    plugin.getStorageManager().saveData();
                    if (plugin.getWorldManager().isLoaded(id))
                        _DiwUtils.getMinecraftServer().worldServerForDimension(id).setAllowedSpawnTypes(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
                }
                break;
            case "allowMonsters":
                if (customWorld) {
                    configuration.setSpawnMonsters(!configuration.isSpawnMonsters());
                    plugin.getStorageManager().saveData();
                    if (plugin.getWorldManager().isLoaded(id))
                        _DiwUtils.getMinecraftServer().worldServerForDimension(id).setAllowedSpawnTypes(configuration.isSpawnMonsters(), configuration.isSpawnAnimals());
                }
                break;
            case "generateStructures":
                if (customWorld) {
                    worldRegistration.settings.generateStructures = !worldRegistration.settings.generateStructures;
                    _WorldMaster.SaveData();
                    if (plugin.getWorldManager().isLoaded(id))
                        requiresRestart = true;
                }
                break;
            case "keepSpawnInMemory":
                if (customWorld) {
                    configuration.setKeepSpawnInMemory(!configuration.isKeepSpawnInMemory());
                    plugin.getStorageManager().saveData();
                }
                break;
            case "loadOnStartup":
                if (customWorld) {
                    configuration.setLoadOnStartup(!configuration.isLoadOnStartup());
                    plugin.getStorageManager().saveData();
                }
                break;
            case "spawn":
                if (id != player.getWorld().getDimension()) {
                    ((ChatPlayer)player).sendMessage(text(ChatColor.RED, "You're in the wrong world."));
                    break;
                }
                BlockPos spawn = new BlockPos(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                if (customWorld) {
                    configuration.setSpawn(spawn);
                    plugin.getStorageManager().saveData();
                }
                if (plugin.getWorldManager().isLoaded(id))
                    _DiwUtils.getMinecraftServer().worldServerForDimension(id).setSpawnPoint(spawn);
                break;
            default:
                plugin.getLogger().warn("player tried to toggle invalid flag: " + string);
        }
    }

    private void showHelp(ChatPlayer player) {
        // show help
        player.sendMessage(text(textColor, "\n\n\n\n\n\n\n\n\n"));
        player.sendMessage(header());
        if (requiresRestart) {
            player.sendMessage(text(ChatColor.RED, "A restart is required to apply all changes!"));
        }
        player.sendMessage(text(textColor, "The MultiWorld plugin is configured through its interactive chat UI."));
        player.sendMessage(text(textColor, "Try clicking one of the items below."));
        if (!requiresRestart) {
            player.sendMessage();
        }
        player.sendMessage(join(
                text(highlightColor, "Actions: "),
                action(text(actionColor, "Manage worlds"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld list")),
                text(textColor, " | "),
                action(text(actionColor, "Manage inventories"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld inv"))
        ));
        player.sendMessage(join(text(textColor, "Use "), action(text(actionColor, "/MultiWorld create <name>"), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld create ")), text(textColor, " to create a new world.")));
        player.sendMessage();
        player.sendMessage(join(text(textColor, "Worlds: "), text(highlightColor, "" + plugin.getServer().getWorlds().size()),
                text(textColor, "  Entities: "), text(highlightColor, "" + countEntities()),
                text(textColor, "  Loaded chunks: "), text(highlightColor, "" + countChunks())));
        player.sendMessage(line());
    }

    private int countEntities() {
        int result = 0;
        for (MC_World world : plugin.getServer().getWorlds()) {
            result += world.getEntities().size();
        }
        return result;
    }

    private int countChunks() {
        int result = 0;
        for (MC_World world : plugin.getServer().getWorlds()) {
            result += world.getLoadedChunks().size();
        }
        return result;
    }

    private BaseComponent[] header(BaseComponent[]... elements) {
        BaseComponent[] component = hover(action(rainbow("MultiWorld"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld")), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Show main menu")));
        for (BaseComponent[] element : elements) {
            component = join(component, text(textColor, " > "), element);
        }
        return center(component);
    }

    private BaseComponent[] rainbow(String text) {
        return TextComponent.fromLegacyText(RainbowUtils.RainbowString(text));
    }

    private BaseComponent[] join(BaseComponent[]... components) {
        int size = 0;
        for (BaseComponent[] component : components) {
            size += component.length;
        }
        BaseComponent[] result = new BaseComponent[size];
        int i = 0;
        for (BaseComponent[] component : components) {
            for (BaseComponent baseComponent : component) {
                result[i++] = baseComponent;
            }
        }
        return result;
    }

    private BaseComponent[] action(BaseComponent[] components, ClickEvent action) {
        for (BaseComponent component : components) {
            component.setClickEvent(action);
        }
        return components;
    }

    private BaseComponent[] bold(BaseComponent[] components) {
        for (BaseComponent component : components) {
            component.setBold(true);
        }
        return components;
    }

    private BaseComponent[] hover(BaseComponent[] components, HoverEvent action) {
        for (BaseComponent component : components) {
            component.setHoverEvent(action);
        }
        return components;
    }

    private BaseComponent[] text(ChatColor color, String text) {
        TextComponent textComponent = new TextComponent(text);
        textComponent.setColor(color);
        return new BaseComponent[]{textComponent};
    }

    private BaseComponent[] center(BaseComponent[] content) {
        return AlignmentHelper.center(content, "" + backgroundColor + frame);
    }

    private BaseComponent[] line() {
        return center(new BaseComponent[0]);
    }

    @Override
    public boolean hasPermissionToUse(MC_Player player) {
        return player.hasPermission("multiworld.admin");
    }

    @Override
    public List<String> getTabCompletionList(MC_Player player, String[] strings) {
        return new ArrayList<>();
    }

    public void showWorldList(ChatPlayer player, int page) {
        player.sendMessage(header(headerElementManageWorlds()));
        if (requiresRestart) {
            player.sendMessage(text(ChatColor.RED, "A restart is required to apply all changes!"));
        }
        player.sendMessage(join(text(textColor, "Use "), action(text(actionColor, "/MultiWorld create <name>"), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld create ")), text(textColor, " to create a new world.")));
        List<BaseComponent[]> worldListItems = new ArrayList<>();
        SortedSet<Integer> worldIds = new TreeSet<>(plugin.getWorldManager().getWorlds());
        for (MC_World world : plugin.getServer().getWorlds()) {
            worldIds.add(world.getDimension());
        }

        for (int id : worldIds) {
            BaseComponent[] line1 = join(
                    left(text(highlightColor, plugin.getWorldManager().getName(id) + ":"), " ", 100));
            if (plugin.getWorldManager().isLoaded(id)) {
                MC_World world = plugin.getServer().getWorld(id);
                line1 = join(left(join(line1,
                        text(textColor, " Entities: "), text(highlightColor, "" + world.getEntities().size())), " ", 180),
                        text(textColor, " Loaded chunks: "), text(highlightColor, "" + world.getLoadedChunks().size()));
            } else {
                line1 = join(line1, text(textColor, "Not loaded"));
            }
            line1 = join(line1,
                    text(textColor, "\n    ")
            );
            if (id > 1 && _WorldMaster.GetRegistrationFromDimension(id) == null) {
                worldListItems.add(join(
                        line1,
                        text(ChatColor.RED, "World will be removed on restart.")
                ));
            } else if (plugin.getWorldManager().isLoaded(id)) {
                worldListItems.add(join(
                        line1,
                        text(textColor, "Actions: "),
                        hover(action(text(actionColor, "Configure"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to configure settings for this world."))),
                        text(textColor, " | "),
                        hover(action(text(actionColor, "Teleport to world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld tp " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to be instantly teleported to that world."))),
                        text(textColor, " | "),
                        hover(action(text(actionColor, "Remove world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld remove " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to delete the world.")))
                ));
            } else {
                worldListItems.add(join(
                        line1,
                        text(textColor, "Actions: "),
                        hover(action(text(actionColor, "Configure"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to configure settings for this world."))),
                        text(textColor, " | "),
                        hover(action(text(actionColor, "Load world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld load " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to load the world world."))),
                        text(textColor, " | "),
                        hover(action(text(actionColor, "Remove world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld remove " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to delete the world.")))
                ));
            }
        }
        int itemsPerPage = 8;
        int pages = (worldListItems.size() + itemsPerPage - 1) / itemsPerPage;
        if (page >= pages) {
            page = pages - 1;
        }
        if (page < 0) {
            page = 0;
        }
        if (!requiresRestart) {
            player.sendMessage();
        }
        for (int i = page * itemsPerPage; i < page * itemsPerPage + itemsPerPage; i++) {
            if (i < worldListItems.size()) {
                player.sendMessage(worldListItems.get(i));
            } else {
                player.sendMessage(text(textColor, "\n"));
            }
        }
        if (pages > 1) {
            BaseComponent[] navigateLeft;
            if (page > 0) {
                navigateLeft = hover(action(text(actionColor, " < "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld list " + (page - 1))), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Navigate to previous page")));
            } else {
                navigateLeft = text(backgroundColor, " < ");
            }
            BaseComponent[] navigateRight;
            if (page < pages - 1) {
                navigateRight = hover(action(text(actionColor, " > "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld list " + (page + 1))), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Navigate to next page")));
            } else {
                navigateRight = text(backgroundColor, " > ");
            }
            player.sendMessage(center(join(navigateLeft, text(textColor, "" + (page + 1) + "/" + pages), navigateRight)));
        } else {
            player.sendMessage(line());
        }
    }

    private BaseComponent[] headerElementManageWorlds() {
        return hover(action(text(actionColor, "Manage worlds"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld list")), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Show world list")));
    }

    private BaseComponent[] headerElementManageInventories() {
        return hover(action(text(actionColor, "Manage Inventories"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld inv")), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Manage Inventories")));
    }

    private BaseComponent[] headerElementWorld(int id) {
        String name = _WorldMaster.GetWorldNameFromDimension(id);
        return hover(action(text(actionColor, name), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Configure world " + name)));
    }

    private void showInvDetails(ChatPlayer player) {
        player.sendMessage(header(headerElementManageInventories()));
        String groups = "";
        for (Iterator<String> iterator = plugin.getMultiInventoryManager().getGroups().iterator(); iterator.hasNext(); ) {
            String group = iterator.next();
            groups += group;
            if (iterator.hasNext()) groups += ", ";
        }
        BaseComponent[] components = join(text(highlightColor, "Groups: "), text(textColor, groups));
        int lines = AlignmentHelper.getLines(components) + 3;
        player.sendMessage(components);
        player.sendMessage(join(text(textColor, "Use "),
                action(text(actionColor, "/MultiWorld inv addgroup <name>"), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld inv addgroup ")),
                text(textColor, " to add a group")));

        player.sendMessage(join(left(text(highlightColor, "World"), " ", 90), left(text(highlightColor, "Inv-Group"), " ", 80), left(text(highlightColor, "World"), " ", 90), text(highlightColor, "Inv-Group")));

        List<MC_World> worlds = plugin.getServer().getWorlds();
        for (int i = 0; i < worlds.size(); i++) {
            MC_World world = worlds.get(i);
            BaseComponent[] line = left(text(textColor, world.getName()), " ", 90);
            line = left(join(line, action(text(actionColor, plugin.getMultiInventoryManager().getWhereForWorld(world)), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld inv setgroup " + world.getName() + " " + plugin.getMultiInventoryManager().getWhereForWorld(world)))), " ", 170);
            i++;
            if (i < worlds.size()) {
                world = worlds.get(i);
                line = left(join(line, text(textColor, world.getName())), " ", 260);
                line = join(line, action(text(actionColor, plugin.getMultiInventoryManager().getWhereForWorld(world)), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld inv setgroup " + world.getName() + " " + plugin.getMultiInventoryManager().getWhereForWorld(world))));
            }
            lines++;
            player.sendMessage(line);
        }

        while (lines++ < 19) {
            player.sendMessage();
        }

        player.sendMessage(line());
    }

    public void showWorldDetails(ChatPlayer player, int id) {
        int lines = 2;
        WorldManager worldManager = plugin.getWorldManager();
        boolean customWorld = id > 1;
        boolean loaded = worldManager.isLoaded(id);
        // HEADER
        player.sendMessage(header(headerElementManageWorlds(), headerElementWorld(id)));

        if (requiresRestart) {
            player.sendMessage(text(ChatColor.RED, "A restart is required to apply all changes!"));
            lines++;
        }

        // ACTIONS
        if (id <= 1) {
            player.sendMessage(join(
                    text(textColor, "Actions: "),
                    hover(action(text(actionColor, "Teleport to world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld tp " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to be instantly teleported to that world.")))
            ));
        } else if (plugin.getWorldManager().isLoaded(id)) {
            player.sendMessage(join(
                    text(textColor, "Actions: "),
                    hover(action(text(actionColor, "Teleport to world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld tp " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to be instantly teleported to that world."))),
                    text(textColor, " | "),
                    hover(action(text(actionColor, "Remove world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld remove " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to delete the world.")))
            ));
        } else {
            player.sendMessage(join(
                    text(textColor, "Actions: "),
                    hover(action(text(actionColor, "Load world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld load " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to load the world world."))),
                    text(textColor, " | "),
                    hover(action(text(actionColor, "Remove world"), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld remove " + id)), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click here to delete the world.")))
            ));
        }


        WorldConfiguration configuration = null;
        if (customWorld) {
            configuration = plugin.getStorageManager().getCustomConfig(id);
        }
        // WORLD TYPE
        BaseComponent[] text = null;
        if (customWorld) {
            text = text(textColor, "World Type: ");
            for (GenerationType type : GenerationType.values()) {
                if (type == configuration.getGenerationType()) {
                    text = join(text, text(highlightColor, type.name() + " "));
                } else {
                    text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " generationType " + type.name())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // LEVEL TYPE
        _WorldRegistration worldRegistration = null;
        if (customWorld) {
            worldRegistration = _WorldMaster.GetRegistrationFromDimension(id);
            if (configuration.getGenerationType() != GenerationType.NETHER && configuration.getGenerationType() != GenerationType.END) {
                text = text(textColor, "Level Type: ");
                for (MC_WorldLevelType type : MC_WorldLevelType.values()) {
                    if (type == MC_WorldLevelType.UNSPECIFIED) {
                        continue;
                    }
                    if (type == worldRegistration.settings.levelType) {
                        text = join(text, text(highlightColor, type.name() + " "));
                    } else {
                        text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " levelType " + type.name())));
                    }
                }
                lines += AlignmentHelper.getLines(text);
                player.sendMessage(text);
            }
        }
        // BIOME TYPE
        if (customWorld && configuration.getGenerationType() == GenerationType.SINGLE_BIOME) {
            text = text(textColor, "Biome Type: ");
            for (MC_WorldBiomeType type : MC_WorldBiomeType.values()) {
                if (type == MC_WorldBiomeType.UNSPECIFIED) {
                    continue;
                }
                if (type == worldRegistration.settings.biomeType) {
                    text = join(text, text(highlightColor, type.name() + " "));
                } else {
                    text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " biomeType " + type.name())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // GENERATOR OPTIONS
        if (customWorld && configuration.getGenerationType() != GenerationType.NETHER && configuration.getGenerationType() != GenerationType.END && worldRegistration.settings.levelType == MC_WorldLevelType.FLAT) {
            BaseComponent[] components = join(text(textColor, "Generator Options: "), action(text(actionColor, "\"" + configuration.getWorldGeneratorOptions() + "\""), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld modify " + id + " generatorOptions " + configuration.getWorldGeneratorOptions() + " ")));
            lines += AlignmentHelper.getLines(components);
            player.sendMessage(components);
        }
        // SEED
        if (customWorld) {
            BaseComponent[] components = join(text(textColor, "Seed: "), action(text(actionColor, "" + worldRegistration.settings.seed), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld modify " + id + " seed " + worldRegistration.settings.seed + " ")));
            lines += AlignmentHelper.getLines(components);
            player.sendMessage(components);
        }
        // ENVIRONMENT
        if (customWorld && (configuration.getGenerationType() == GenerationType.SINGLE_BIOME || configuration.getGenerationType() == GenerationType.OVERWORLD)) {
            text = hover(text(textColor, "Environment: "), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "The environment property influences\nhow the sky of your world looks like.")));
            for (Environment type : Environment.values()) {
                if (type == configuration.getEnvironment()) {
                    text = join(text, text(highlightColor, type.name() + " "));
                } else {
                    text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " environment " + type.name())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // GAMEMODE
        text = text(textColor, "Game Mode: ");
        WorldSettings.GameType current = null;
        if (customWorld) {
            current = configuration.getGameMode();
        } else {
            current = _DiwUtils.getMinecraftServer().worldServerForDimension(id).getWorldInfo().getGameType();
        }
        for (WorldSettings.GameType type : WorldSettings.GameType.values()) {
            if (type == WorldSettings.GameType.NOT_SET) {
                continue;
            }
            if (type == current) {
                text = join(text, text(highlightColor, type.name() + " "));
            } else {
                text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " gamemode " + type.name())));
            }
        }
        lines += AlignmentHelper.getLines(text);
        player.sendMessage(text);
        // DIFFICULTY
        text = text(textColor, "Difficulty: ");
        EnumDifficulty currentDifficulty;
        if (customWorld) {
            currentDifficulty = configuration.getDifficulty();
        } else {
            currentDifficulty = _DiwUtils.getMinecraftServer().worldServerForDimension(id).getDifficulty();
        }
        for (EnumDifficulty type : EnumDifficulty.values()) {
            if (type == currentDifficulty) {
                text = join(text, text(highlightColor, type.name() + " "));
            } else {
                text = join(text, action(text(actionColor, type.name() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " difficulty " + type.name())));
            }
        }
        lines += AlignmentHelper.getLines(text);
        player.sendMessage(text);
        // SPAWN
        BlockPos spawn;
        if (customWorld) {
            spawn = configuration.getSpawn();
        } else {
            spawn = _DiwUtils.getMinecraftServer().worldServerForDimension(id).getSpawnPoint();
        }
        if (spawn != null) {
            BaseComponent[] components = join(text(textColor, "Spawn: "), hover(action(text(actionColor, "" + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ()), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " spawn")), new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(textColor, "Click to set spawn to your position"))));
            lines += AlignmentHelper.getLines(components);
            player.sendMessage(components);
        }
        // FLAGS
        if (customWorld) {
            text = join(text(textColor, "Flags: "),
                    action(join(configuration.isSpawnAnimals() ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, "allowAnimals ")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " allowAnimals")),
                    action(join(configuration.isSpawnMonsters() ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, "allowMonsters ")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " allowMonsters")),
                    action(join(worldRegistration.settings.generateStructures ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, "generateStructures ")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " generateStructures")),
                    action(join(configuration.isKeepSpawnInMemory() ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, "keepSpawnInMemory ")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " keepSpawnInMemory")),
                    action(join(configuration.isLoadOnStartup() ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, "loadOnStartup")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " loadOnStartup"))
            );
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // GAMERULES
        if (plugin.getWorldManager().isLoaded(id)) {
            GameRules gameRules = _DiwUtils.getMinecraftServer().worldServerForDimension(id).getWorldInfo().getGameRulesInstance();
            text = text(textColor, "Game Rules: ");
            for (String gamerule : gameRules.getRules()) {
                if (gameRules.areSameType(gamerule, GameRules.ValueType.BOOLEAN_VALUE)) {
                    // this is a boolean rule
                    text = join(text, action(join(gameRules.getBoolean(gamerule) ? text(ChatColor.GREEN, "✔") : text(ChatColor.RED, "✖"), text(actionColor, gamerule + " ")), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld gamerule " + id + " " + gamerule + " " + !gameRules.getBoolean(gamerule))));
                } else {
                    // this is free text rule
                    text = join(text, action(text(actionColor, gamerule + "=" + gameRules.getString(gamerule) + " "), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/MultiWorld gamerule " + id + " " + gamerule + " " + gameRules.getString(gamerule))));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // RESPAWN WORLD
        if (customWorld) {
            text = text(textColor, "Respawn world: ");
            for (MC_World world : plugin.getServer().getWorlds()) {
                if (world.getDimension() == configuration.getRespawnWorld()) {
                    text = join(text, text(highlightColor, world.getName() + " "));
                } else {
                    text = join(text, action(text(actionColor, world.getName() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " respawnWorld " + world.getDimension())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // NETHER PORTAL TARGET
        if (customWorld && configuration.getGenerationType() == GenerationType.OVERWORLD) {
            text = text(textColor, "Nether world: ");
            if (-2 == configuration.getNetherPortalTarget()) {
                text = join(text, text(highlightColor, "NONE "));
            } else {
                text = join(text, action(text(actionColor, "NONE "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " netherPortalTarget -2")));
            }
            for (MC_World world : plugin.getServer().getWorlds()) {
                if (world.getDimension() <= 2) continue;
                if (configuration.getGenerationType() == GenerationType.OVERWORLD && plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.NETHER)
                    continue;
                if (configuration.getGenerationType() == GenerationType.NETHER && plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.OVERWORLD)
                    continue;
                if (world.getDimension() == configuration.getNetherPortalTarget()) {
                    text = join(text, text(highlightColor, world.getName() + " "));
                } else {
                    text = join(text, action(text(actionColor, world.getName() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " netherPortalTarget " + world.getDimension())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }
        // END PORTAL TARGET
        if (customWorld && configuration.getGenerationType() == GenerationType.OVERWORLD) {
            text = text(textColor, "End world: ");
            if (-2 == configuration.getEndPortalTarget()) {
                text = join(text, text(highlightColor, "NONE "));
            } else {
                text = join(text, action(text(actionColor, "NONE "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " endPortalTarget -2")));
            }
            for (MC_World world : plugin.getServer().getWorlds()) {
                if (world.getDimension() <= 2) continue;
                if (plugin.getStorageManager().getCustomConfig(world.getDimension()).getGenerationType() != GenerationType.END)
                    continue;
                if (world.getDimension() == configuration.getEndPortalTarget()) {
                    text = join(text, text(highlightColor, world.getName() + " "));
                } else {
                    text = join(text, action(text(actionColor, world.getName() + " "), new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/MultiWorld modify " + id + " endPortalTarget " + world.getDimension())));
                }
            }
            lines += AlignmentHelper.getLines(text);
            player.sendMessage(text);
        }

        while (lines++ < 19) {
            player.sendMessage();
        }

        player.sendMessage(line());
    }
}
