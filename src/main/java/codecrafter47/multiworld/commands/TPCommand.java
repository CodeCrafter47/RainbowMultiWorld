package codecrafter47.multiworld.commands;

import PluginReference.MC_World;
import WrapperObjects.Entities.PlayerWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import joebkt.*;

/**
 * Created by florian on 18.12.14.
 */
public class TPCommand extends CommandAbstract {
	@Override public String getCommandName() {
		return "mwtp";
	}

	@Override public String getHelpLine(CommandSender commandSender) {
		return "/mvtp <player> <world>";
	}

	@Override public void handleCommand(CommandSender commandSender, String[] strings) throws di_BaseException {
		if(commandSender instanceof EntityPlayer){
			throw new dm_PlayerNotFoundException("this command is not for players");
		}
		if(strings.length != 2){
			throw new dm_PlayerNotFoundException("/mvtp <player> <world>");
		}
		MC_World world = getWorldByName(strings[1]);
		if(world == null){
			throw new dm_PlayerNotFoundException("unknown world: " + strings[1]);
		}
		EntityPlayer player = CommandAbstract.findPlayerTarget(commandSender, strings[0]);
		if(player == null){
			throw new dm_PlayerNotFoundException("unknown player: " + strings[0]);
		}
		new PlayerWrapper(player).teleport(world.getSpawnLocation());
	}

	private MC_World getWorldByName(String name){
		for(MC_World world: PluginMultiWorld.getInstance().getServer().getWorlds()){
			if(world.getName().equals(name))return world;
		}
		return null;
	}

	@Override public boolean hasPermissionToUse(CommandSender var1) {
		return !(var1 instanceof EntityPlayer) || super.hasPermissionToUse(var1);
	}
}
