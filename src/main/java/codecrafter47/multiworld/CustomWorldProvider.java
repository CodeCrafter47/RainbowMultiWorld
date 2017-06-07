package codecrafter47.multiworld;

import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.manager.StorageManager;
import com.google.common.collect.BiMap;
import joebkt._WorldRegistration;
import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.projectrainbow.PluginHelper;

public class CustomWorldProvider extends WorldProvider {
    private final int worldId;

    public CustomWorldProvider(int worldId) {
        this.worldId = worldId;
    }

    @Override
    public DimensionType getDimensionType() {
        StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
        WorldConfiguration customConfig = storageManager.getCustomConfig(worldId);
        switch (customConfig.getEnvironment()) {
            case NETHER:
                //return DimensionType.NETHER;
            case END:
                //return DimensionType.THE_END;
            case OVERWORLD:
            default:
                return DimensionType.OVERWORLD;
        }
    }

    @Override
    protected void init() {
        this.hasSkyLight = true;
        WorldType var1 = this.world.getWorldInfo().getTerrainType();
        if(var1 == WorldType.FLAT) {
            FlatGeneratorInfo var2 = FlatGeneratorInfo.createFlatGeneratorFromString(this.world.getWorldInfo().getGeneratorOptions());
            this.biomeProvider = new BiomeProviderSingle(Biome.getBiome(var2.getBiome(), Biomes.DEFAULT));
        } else {
            GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.worldId).getGenerationType();
            PluginMultiWorld.getInstance().getLogger().info("Injecting generationType into " + this.worldId + ": " + generationType.name());
            if (generationType == GenerationType.SINGLE_BIOME) {
                _WorldRegistration entry1 = _WorldMaster.GetRegistrationFromDimension(this.worldId);
                if (entry1 != null) {
                    this.biomeProvider = new BiomeProviderSingle((Biome) ((BiMap<Object, Object>) (Object) PluginHelper.biomeMap).inverse().get(entry1.settings.biomeType));
                } else {
                    this.biomeProvider = new BiomeProvider(this.world.getWorldInfo());
                }
            } else if (generationType == GenerationType.OVERWORLD) {
                this.biomeProvider = new BiomeProvider(this.world.getWorldInfo());
            } else {
                PluginMultiWorld.getInstance().getLogger().info("ERROR: This should not happen.");
            }
        }
    }

    @Override
    public boolean hasNoSky() {
        Environment environment = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.worldId).getEnvironment();
        return environment == Environment.END;
    }

    @Override
    public boolean canDropChunk(int var1, int var2) {
        StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
        WorldConfiguration customConfig = storageManager.getCustomConfig(worldId);
        return !(customConfig.isKeepSpawnInMemory() && this.world.e(var1, var2)); // isSpawnChunk
    }
}
