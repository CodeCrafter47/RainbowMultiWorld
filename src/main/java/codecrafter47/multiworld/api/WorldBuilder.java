package codecrafter47.multiworld.api;

import PluginReference.MC_GameMode;
import PluginReference.MC_WorldBiomeType;
import PluginReference.MC_WorldLevelType;

public interface WorldBuilder {

    WorldBuilder setSeed(long seed);

    WorldBuilder setGenerationType(GenerationType generationType);

    WorldBuilder setEnvironment(Environment environment);

    WorldBuilder setLevelType(MC_WorldLevelType levelType);

    WorldBuilder setBiomeType(MC_WorldBiomeType biomeType);

    WorldBuilder setGenerateStructures(boolean generateStructures);

    WorldBuilder setGameMode(MC_GameMode gameMode);

    CustomWorld load();
}
