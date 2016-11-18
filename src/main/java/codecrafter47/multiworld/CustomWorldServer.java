package codecrafter47.multiworld;

import PluginReference.MC_GameMode;
import PluginReference.MC_Location;
import PluginReference.MC_World;
import codecrafter47.multiworld.api.CustomWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.manager.StorageManager;
import lombok.Getter;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.projectrainbow.PluginHelper;
import org.projectrainbow._DiwUtils;

import java.io.File;

@Getter
public class CustomWorldServer extends WorldServer implements CustomWorld {
    private final int worldId;
    private static int staticId;

    private WorldConfiguration getWC() {
        return PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(worldId);
    }

    public CustomWorldServer(MinecraftServer minecraftServer, ISaveHandler iSaveHandler, WorldInfo worldInfo, int worldId, Profiler profiler) {
        super(minecraftServer, iSaveHandler, worldInfo, staticId = worldId, profiler);
        this.worldId = worldId;
        worldScoreboard = minecraftServer.worldServerForDimension(0).getScoreboard();
    }

    public int getWorldId() {
        return this.worldId;
    }

    /*
     * This method overrides a method injected into the super class via mixin. It must not be removed.
     */
    public int getClientDimension() {
        if (getDimension() > 1) {
            WorldConfiguration customConfig = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(worldId);
            if (customConfig.getEnvironment() == Environment.NETHER) {
                return -1;
            } else if (customConfig.getEnvironment() == Environment.END) {
                return 1;
            } else {
                return 0;
            }
        }
        return getDimension();
    }

    public int getDimension() {
        return worldId == 0 ? staticId : worldId;
    }

    public String getName() {
        return _WorldMaster.GetWorldNameFromDimension(worldId);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        File worldDirectory = this.saveHandler.getWorldDirectory();
        File file = new File(worldDirectory, "DIM_" + getDimension());
        IChunkLoader var1 = new AnvilChunkLoader(file, super.getMinecraftServer().getDataFixer());
        return new ChunkProviderServer(this, var1, this.provider.createChunkGenerator());
    }

    @Override
    public void setSpawnPoint(BlockPos blockPos) {
        super.setSpawnPoint(blockPos);
        StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
        WorldConfiguration customConfig = storageManager.getCustomConfig(worldId);
        customConfig.setSpawn(blockPos);
        storageManager.saveData();
    }

    @Override
    public GenerationType getGenerationType() {
        return getWC().getGenerationType();
    }

    @Override
    public String getWorldGeneratorOptions() {
        return getWC().getWorldGeneratorOptions();
    }

    @Override
    public boolean isKeepSpawnInMemory() {
        return getWC().isKeepSpawnInMemory();
    }

    @Override
    public MC_GameMode getGameMode() {
        return PluginHelper.gamemodeMap.get(getWC().getGameMode());
    }

    @Override
    public void setKeepSpawnInMemory(boolean keepSpawnInMemory) {
        getWC().setKeepSpawnInMemory(keepSpawnInMemory);
        PluginMultiWorld.getInstance().getStorageManager().saveData();
    }

    @Override
    public void setGameMode(MC_GameMode gameMode) {
        getWC().setGameMode((GameType) (Object) PluginHelper.gamemodeMap.inverse().get(gameMode));
        PluginMultiWorld.getInstance().getStorageManager().saveData();
        getWorldInfo().setGameType((GameType) (Object) PluginHelper.gamemodeMap.inverse().get(gameMode));
    }

    @Override
    public Environment getEnvironment() {
        return getWC().getEnvironment();
    }

    @Override
    public void setSpawnLocation(MC_Location location) {
        BlockPos spawn = new BlockPos(location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        getWC().setSpawn(spawn);
        PluginMultiWorld.getInstance().getStorageManager().saveData();
        setSpawnPoint(spawn);
    }

    @Override
    public MC_World getRespawnWorld() {
        return (MC_World) _DiwUtils.getMinecraftServer().worldServerForDimension(getWC().getRespawnWorld());
    }

    @Override
    public void setRespawnWorld(MC_World world) {
        if (world != null) {
            getWC().setRespawnWorld(world.getDimension());
            PluginMultiWorld.getInstance().getStorageManager().saveData();
        }
    }

    @Override
    public MC_World getNetherPortalTarget() {
        int i = getWC().getNetherPortalTarget();
        return i >= -1 ? (MC_World) _DiwUtils.getMinecraftServer().worldServerForDimension(i) : null;
    }

    @Override
    public void setNetherPortalTarget(MC_World world) {
        getWC().setNetherPortalTarget(world != null ? world.getDimension() : -2);
        PluginMultiWorld.getInstance().getStorageManager().saveData();
    }

    @Override
    public MC_World getEndPortalTarget(MC_World world) {
        int i = getWC().getEndPortalTarget();
        return i >= -1 ? (MC_World) _DiwUtils.getMinecraftServer().worldServerForDimension(i) : null;
    }

    @Override
    public void setEndPortalTarget(MC_World world) {
        getWC().setEndPortalTarget(world != null ? world.getDimension() : -2);
        PluginMultiWorld.getInstance().getStorageManager().saveData();
    }

    @Override
    public MC_World asMCWorld() {
        return (MC_World) this;
    }

    @Override
    public boolean deleteOnRestart() {
        return _WorldMaster.UnregisterWorld(_WorldMaster.GetWorldNameFromDimension(worldId));
    }
}
