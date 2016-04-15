package codecrafter47.multiworld.mixins;

import PluginReference.MC_WorldSettings;
import codecrafter47.multiworld._WorldMaster;
import org.projectrainbow.ServerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerWrapper.class)
public class MixinServerWrapper {

    @Overwrite
    public boolean unregisterWorld(String worldName) {
        return _WorldMaster.UnregisterWorld(worldName);
    }

    @Overwrite
    public int registerWorld(String worldName, MC_WorldSettings settings) {
        return _WorldMaster.RegisterWorld(worldName, settings);
    }
}
