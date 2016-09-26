package codecrafter47.multiworld.mixins;

import PluginReference.MC_World;
import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.projectrainbow._DiwUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 900)
public abstract class MixinEntity {
    @Shadow
    public World worldObj;
    @Shadow
    public boolean isDead;
    @Shadow
    public int dimension;
    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    @Shadow
    public float rotationYaw;

    @Shadow
    public abstract void setLocationAndAngles(double var1, double var3, double var5, float var7, float var8);

    @Inject(method = "<init>", at = @At("RETURN"))
    void onInit(World w, CallbackInfo ci) {
        if (worldObj != null) {
            this.dimension = ((MC_World) worldObj).getDimension();
        }
    }

    @Overwrite
    public Entity changeDimension(int var1) {
        if (!this.worldObj.isRemote && !this.isDead) {
            this.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer var2 = _DiwUtils.getMinecraftServer();
            int var3 = this.dimension;
            WorldServer var4 = var2.worldServerForDimension(var3);
            WorldServer var5 = var2.worldServerForDimension(var1);
            if (this.worldObj instanceof CustomWorldServer) {
                if (var1 == 1) {
                    int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
                    if (endPortalTarget < -1) {
                        return (Entity) (Object) this;
                    }
                    var5 = var2.worldServerForDimension(endPortalTarget);
                    this.dimension = endPortalTarget;
                } else {
                    int netherPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
                    if (netherPortalTarget < -1) {
                        return (Entity) (Object) this;
                    }
                    var5 = var2.worldServerForDimension(netherPortalTarget);
                    this.dimension = netherPortalTarget;
                }
            } else {
                this.dimension = var1;
                if (var3 == 1 && var1 == 1) {
                    var5 = var2.worldServerForDimension(0);
                    this.dimension = 0;
                }
            }

            this.worldObj.removeEntity((Entity) (Object) this);
            this.isDead = false;
            this.worldObj.theProfiler.startSection("reposition");
            BlockPos var6;
            if (var1 == 1) {
                var6 = var5.getSpawnCoordinate();
            } else {
                double var7 = this.posX;
                double var9 = this.posZ;
                double var11 = 8.0D;
                if (var1 == -1) {
                    var7 = MathHelper.clamp_double(var7 / var11, var5.getWorldBorder().minX() + 16.0D, var5.getWorldBorder().maxX() - 16.0D);
                    var9 = MathHelper.clamp_double(var9 / var11, var5.getWorldBorder().minZ() + 16.0D, var5.getWorldBorder().maxZ() - 16.0D);
                } else if (var1 == 0) {
                    var7 = MathHelper.clamp_double(var7 * var11, var5.getWorldBorder().minX() + 16.0D, var5.getWorldBorder().maxX() - 16.0D);
                    var9 = MathHelper.clamp_double(var9 * var11, var5.getWorldBorder().minZ() + 16.0D, var5.getWorldBorder().maxZ() - 16.0D);
                }

                var7 = (double) MathHelper.clamp_int((int) var7, -29999872, 29999872);
                var9 = (double) MathHelper.clamp_int((int) var9, -29999872, 29999872);
                float var13 = this.rotationYaw;
                this.setLocationAndAngles(var7, this.posY, var9, 90.0F, 0.0F);
                Teleporter var14 = var5.getDefaultTeleporter();
                var14.placeInExistingPortal((Entity) (Object) this, var13);
                var6 = new BlockPos((Entity) (Object) this);
            }

            var4.updateEntityWithOptionalForce((Entity) (Object) this, false);
            this.worldObj.theProfiler.endStartSection("reloading");
            Entity var16 = EntityList.createEntityByName(EntityList.getEntityString((Entity) (Object) this), var5);
            if (var16 != null) {
                NBTTagCompound nbt = new NBTTagCompound();
                ((Entity) (Object) this).writeToNBT(nbt);
                nbt.removeTag("Dimension");
                var16.writeToNBT(nbt); // todo wrong name
                var16.timeUntilPortal = ((Entity) (Object) this).timeUntilPortal;
                if (var4.provider.getDimensionType().getId() == 1 && var1 == 1) {
                    BlockPos var8 = var5.getTopSolidOrLiquidBlock(var5.getSpawnPoint());
                    var16.moveToBlockPosAndAngles(var8, var16.rotationYaw, var16.rotationPitch);
                } else {
                    var16.moveToBlockPosAndAngles(var6, var16.rotationYaw, var16.rotationPitch);
                }

                boolean var15 = var16.forceSpawn;
                var16.forceSpawn = true;
                var5.spawnEntityInWorld(var16);
                var16.forceSpawn = var15;
                var5.updateEntityWithOptionalForce(var16, false);
            }

            this.isDead = true;
            this.worldObj.theProfiler.endSection();
            var4.resetUpdateEntityTick();
            var5.resetUpdateEntityTick();
            this.worldObj.theProfiler.endSection();
            return var16;
        } else {
            return null;
        }
    }
}
