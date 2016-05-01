package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.WorldConfiguration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.demo.DemoWorldManager;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerList.class, priority = 900)
public abstract class MixinPlayerList {

    @Shadow
    @Final
    private MinecraftServer mcServer;

    @Shadow
    @Final
    private List<EntityPlayerMP> playerEntityList;

    @Shadow
    @Final
    private Map<UUID, EntityPlayerMP> uuidToPlayerMap;

    @Shadow
    public abstract void func_187243_f(EntityPlayerMP var1);

    @Shadow
    public abstract void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4);

    @Shadow
    public abstract void preparePlayer(EntityPlayerMP var1, WorldServer var2);

    @Shadow
    public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP var1, WorldServer var2);

    @Shadow
    public abstract void syncPlayerInventory(EntityPlayerMP var1);

    @Shadow
    private void setPlayerGameTypeBasedOnOther(EntityPlayerMP var1, EntityPlayerMP var2, World var3) {}

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "net.minecraft.world.DimensionType.getId()I"))
    private int getDimension(DimensionType dimensionType, NetworkManager var1, EntityPlayerMP var2) {
        if (var2.dimension > 1) {
            Environment environment = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(var2.dimension).getEnvironment();
            switch (environment) {
                case OVERWORLD:
                    return 0;
                case NETHER:
                    return -1;
                case END:
                    return 1;
            }
        }
        return dimensionType.getId();
    }


    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP oldPlayer, int newWorldId, boolean force) {
        int oldClientDimension = getDimensionByEnvironment(oldPlayer.dimension);
        oldPlayer.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(oldPlayer);
        oldPlayer.getServerForPlayer().getEntityTracker().untrackEntity(oldPlayer);
        oldPlayer.getServerForPlayer().getPlayerChunkManager().removePlayer(oldPlayer);
        this.playerEntityList.remove(oldPlayer);
        this.mcServer.worldServerForDimension(oldPlayer.dimension).removePlayerEntityDangerously(oldPlayer);
        BlockPos bedLocation = oldPlayer.getBedLocation();
        boolean var5 = oldPlayer.isSpawnForced();
        oldPlayer.dimension = newWorldId;
        Object interactManager;
        if(this.mcServer.isDemo()) {
            interactManager = new DemoWorldManager(this.mcServer.worldServerForDimension(oldPlayer.dimension));
        } else {
            interactManager = new PlayerInteractionManager(this.mcServer.worldServerForDimension(oldPlayer.dimension));
        }

        EntityPlayerMP newPlayer = new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(oldPlayer.dimension), oldPlayer.getGameProfile(), (PlayerInteractionManager)interactManager);
        newPlayer.dimension = oldPlayer.dimension;
        newPlayer.playerNetServerHandler = oldPlayer.playerNetServerHandler;
        newPlayer.clonePlayer(oldPlayer, force);
        newPlayer.setEntityId(oldPlayer.getEntityId());
        newPlayer.setCommandStats(oldPlayer);
        newPlayer.func_184819_a(oldPlayer.getPrimaryHand());
        Iterator var8 = oldPlayer.getTags().iterator();

        while(var8.hasNext()) {
            String var9 = (String)var8.next();
            newPlayer.addTag(var9);
        }

        WorldServer var10 = this.mcServer.worldServerForDimension(oldPlayer.dimension);
        this.setPlayerGameTypeBasedOnOther(newPlayer, oldPlayer, var10);
        BlockPos var11;
        if(bedLocation != null) {
            var11 = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(oldPlayer.dimension), bedLocation, var5);
            if(var11 != null) {
                newPlayer.setLocationAndAngles((double)((float)var11.getX() + 0.5F), (double)((float)var11.getY() + 0.1F), (double)((float)var11.getZ() + 0.5F), 0.0F, 0.0F);
                newPlayer.setSpawnPoint(bedLocation, var5);
            } else {
                newPlayer.playerNetServerHandler.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
        }

        var10.getChunkProvider().func_186025_d((int)newPlayer.posX >> 4, (int)newPlayer.posZ >> 4);

        while(!var10.func_184144_a(newPlayer, newPlayer.getEntityBoundingBox()).isEmpty() && newPlayer.posY < 256.0D) {
            newPlayer.setPosition(newPlayer.posX, newPlayer.posY + 1.0D, newPlayer.posZ);
        }

        int newClientDimension = getDimensionByEnvironment(newPlayer.dimension);
        if (oldClientDimension == newClientDimension) {
            newPlayer.playerNetServerHandler.sendPacket(new SPacketRespawn((byte) (newClientDimension >= 0 ? -1 : 0), newPlayer.worldObj.getDifficulty(), newPlayer.worldObj.getWorldInfo().getTerrainType(), newPlayer.theItemInWorldManager.getGameType()));
        }
        newPlayer.playerNetServerHandler.sendPacket(new SPacketRespawn(newClientDimension, newPlayer.worldObj.getDifficulty(), newPlayer.worldObj.getWorldInfo().getTerrainType(), newPlayer.theItemInWorldManager.getGameType()));
        var11 = var10.getSpawnPoint();
        newPlayer.playerNetServerHandler.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ, newPlayer.rotationYaw, newPlayer.rotationPitch);
        newPlayer.playerNetServerHandler.sendPacket(new SPacketSpawnPosition(var11));
        newPlayer.playerNetServerHandler.sendPacket(new SPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));
        this.updateTimeAndWeatherForPlayer(newPlayer, var10);
        this.func_187243_f(newPlayer);
        var10.getPlayerChunkManager().addPlayer(newPlayer);
        var10.spawnEntityInWorld(newPlayer);
        this.playerEntityList.add(newPlayer);
        this.uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
        newPlayer.addSelfToInternalCraftingInventory();
        newPlayer.setHealth(newPlayer.getHealth());
        return newPlayer;
    }

    private int getDimensionByEnvironment(int worldId) {
        if (worldId > 1) {
            WorldConfiguration customConfig = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(worldId);
            if (customConfig.getEnvironment() == Environment.NETHER) {
                return -1;
            } else if (customConfig.getEnvironment() == Environment.END) {
                return 1;
            } else {
                return 0;
            }
        }
        return worldId;
    }

    @Redirect(method = "transferEntityToWorld", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "net.minecraft.entity.Entity.dimension:I"))
    private int getDimension(Entity entity) {
        if (entity.dimension > 1) {
            WorldConfiguration customConfig = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(entity.dimension);
            if (customConfig.getGenerationType() == GenerationType.NETHER) {
                return -1;
            } else if (customConfig.getGenerationType() == GenerationType.END) {
                return 1;
            } else {
                return 0;
            }
        }
        return entity.dimension;
    }

    @ModifyVariable(method = "transferEntityToWorld", at = @At("HEAD"), argsOnly = true)
    private int fix(int d) {
        if (d > 1) {
            WorldConfiguration customConfig = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(d);
            if (customConfig.getGenerationType() == GenerationType.NETHER) {
                return -1;
            } else if (customConfig.getGenerationType() == GenerationType.END) {
                return 1;
            } else {
                return 0;
            }
        }
        return d;
    }

    @Overwrite
    public void func_187242_a(EntityPlayerMP var1, int var2) {
        int var3 = var1.dimension;
        WorldServer var4 = this.mcServer.worldServerForDimension(var1.dimension);
        var1.dimension = var2;
        WorldServer var5 = this.mcServer.worldServerForDimension(var1.dimension);
        int packetDimen = getDimensionByEnvironment(var2);
        int oldDimension = getDimensionByEnvironment(var3);
        if (oldDimension == packetDimen) {
            var1.playerNetServerHandler.sendPacket(new SPacketRespawn((byte) (packetDimen >= 0 ? -1 : 0), var1.worldObj.getDifficulty(), var1.worldObj.getWorldInfo().getTerrainType(), var1.theItemInWorldManager.getGameType()));
        }
        var1.playerNetServerHandler.sendPacket(new SPacketRespawn(packetDimen, var1.worldObj.getDifficulty(), var1.worldObj.getWorldInfo().getTerrainType(), var1.theItemInWorldManager.getGameType()));
        this.func_187243_f(var1);
        var4.removePlayerEntityDangerously(var1);
        var1.isDead = false;
        this.transferEntityToWorld(var1, var3, var4, var5);
        this.preparePlayer(var1, var4);
        var1.playerNetServerHandler.setPlayerLocation(var1.posX, var1.posY, var1.posZ, var1.rotationYaw, var1.rotationPitch);
        var1.theItemInWorldManager.setWorld(var5);
        var1.playerNetServerHandler.sendPacket(new SPacketPlayerAbilities(var1.capabilities));
        this.updateTimeAndWeatherForPlayer(var1, var5);
        this.syncPlayerInventory(var1);
        Iterator var6 = var1.getActivePotionEffects().iterator();

        while (var6.hasNext()) {
            PotionEffect var7 = (PotionEffect) var6.next();
            var1.playerNetServerHandler.sendPacket(new SPacketEntityEffect(var1.getEntityId(), var7));
        }
    }
}
