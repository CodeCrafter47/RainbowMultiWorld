package MultiWorld;

import PluginReference.MC_Server;
import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by florian on 16.11.14.
 */
public class MyPlugin extends PluginBase {

	MC_Server server;


	@Override public void onStartup(MC_Server argServer) {
		server = argServer;
		// here's the place to inject magic
		System.out.println("Injecting classes");
		Injector injector = new Injector(this);
		injector.injectMixins();
		System.out.println("Injection finished");
	}

	@Override public void onServerFullyLoaded() {
		LogManager.getLogger().error("Failed to initialize the MultiWorld plugin - stopping server");
		server.executeCommand("stop");
	}

	@SneakyThrows void saveResource(String name, boolean overwrite) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
		if(inputStream == null)throw new RuntimeException("Built-in resource not found: " + name);
		File file = new File(getDataFolder(), name);
		Files.createDirectories(file.getParentFile().toPath());
		if(overwrite && file.exists())Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		else if(!file.exists()) Files.copy(inputStream, file.toPath());
	}

	File getDataFolder(){
		return new File("plugins_mod" + File.separator + getPluginInfo().name);
	}

	@Override public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		if(info == null){
			info = new PluginInfo();
		}
		info.name = "MultiWorld";
		info.ref = this;
		info.version = "0.1";
		return info;
	}

}
