package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

import java.util.Date;

/**
 * Created by florian on 26.11.14.
 */
@Mixin(joebkt.PlayerConnection.class)
public class MixinRespawnWorld {

	@Shadow
	public EntityPlayer m_player;

	@Shadow
	private MinecraftServer m_server;

	@Overwrite
	public void a(Packet_IncomingClientStatus var1) {
		ig.a(var1, (InterfacePacketTextObjectRelated) this, this.m_player.getWorldServer());
		this.m_player.z();
		EnumRespawnStatsOrOpenInvAchieve var2 = var1.a();
		if (var2 == EnumRespawnStatsOrOpenInvAchieve.PERFORM_RESPAWN) {
			int respawnDimension;
			respawnDimension = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.m_player.dimension).getRespawnWorld();
			if (!PluginMultiWorld.getInstance().getWorldManager().isLoaded(respawnDimension)) {
				PluginMultiWorld.getInstance().getLogger().warn("Invalid respawn dimension " + respawnDimension + " setting to 0");
				respawnDimension = 0;
			}
			if (this.m_player.viewingCredits) {
				this.m_player = this.m_server.getThePlayerList().respawnPlayer(this.m_player, respawnDimension, true);
			}
			else if (this.m_player.getWorldServer().getWorldData().isHardcore()) {
				if (this.m_server.isSinglePlayer() && this.m_player.getName().equals(this.m_server.getSinglePlayerName())) {
					this.m_player.plrConnection.disconnect("You have died. Game over, man, it\'s game over!");
					this.m_server.Z();
				}
				else {
					sw_BanByNameUUID var3 = new sw_BanByNameUUID(this.m_player.getGameProfile(), (Date) null, "(You just lost the game)", (Date) null, "Death in Hardcore");
					this.m_server.getThePlayerList().getBannedPlayers().a((sr) var3);
					this.m_player.plrConnection.disconnect("You have died. Game over, man, it\'s game over!");
				}
			}
			else {
				if (this.m_player.getHealth() > 0.0F) {
					return;
				}

				this.m_player = this.m_server.getThePlayerList().respawnPlayer(this.m_player, respawnDimension, false);
			}

		}
		else if (var2 == EnumRespawnStatsOrOpenInvAchieve.REQUEST_STATS) {
			this.m_player.getStatMapManager().sendPacketStats(this.m_player);

		}
		else if (var2 == EnumRespawnStatsOrOpenInvAchieve.OPEN_INVENTORY_ACHIEVEMENT) {
			this.m_player.setStatAsAcquired(Achievement.openInventory);
		}
	}

}
