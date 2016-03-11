package codecrafter47.multiworld;

import PluginReference.MC_Player;
import PluginReference.MC_Server;
import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import codecrafter47.multiworld.commands.MainCommand;
import codecrafter47.multiworld.commands.TPCommand;
import codecrafter47.multiworld.manager.HookManager;
import codecrafter47.multiworld.manager.MultiInventoryManager;
import codecrafter47.multiworld.manager.StorageManager;
import codecrafter47.multiworld.manager.WorldManager;
import joebkt._WorldRegistration;
import lombok.SneakyThrows;
import net.minecraft.command.ServerCommandManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectrainbow._DiwUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Created by florian on 23.11.14.
 */
public class PluginMultiWorld extends PluginBase {
	private Logger logger = LogManager.getLogger();

	private MC_Server server;

	private static PluginMultiWorld instance;

	private WorldManager worldManager;

	private StorageManager storageManager;

	private HookManager hookManager = new HookManager();

	MultiInventoryManager multiInventoryManager;

	public static PluginMultiWorld getInstance() {
		return PluginMultiWorld.instance;
	}

	@Override public void onStartup(MC_Server argServer) {
		instance = this;

		getDataFolder().mkdirs();

		server = argServer;

		storageManager = new StorageManager(this);
		worldManager = new WorldManager();
		multiInventoryManager = new MultiInventoryManager(this);

		// register commands
		server.registerCommand(new MainCommand(this));
		((ServerCommandManager) _DiwUtils.getMinecraftServer().getCommandManager()).registerCommand(new TPCommand());
	}

	@Override public void onTick(int tickNumber) {
		multiInventoryManager.checkWorldChange();
	}

	@Override public void onPlayerJoin(MC_Player plr) {
		getMultiInventoryManager().onPlayerJoin(plr);
	}

	@Override public void onPlayerLogout(String playerName, UUID uuid) {
		getMultiInventoryManager().onPlayerDisconnect(uuid);
	}

	@Override public void onServerFullyLoaded() {
		getHookManager().scanForHooks(getServer());
	}

	@Override
	public void onShutdown() {
		_WorldMaster.SaveData();
	}

	public void onItsTimeToLoadCustomWorlds() {
		for (_WorldRegistration registration : _WorldMaster.worldRegs) {
			if (getStorageManager().getCustomConfig(registration.dimension).isLoadOnStartup()) {
				getWorldManager().loadWorld(registration.dimension);
			}
		}
	}

	@SneakyThrows void saveResource(String name, boolean overwrite) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
		if (inputStream == null) {
			throw new RuntimeException("Built-in resource not found: " + name);
		}
		File file = new File(getDataFolder(), name);
		Files.createDirectories(file.getParentFile().toPath());
		if (overwrite && file.exists()) {
			Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else if (!file.exists()) {
			Files.copy(inputStream, file.toPath());
		}
	}

	public File getDataFolder() {
		return new File("plugins_mod" + File.separator + getPluginInfo().name);
	}

	@Override public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		if (info == null) {
			info = new PluginInfo();
		}
		info.name = "MultiWorld";
		info.ref = this;
		info.version = "0.1";
		return info;
	}

	public Logger getLogger() {
		return logger;
	}

	public MC_Server getServer() {
		return this.server;
	}

	public WorldManager getWorldManager() {
		return this.worldManager;
	}

	public StorageManager getStorageManager() {
		return this.storageManager;
	}

	public HookManager getHookManager() {
		return this.hookManager;
	}

	public MultiInventoryManager getMultiInventoryManager() {
		return this.multiInventoryManager;
	}
}
