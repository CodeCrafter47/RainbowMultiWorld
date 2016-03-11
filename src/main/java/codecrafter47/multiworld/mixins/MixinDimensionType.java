package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldProvider;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.GenerationType;
import codecrafter47.multiworld.api.WorldConfiguration;
import codecrafter47.multiworld.manager.StorageManager;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(DimensionType.class)
public class MixinDimensionType {
    private static int worldId;

    @Shadow
    @Final
    private Class<? extends WorldProvider> clazz;
    @Shadow
    public static DimensionType[] values() {
        return null;
    }

    @Overwrite
    public static DimensionType getById(int var0) {
        worldId = var0;

        if (var0 > 1) {
            StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
            WorldConfiguration customConfig = storageManager.getCustomConfig(worldId);
            GenerationType generationType = customConfig.getGenerationType();

            if (generationType == GenerationType.NETHER) {
                var0 = -1;
            } else if (generationType == GenerationType.END) {
                var0 = 1;
            } else {
                var0 = 0;
            }
        }

        DimensionType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            DimensionType var4 = var1[var3];
            if(var4.getId() == var0) {
                return var4;
            }
        }

        throw new IllegalArgumentException("Invalid dimension id " + var0);
    }

    @Overwrite
    public WorldProvider createDimension() {
        if (worldId > 1) {
            StorageManager storageManager = PluginMultiWorld.getInstance().getStorageManager();
            WorldConfiguration customConfig = storageManager.getCustomConfig(worldId);
            GenerationType generationType = customConfig.getGenerationType();

            if (generationType == GenerationType.OVERWORLD || generationType == GenerationType.SINGLE_BIOME) {
                return new CustomWorldProvider(worldId);
            }
        }
        try {
            Constructor var1 = this.clazz.getConstructor(new Class[0]);
            return (WorldProvider)var1.newInstance(new Object[0]);
        } catch (NoSuchMethodException var2) {
            throw new Error("Could not create new dimension", var2);
        } catch (InvocationTargetException var3) {
            throw new Error("Could not create new dimension", var3);
        } catch (InstantiationException var4) {
            throw new Error("Could not create new dimension", var4);
        } catch (IllegalAccessException var5) {
            throw new Error("Could not create new dimension", var5);
        }
    }
}
