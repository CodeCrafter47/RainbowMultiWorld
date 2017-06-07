package codecrafter47.multiworld.mixins;

import PluginReference.MC_World;
import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 900)
public abstract class MixinEntity {
    @Shadow
    public World world;
    @Shadow
    public int dimension;


    @Inject(method = "<init>", at = @At("RETURN"))
    void onInit(World w, CallbackInfo ci) {
        if (world != null) {
            this.dimension = ((MC_World) world).getDimension();
        }
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

    @Redirect(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimension:I", opcode = Opcodes.GETFIELD), expect = 1)
    private int onChangeDimension2(Entity entity) {
        return entity.world.provider.getDimensionType().getId();
    }

    @Redirect(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimension:I", opcode = Opcodes.PUTFIELD), expect = 2)
    private void onChangeDimension3(Entity entity, int value) {
        // ignore
    }

    @Redirect(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;worldServerForDimension(I)Lnet/minecraft/world/WorldServer;", ordinal = 0), expect = 1)
    private WorldServer onChangeDimension4(MinecraftServer server, int dimension, int targetDimension) {
        return server.worldServerForDimension(this.dimension);
    }

    @Redirect(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;worldServerForDimension(I)Lnet/minecraft/world/WorldServer;", ordinal = 1), expect = 1)
    private WorldServer onChangeDimension5(MinecraftServer server, int dimension, int targetDimension) {
        if (this.world instanceof CustomWorldServer) {
            if (targetDimension == 1) {
                int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
                this.dimension = endPortalTarget;
            } else {
                int netherPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
                this.dimension = netherPortalTarget;
            }
        } else {
            this.dimension = targetDimension;
        }
        return server.worldServerForDimension(this.dimension);
    }
}
