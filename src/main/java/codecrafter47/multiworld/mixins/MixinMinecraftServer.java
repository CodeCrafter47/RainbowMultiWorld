package codecrafter47.multiworld.mixins;

import PluginReference.MC_World;
import PluginReference.PluginInfo;
import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import org.projectrainbow.ServerWrapper;
import org.projectrainbow._DiwUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Shadow
    public WorldServer[] worlds;

    @Shadow
    private int tickCounter;

    @Shadow
    private PlayerList playerList;

    @Inject(method = "loadAllWorlds", at = @At("RETURN"))
    private void onWorldsLoaded(String var1, String var2, long var3, WorldType var5, String var6, CallbackInfo ci) {
        Launch.classLoader.addClassLoaderExclusion("codecrafter47.multiworld.api.");
        PluginMultiWorld pluginMultiWorld = new PluginMultiWorld();
        pluginMultiWorld.onStartup(ServerWrapper.getInstance());
        PluginInfo pluginInfo = pluginMultiWorld.getPluginInfo();
        pluginInfo.ref = pluginMultiWorld;
        _DiwUtils.pluginManager.plugins.add(pluginInfo);
        PluginMultiWorld.getInstance().onItsTimeToLoadCustomWorlds();
    }

    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "net.minecraft.server.management.PlayerList.sendPacketToAllPlayersInDimension(Lnet/minecraft/network/Packet;I)V"))
    private void getDimension(PlayerList playerList, Packet<?> packet, int dimension) {
        // we do that below
    }

    @Inject(method = "updateTimeLightAndEntities", at = @At("HEAD"))
    private void updateTime(CallbackInfo ci) {
        if (this.tickCounter % 20 == 0) {
            for (WorldServer worldServer : worlds) {
                this.playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldServer.getTotalWorldTime(), worldServer.getWorldTime(), worldServer.getGameRules().getBoolean("doDaylightCycle")), ((MC_World) worldServer).getDimension());
            }
        }
    }

    @Inject(method = "worldServerForDimension", at = @At("HEAD"), cancellable = true)
    private void worldServerForDimension(int dimension, CallbackInfoReturnable<WorldServer> ci) {
        if (dimension > 1) {
            for (int i = 3; i < worlds.length; i++) {
                WorldServer worldServer = worlds[i];
                if (worldServer instanceof CustomWorldServer && ((CustomWorldServer) worldServer).getWorldId() == dimension) {
                    ci.setReturnValue(worldServer);
                    return;
                }
            }
        }
    }

    @Overwrite
    public void setGameType(GameType var1) {
        for (int var2 = 0; var2 < 3; ++var2) {
            this.worlds[var2].getWorldInfo().setGameType(var1);
        }
    }
}
