package codecrafter47.multiworld.api;

import PluginReference.MC_GameMode;
import PluginReference.MC_Location;
import PluginReference.MC_World;

/**
 * Represents a custom world
 */
public interface CustomWorld {
    GenerationType getGenerationType();

    String getWorldGeneratorOptions();

    boolean isKeepSpawnInMemory();

    MC_GameMode getGameMode();

    void setKeepSpawnInMemory(boolean keepSpawnInMemory);

    void setGameMode(MC_GameMode gameMode);

    Environment getEnvironment();

    void setSpawnLocation(MC_Location location);

    MC_World getRespawnWorld();

    void setRespawnWorld(MC_World world);

    MC_World getNetherPortalTarget();

    void setNetherPortalTarget(MC_World world);

    MC_World getEndPortalTarget(MC_World world);

    void setEndPortalTarget(MC_World world);

    MC_World asMCWorld();

    boolean deleteOnRestart();
}
