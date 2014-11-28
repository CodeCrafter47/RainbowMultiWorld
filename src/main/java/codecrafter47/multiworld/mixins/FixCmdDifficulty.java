package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 28.11.14.
 */
@Mixin(joebkt.CmdDifficulty.class)
public abstract class FixCmdDifficulty extends CommandAbstract {

	@Shadow
	protected abstract Difficulty e(String var1) throws di_BaseException;

	@Overwrite
	public void handleCommand(CommandSender commandSender, String[] args) throws di_BaseException {
		if (args.length <= 0) {
			throw new dp("commands.difficulty.usage");
		}
		else {
			Difficulty difficulty = this.e(args[0]);
			if (commandSender instanceof EntityPlayer) {
				((EntityPlayer)commandSender).getWorldServer().getWorldData().setDifficulty(difficulty);
				PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(
						((EntityPlayer) commandSender).getWorldServer().worldProvider.getDimenIdx()).setDifficulty(difficulty);
				PluginMultiWorld.getInstance().getStorageManager().saveData();
			}
			else {
				MinecraftServer.getServer().getWorldServerByDimension(0).getWorldData().setDifficulty(difficulty);
			}
			a(commandSender, this, "commands.difficulty.success", new ChatMessage(difficulty.b(), new Object[0]));
		}
	}
}
