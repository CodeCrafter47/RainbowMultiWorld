package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.manager.StorageManager;
import net.minecraft.command.CommandDefaultGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandDefaultGameMode.class)
public abstract class MixinCommandDefaultGameMode {

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "net.minecraft.command.CommandDefaultGameMode.setDefaultGameType(Lnet/minecraft/world/GameType;Lnet/minecraft/server/MinecraftServer;)V"))
    void onChangeDefaultGameMode(CommandDefaultGameMode self, GameType defaultGameType, MinecraftServer minecraftServer, MinecraftServer var1, ICommandSender commandSender, String[] args) {
        if (commandSender.getEntityWorld() instanceof CustomWorldServer) {
            commandSender.getEntityWorld().getWorldInfo().setGameType(defaultGameType);
            int worldId = ((CustomWorldServer) commandSender.getEntityWorld()).getWorldId();
            StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
            storageManager.getCustomConfig(worldId).setGameMode(defaultGameType);
            storageManager.saveData();
            if (minecraftServer.getForceGamemode()) {
                for (EntityPlayerMP entityPlayerMP : minecraftServer.getPlayerList().getPlayerList()) {
                    if (entityPlayerMP.dimension == worldId) {
                        entityPlayerMP.setGameType(defaultGameType);
                    }
                }
            }
        } else {
            setDefaultGameType(defaultGameType, minecraftServer);
        }
    }

    @Overwrite
    protected void setDefaultGameType(GameType var1, MinecraftServer var2) {
        var2.setGameType(var1);
        if (var2.getForceGamemode()) {
            for (EntityPlayerMP var4 : var2.getPlayerList().getPlayerList()) {
                if (var4.dimension < 2) {
                    var4.setGameType(var1);
                }
            }
        }
    }
}
