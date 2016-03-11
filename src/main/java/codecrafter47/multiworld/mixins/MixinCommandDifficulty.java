package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.manager.StorageManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandDifficulty;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandDifficulty.class)
public abstract class MixinCommandDifficulty extends CommandBase {

    @Shadow
    protected abstract EnumDifficulty getDifficultyFromCommand(String var1) throws NumberInvalidException;

    @Overwrite
    public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
        if(var3.length <= 0) {
            throw new WrongUsageException("commands.difficulty.usage", new Object[0]);
        } else {
            EnumDifficulty var4 = this.getDifficultyFromCommand(var3[0]);
            World world = var2.getEntityWorld();
            world.getWorldInfo().setDifficulty(var4);

            if (world instanceof CustomWorldServer) {
                StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
                WorldConfiguration customConfig = storageManager.getCustomConfig(((CustomWorldServer) world).getWorldId());
                customConfig.setDifficulty(var4);
                storageManager.saveData();
            }

            notifyOperators(var2, this, "commands.difficulty.success", new Object[]{new TextComponentTranslation(var4.getDifficultyResourceKey(), new Object[0])});
        }
    }
}
