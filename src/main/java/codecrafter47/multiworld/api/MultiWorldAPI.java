package codecrafter47.multiworld.api;

import PluginReference.MC_World;
import PluginReference.RainbowUtils;
import com.google.common.base.Function;

import java.util.List;

/**
 * Use this class to access the MultiWorld plugin api.
 */
public class MultiWorldAPI {

    private static F wb = null;

    /**
     * Get a CustomWorld instance for a {@link MC_World} if the world is created
     * using the MultiWorld plugin.
     *
     * @param world the world
     * @return the world as {@link CustomWorld} or null
     */
    public static CustomWorld asCustomWorld(MC_World world) {
        return world instanceof CustomWorld ? ((CustomWorld) world) : null;
    }

    /**
     * Get a custom world by its name.
     *
     * @param name name of the world
     * @return the world or null
     */
    public static CustomWorld getCustomWorld(String name) {
        List<MC_World> worlds = RainbowUtils.getServer().getWorlds();
        for (MC_World world : worlds) {
            if (world.getName().equals(name)) {
                return asCustomWorld(world);
            }
        }
        return null;
    }

    /**
     * Create a custom world.
     *
     * @param name name of the new world
     * @return a {@link WorldBuilder} to create the world
     */
    public static WorldBuilder createWorld(String name) {
        return wb.apply(name);
    }

    public interface F {
        WorldBuilder apply(String name);
    }
}
