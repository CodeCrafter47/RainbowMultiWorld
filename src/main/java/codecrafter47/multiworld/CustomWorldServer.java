package codecrafter47.multiworld;

import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.manager.StorageManager;
import lombok.Getter;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

@Getter
public class CustomWorldServer extends WorldServer {
    private final int worldId;
    private static DataFixer dataFixer = DataFixesManager.func_188279_a();
    private static int staticId;

    public CustomWorldServer(MinecraftServer minecraftServer, ISaveHandler iSaveHandler, WorldInfo worldInfo, int worldId, Profiler profiler) {
        super(minecraftServer, iSaveHandler, worldInfo, staticId = worldId, profiler);
        this.worldId = worldId;
        worldScoreboard = minecraftServer.worldServerForDimension(0).getScoreboard();
    }

    public int getWorldId() {
        return this.worldId;
    }

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
        IChunkLoader var1 = new AnvilChunkLoader(file, dataFixer);
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
}
