package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import joebkt.*;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

/**
 * Created by florian on 10.01.15.
 */
@Mixin(joebkt.CmdWeather.class)
public abstract class FixCmdWeather extends CommandAbstract {

    @Overwrite
    public void handleCommand(CommandSender var1, String[] var2) throws di_BaseException {
        if(var2.length >= 1 && var2.length <= 2) {
            int var3 = (300 + (new Random()).nextInt(600)) * 20;
            if(var2.length >= 2) {
                var3 = safeParseIntMinMax(var2[1], 1, 1000000) * 20;
            }

            WorldServer var4 = MinecraftServer.getServer().worldServers[0];
            if(var1 instanceof EntityPlayer){
                var4 = MinecraftServer.getServer().getWorldServerByDimension(((EntityPlayer) var1).dimension);
            }
            WorldData var5 = var4.getWorldData();
            if("clear".equalsIgnoreCase(var2[0])) {
                var5.setClearWeatherTime(var3);
                var5.setRainDuration(0);
                var5.setThunderDuration(0);
                var5.setRaining(false);
                var5.setThundering(false);
                a(var1, this, "commands.weather.clear", new Object[0]);
            } else if("rain".equalsIgnoreCase(var2[0])) {
                var5.setClearWeatherTime(0);
                var5.setRainDuration(var3);
                var5.setThunderDuration(var3);
                var5.setRaining(true);
                var5.setThundering(false);
                a(var1, this, "commands.weather.rain", new Object[0]);
            } else {
                if(!"thunder".equalsIgnoreCase(var2[0])) {
                    throw new dp("commands.weather.usage", new Object[0]);
                }

                var5.setClearWeatherTime(0);
                var5.setRainDuration(var3);
                var5.setThunderDuration(var3);
                var5.setRaining(true);
                var5.setThundering(true);
                a(var1, this, "commands.weather.thunder", new Object[0]);
            }

        } else {
            throw new dp("commands.weather.usage", new Object[0]);
        }
    }
}
