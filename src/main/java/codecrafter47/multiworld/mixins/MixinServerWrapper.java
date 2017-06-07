package codecrafter47.multiworld.mixins;

import PluginReference.MC_WorldSettings;
import codecrafter47.multiworld._WorldMaster;
import org.projectrainbow.ServerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerWrapper.class)
public class MixinServerWrapper {

    // No @Overwrite because the method is not obfuscated
    public boolean unregisterWorld(String worldName) {
        return _WorldMaster.UnregisterWorld(worldName);
    }

    // No @Overwrite because the method is not obfuscated
    public int registerWorld(String worldName, MC_WorldSettings settings) {
        return _WorldMaster.RegisterWorld(worldName, settings);
    }
}
