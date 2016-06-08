package codecrafter47.multiworld.mixins;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandToggleDownfall;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CommandToggleDownfall.class)
public abstract class MixinCommandToggleDownfall extends CommandBase {

    @Overwrite
    public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
        WorldInfo wi = var2.getEntityWorld().getWorldInfo();
        wi.setRaining(!wi.isRaining());
        notifyCommandListener(var2, this, "commands.downfall.success");
    }
}
