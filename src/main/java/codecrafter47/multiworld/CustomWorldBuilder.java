package codecrafter47.multiworld;

import PluginReference.MC_GameMode;
import PluginReference.MC_WorldBiomeType;
import PluginReference.MC_WorldLevelType;
import PluginReference.MC_WorldSettings;
import codecrafter47.multiworld.api.CustomWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.api.WorldBuilder;
import codecrafter47.multiworld.commands.MainCommand;
import codecrafter47.multiworld.manager.StorageManager;
import com.google.common.base.Preconditions;
import net.minecraft.world.GameType;
import org.projectrainbow.PluginHelper;

import java.util.concurrent.ThreadLocalRandom;

public class CustomWorldBuilder implements WorldBuilder {
    private final String name;
    private long seed = ThreadLocalRandom.current().nextLong();
    private GenerationType generationType = GenerationType.OVERWORLD;
    private Environment environment = Environment.OVERWORLD;
    private MC_WorldLevelType levelType = MC_WorldLevelType.DEFAULT;
    private MC_WorldBiomeType biomeType = MC_WorldBiomeType.FOREST;
    private boolean generateStructures = true;
    private MC_GameMode gameMode = MC_GameMode.SURVIVAL;

    public CustomWorldBuilder(String name) {
        this.name = name;
        Preconditions.checkArgument(!_WorldMaster.mapDimensionToWorldName.values().contains(name) && !_WorldMaster.mapWorldNameToDimensionIdx.containsKey(name.toLowerCase()), "A world with the requested name already exists.");
    }

    @Override
    public WorldBuilder setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public WorldBuilder setGenerationType(GenerationType generationType) {
        this.generationType = generationType;
        return this;
    }

    @Override
    public WorldBuilder setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public WorldBuilder setLevelType(MC_WorldLevelType levelType) {
        this.levelType = levelType;
        return this;
    }

    @Override
    public WorldBuilder setBiomeType(MC_WorldBiomeType biomeType) {
        this.biomeType = biomeType;
        return this;
    }

    @Override
    public WorldBuilder setGenerateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    @Override
    public WorldBuilder setGameMode(MC_GameMode gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    @Override
    public CustomWorld load() {
        MC_WorldSettings mc_worldSettings = new MC_WorldSettings();
        mc_worldSettings.generateStructures = generateStructures;
        mc_worldSettings.seed = seed;
        mc_worldSettings.levelType = levelType;
        mc_worldSettings.biomeType = biomeType;
        int id = _WorldMaster.RegisterWorld(name, mc_worldSettings);
        StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
        WorldConfiguration customConfig = storageManager.getCustomConfig(id);
        customConfig.setGenerationType(generationType);
        customConfig.setEnvironment(environment);
        customConfig.setGameMode(((GameType) (Object) PluginHelper.gamemodeMap.inverse().get(gameMode)));
        storageManager.saveData();
        PluginMultiWorld.getInstance().getWorldManager().loadWorld(id);
        return (CustomWorld) MainCommand.getWorldByName(name);
    }
}
