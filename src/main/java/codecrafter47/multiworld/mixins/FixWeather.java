package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 10.01.15.
 */
@Mixin(joebkt.WorldServer.class)
public abstract class FixWeather extends World{

    @Shadow
    private final MinecraftServer m_server = null;

    protected FixWeather(IDataManager var1, WorldData argWorldData, WorldProvider var3, MethodProfiler var4, boolean someFlag) {
        super(var1, argWorldData, var3, var4, someFlag);
    }

    @Overwrite
    protected void handleWeather() {
        super.handleWeather();
        this.m_server.getThePlayerList().a(new Packet_ChangeGameState(7, this.p), this.worldProvider.getDimenIdx());

        this.m_server.getThePlayerList().a(new Packet_ChangeGameState(8, this.r), this.worldProvider.getDimenIdx());

        if (!this.S()) {
            this.m_server.getThePlayerList().a(new Packet_ChangeGameState(2, 0.0F), this.worldProvider.getDimenIdx());
        } else {
            this.m_server.getThePlayerList().a(new Packet_ChangeGameState(1, 0.0F), this.worldProvider.getDimenIdx());
        }
        this.m_server.getThePlayerList().a(new Packet_ChangeGameState(7, this.p), this.worldProvider.getDimenIdx());

        this.m_server.getThePlayerList().a(new Packet_ChangeGameState(8, this.r), this.worldProvider.getDimenIdx());

    }
}
