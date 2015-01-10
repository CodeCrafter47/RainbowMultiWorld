package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 10.01.15.
 */
@Mixin(joebkt.CmdToggleDownFall.class)
public abstract class FixCmdToggleDownFall extends CommandAbstract {

    @Shadow
    protected abstract void d();

    @Overwrite
    public void handleCommand(CommandSender var1, String[] var2) throws di_BaseException {
        if(var1 instanceof EntityPlayer){
            WorldData var3 = MinecraftServer.getServer().worldServers[((EntityPlayer) var1).dimension].getWorldData();
            var3.setRaining(!var3.getIsRaining());
        } else {
            this.d();
        }
        a(var1, this, "commands.downfall.success", new Object[0]);
    }
}
