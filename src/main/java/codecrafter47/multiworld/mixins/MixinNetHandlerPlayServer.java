package codecrafter47.multiworld.mixins;

import PluginReference.MC_World;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.WorldConfiguration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketRespawn;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
    @Shadow
    public EntityPlayerMP playerEntity;

    @ModifyArg(method = "processClientStatus", at = @At(value = "INVOKE", target = "net.minecraft.server.management.PlayerList.recreatePlayerEntity(Lnet/minecraft/entity/player/EntityPlayerMP;IZ)Lnet/minecraft/entity/player/EntityPlayerMP;"))
    private int onRespawn(int oldDim) {
        int respawnDimension = oldDim;
        if (playerEntity.dimension > 1) {
            respawnDimension = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.playerEntity.dimension).getRespawnWorld();
            if (!PluginMultiWorld.getInstance().getWorldManager().isLoaded(respawnDimension)) {
                PluginMultiWorld.getInstance().getLogger().warn("Invalid respawn dimension " + respawnDimension + " setting to 0");
                respawnDimension = 0;
            }
        }
        return respawnDimension;
    }

    @ModifyArg(method = "handleSpectate", at = @At(value = "INVOKE", target = "net/minecraft/network/play/server/SPacketRespawn.<init>(ILnet/minecraft/world/EnumDifficulty;Lnet/minecraft/world/WorldType;Lnet/minecraft/world/WorldSettings$GameType;)V"))
    private int onSpectate(int dimension) {
        dimension = getDimensionByEnvironment(dimension);
        int oldClientDimension = getDimensionByEnvironment(((MC_World)playerEntity.worldObj).getDimension());
        if (oldClientDimension == dimension) {
             playerEntity.playerNetServerHandler.sendPacket(new SPacketRespawn((byte) (dimension >= 0 ? -1 : 0), playerEntity.worldObj.getDifficulty(), playerEntity.worldObj.getWorldInfo().getTerrainType(), playerEntity.theItemInWorldManager.getGameType()));
        }
        return dimension;
    }

    private int getDimensionByEnvironment(int worldId) {
        if (worldId > 1) {
            WorldConfiguration customConfig = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(worldId);
            if (customConfig.getEnvironment() == Environment.NETHER) {
                return -1;
            } else if (customConfig.getEnvironment() == Environment.END) {
                return 1;
            } else {
                return 0;
            }
        }
        return worldId;
    }
}
