package codecrafter47.multiworld.commands;

import PluginReference.MC_Player;
import PluginReference.MC_World;
import codecrafter47.multiworld.PluginMultiWorld;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 18.12.14.
 */
public class TPCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "mwtp";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/mvtp <player> <world>";
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender commandSender, String[] strings) throws CommandException {
        if (commandSender instanceof EntityPlayer) {
            throw new WrongUsageException("this command is not for players");
        }
        if (strings.length != 2) {
            throw new WrongUsageException("/mvtp <player> <world>");
        }
        MC_World world = getWorldByName(strings[1]);
        if (world == null) {
            throw new WrongUsageException("unknown world: " + strings[1]);
        }
        EntityPlayer player = CommandBase.getPlayer(minecraftServer, commandSender, strings[0]);
        ((MC_Player) player).teleport(world.getSpawnLocation());
    }

    private MC_World getWorldByName(String name) {
        for (MC_World world : PluginMultiWorld.getInstance().getServer().getWorlds()) {
            if (world.getName().equals(name)) return world;
        }
        return null;
    }

    @Override
    public boolean checkPermission(MinecraftServer minecraftServer, ICommandSender iCommandSender) {
        return !(iCommandSender instanceof EntityPlayer) || super.checkPermission(minecraftServer, iCommandSender);
    }
}
