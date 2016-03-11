package codecrafter47.multiworld.mixins;

import net.minecraft.command.CommandWeather;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandWeather.class)
public class MixinCommandWeather {

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "net.minecraft.world.World.getWorldInfo()Lnet/minecraft/world/storage/WorldInfo;"))
    private WorldInfo fix(World world0, MinecraftServer var1, ICommandSender var2, String[] var3) {
        return var1.getEntityWorld().getWorldInfo();
    }
}
