package codecrafter47.multiworld.mixins;

import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandGameRule.class)
public class MixinCommandGameRule {

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "net.minecraft.command.CommandGameRule.getOverWorldGameRules(Lnet/minecraft/server/MinecraftServer;)Lnet/minecraft/world/GameRules;"))
    private GameRules fix(CommandGameRule self, MinecraftServer server, MinecraftServer var1, ICommandSender var2, String[] var3) {
        return var1.getEntityWorld().getGameRules();
    }
}
