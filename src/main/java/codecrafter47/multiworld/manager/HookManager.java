package codecrafter47.multiworld.manager;

import PluginReference.MC_Server;
import PluginReference.MC_World;
import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by florian on 29.11.14.
 */
public class HookManager {
	Set<Pair<PluginBase, Method>> hooksLoadWorld = new HashSet<>();

	public void scanForHooks(MC_Server server){
		for(PluginInfo info: server.getPlugins()){
			try {
				hooksLoadWorld.add(new ImmutablePair<PluginBase, Method>(info.ref, info.ref.getClass().getMethod("onWorldLoaded", MC_World.class)));
			}
			catch (NoSuchMethodException ignored) {
			}
		}
	}

	public void callWorldLoadedHooks(MC_World world){
		for(Pair<PluginBase, Method> hook: hooksLoadWorld){
			try {
				hook.getValue().invoke(hook.getLeft(), world);
			}
			catch (Throwable e) {
				LogManager.getLogger().warn("Unable to pass hook to a plugin", e);
			}
		}
	}
}
