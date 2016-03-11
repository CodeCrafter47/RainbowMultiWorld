package codecrafter47.multiworld.mixins;

import PluginReference.MC_World;
import codecrafter47.multiworld.CustomWorldServer;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
public class MixinWorldServer {
    @Redirect(method = "updateWeather", at = @At(value = "INVOKE", target = "net.minecraft.world.DimensionType.getId()I"))
    private int getDimension(DimensionType dimensionType) {
        WorldServer ws = (WorldServer) (Object) this;
        if (ws instanceof CustomWorldServer) {
            return ((CustomWorldServer) ws).getWorldId();
        }
        return dimensionType.getId();
    }

    @Redirect(method = "updateWeather", at = @At(value = "INVOKE", target = "net.minecraft.server.management.PlayerList.sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void sendPacket(PlayerList playerList, Packet<?> packet) {
        playerList.sendPacketToAllPlayersInDimension(packet, ((MC_World) this).getDimension());
    }
}
