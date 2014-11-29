package codecrafter47.multiworld;

import PluginReference.MC_Server;
import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import codecrafter47.multiworld.commands.MainCommand;
import codecrafter47.multiworld.manager.HookManager;
import codecrafter47.multiworld.manager.StorageManager;
import codecrafter47.multiworld.manager.WorldManager;
import joebkt._WorldMaster;
import joebkt._WorldRegistration;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by florian on 23.11.14.
 */
// This is the real Plugin
public class PluginMultiWorld extends PluginBase {
	private Logger logger = LogManager.getLogger();

	@Getter
	private MC_Server server;

	@Getter
	private static PluginMultiWorld instance;

	@Getter
	private WorldManager worldManager;

	@Getter
	private StorageManager storageManager;

	@Getter
	private HookManager hookManager = new HookManager();

	@Override public void onStartup(MC_Server argServer) {
		instance = this;

		server = argServer;

		storageManager = new StorageManager(this);
		worldManager = new WorldManager();

		// register commands
		server.registerCommand(new MainCommand(this));
	}

	@Override public void onServerFullyLoaded() {
		getHookManager().scanForHooks(getServer());
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
}
