package MultiWorld.transformers;

import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import WrapperObjects.ServerWrapper;
import joebkt._JoeUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Created by florian on 22.11.14.
 */
public class DummyTransformAgent implements ClassFileTransformer {
	//All pretransformed classes should be located in the pretransformedclasses folder inside the jar

	private static Instrumentation instrumentation = null;
	private static DummyTransformAgent transformer;

	private static Object mixinTransformer;

	static boolean failure = false;

    private static Set<String> transformedClasses = new HashSet<>();

	// Public static void main() but for this agent
	@SuppressWarnings("unchecked")
	public static void agentmain(String string, Instrumentation instrument) {
		instrumentation = instrument;

		LogManager.getLogger().info("Loaded transformer agent!");

		transformer = new DummyTransformAgent();
		instrumentation.addTransformer(transformer, true);
		try {
			// redefine classes
			instrumentation.appendToSystemClassLoaderSearch(new JarFile(new File("plugins_mod" + File.separator + "MultiWorld.jar")));
			mixinTransformer = new MixinTransformer(DummyTransformAgent.class.getClassLoader().getResourceAsStream("MultiWorld/mixins.json"));

			for (String s : new ArrayList<>(((MixinTransformer) mixinTransformer).getMixinTargets())) {
                if(!transformedClasses.contains(s)) {
                    Class<?> aClass = Class.forName(s);
                    if(!transformedClasses.contains(s)) {
                        instrumentation.retransformClasses(aClass);
                        if (failure || !transformedClasses.contains(s)) return;
                    }
                }
			}

			// init the real plugin
			PluginBase plugin = (PluginBase) Class.forName("codecrafter47.multiworld.PluginMultiWorld").getDeclaredConstructors()[0].newInstance();
			plugin.onStartup(new ServerWrapper());
			for (PluginInfo info : _JoeUtils.plugins) {
				if (info.name.equalsIgnoreCase("MultiWorld")) {
					info.ref = plugin;
				}
			}
		}
		catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	@Override
	@SneakyThrows
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		String realName = className.replaceAll("/", ".");
		if (((MixinTransformer) mixinTransformer).getMixinTargets().contains(realName)) {
			try {
                transformedClasses.add(realName);
				return ((MixinTransformer) mixinTransformer).transform(realName, realName, classfileBuffer);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				failure = true;
			}
		}
		return null;
	}
}
