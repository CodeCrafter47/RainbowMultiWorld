package MultiWorld.transformers;

import PluginReference.PluginBase;
import PluginReference.PluginInfo;
import WrapperObjects.ServerWrapper;
import joebkt._JoeUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
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

	// Public static void main() but for this agent
	@SuppressWarnings("unchecked")
	public static void agentmain(String string, Instrumentation instrument) {
		instrumentation = instrument;

		LogManager.getLogger().info("Loaded transformer agent!");

		transformer = new DummyTransformAgent();
		instrumentation.addTransformer(transformer);
		try {
			// redefine classes
			instrumentation.appendToSystemClassLoaderSearch(new JarFile(new File("plugins_mod" + File.separator + "MultiWorld.jar")));

			mixinTransformer = new MixinTransformer(DummyTransformAgent.class.getClassLoader().getResourceAsStream("MultiWorld/mixins.json"));

			for (String s : ((MixinTransformer) mixinTransformer).getMixinTargets()) {
				instrumentation.redefineClasses(new ClassDefinition(Class.forName(s),getBaseClass(s)));
				if(failure)return;
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
				return ((MixinTransformer) mixinTransformer).transform(realName, realName, classfileBuffer);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				failure = true;
			}
		}
		return classfileBuffer;
	}

	@SneakyThrows
	public static byte[] getBaseClass(String className) {
		InputStream stream = DummyTransformAgent.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[4096];
		while ((nRead = stream.read(data)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}
}
