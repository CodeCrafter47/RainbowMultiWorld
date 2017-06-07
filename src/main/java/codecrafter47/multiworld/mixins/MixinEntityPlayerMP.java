package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerMP.class, priority = 900)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements ICommandSender {

    public MixinEntityPlayerMP(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void onChangeDimension1(int targetDimension, CallbackInfoReturnable<Entity> ci) {
        if (this.world instanceof CustomWorldServer) {
            if (targetDimension == 1) {
                int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
                if (endPortalTarget < -1) {
                    ci.setReturnValue((Entity) (Object) this);
                }
            } else {
                int netherPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
                if (netherPortalTarget < -1) {
                    ci.setReturnValue((Entity) (Object) this);
                }
            }
        }
    }

    @Redirect(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimension:I", opcode = Opcodes.GETFIELD))
    private int onChangeDimension2(Entity entity) {
        return entity.world.provider.getDimensionType().getId();
    }

    @ModifyArg(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;changePlayerDimension(Lnet/minecraft/entity/player/EntityPlayerMP;I)V"))
    private int onChangeDimension3(int targetDimension) {
        if (this.world instanceof CustomWorldServer) {
            if (targetDimension == 1) {
                return PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
            } else {
                return PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
            }
        }
        return targetDimension;
    }
}
