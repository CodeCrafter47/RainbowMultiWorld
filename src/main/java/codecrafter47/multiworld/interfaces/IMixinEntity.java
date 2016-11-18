package codecrafter47.multiworld.interfaces;

import net.minecraft.entity.Entity;

public interface IMixinEntity {

    void copyDataFromOldPublic(Entity other);
}
