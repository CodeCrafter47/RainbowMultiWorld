package MultiWorld;

import MultiWorld.transformers.DummyTransformAgent;
import MultiWorld.transformers.MixinTransformer;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import lombok.SneakyThrows;
import sun.tools.attach.BsdAttachProvider;
import sun.tools.attach.LinuxAttachProvider;
import sun.tools.attach.SolarisAttachProvider;
import sun.tools.attach.WindowsAttachProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by florian on 22.11.14.
 */
public class Injector {
	MyPlugin plugin;

	public Injector(MyPlugin plugin) {
		this.plugin = plugin;
	}

	@SneakyThrows
	void injectMixins() {
		plugin.saveResource("libraries/natives/32/linux/libattach.so", true);
		plugin.saveResource("libraries/natives/32/solaris/libattach.so", true);
		plugin.saveResource("libraries/natives/32/windows/attach.dll", true);
		plugin.saveResource("libraries/natives/64/linux/libattach.so", true);
		plugin.saveResource("libraries/natives/64/mac/libattach.dylib", true);
		plugin.saveResource("libraries/natives/64/solaris/libattach.so", true);
		plugin.saveResource("libraries/natives/64/windows/attach.dll", true);

		File jarFile = new File(plugin.getDataFolder(), "MixinTransformAgent.jar");
		if (jarFile.exists()) {
			jarFile.delete();
		}
		jarFile.deleteOnExit();

		Manifest manifest = new Manifest();
		Attributes mainAttributes = manifest.getMainAttributes();
		// Create manifest stating that agent is allowed to transform classes
		mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttributes.put(new Attributes.Name("Agent-Class"), DummyTransformAgent.class.getName());
		mainAttributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
		mainAttributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");

		JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);

		jos.putNextEntry(new JarEntry(DummyTransformAgent.class.getName().replace('.', '/') + ".class"));
		jos.write(Tools.getBytesFromStream(
				DummyTransformAgent.class.getClassLoader().getResourceAsStream(DummyTransformAgent.class.getName().replaceAll("\\.", "/") + ".class")));
		jos.closeEntry();

		jos.putNextEntry(new JarEntry(MixinTransformer.class.getName().replace('.', '/') + ".class"));
		jos.write(Tools.getBytesFromStream(
				MixinTransformer.class.getClassLoader().getResourceAsStream(MixinTransformer.class.getName().replaceAll("\\.", "/") + ".class")));
		jos.closeEntry();

		jos.flush();
		jos.close();

		Tools.addToLibPath(getLibraryPath(new File(plugin.getDataFolder(), "libraries/natives/").getPath()));
		AttachProvider.setAttachProvider(getAttachProvider());
		VirtualMachine vm = VirtualMachine.attach(Tools.getCurrentPID());

		vm.loadAgent(jarFile.getAbsolutePath());
	}

	@SneakyThrows
	public static byte[] getBaseClass(String className) {
		InputStream stream = MyPlugin.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[4096];
		while ((nRead = stream.read(data)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	private static String getLibraryPath(String parentDir) {
		String path = Tools.Platform.is64Bit() ? "64/" : "32/";
		switch (Tools.Platform.getPlatform()) {
			case LINUX:
				path += "linux/";
				break;
			case WINDOWS:
				path += "windows/";
				break;
			case MAC:
				path += "mac/";
				break;
			case SOLARIS:
				path += "solaris/";
				break;
			default:
				throw new UnsupportedOperationException("unsupported platform");
		}
		return new File(parentDir, path).getAbsolutePath();
	}

	private static AttachProvider getAttachProvider() {
		switch (Tools.Platform.getPlatform()) {
			case LINUX:
				return new LinuxAttachProvider();
			case WINDOWS:
				return new WindowsAttachProvider();
			case MAC:
				return new BsdAttachProvider();
			case SOLARIS:
				return new SolarisAttachProvider();
			default:
				throw new UnsupportedOperationException("unsupported platform");
		}
	}
}
