package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import PluginReference.MC_EventInfo;
import PluginReference.MC_Location;
import PluginReference.PluginInfo;
import WrapperObjects.Entities.EntityWrapper;
import WrapperObjects.Entities.PlayerWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import joebkt.*;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;

@Mixin(WrapperObjects.Entities.PlayerWrapper.class)
public class FixCrossDimensionTeleport extends EntityWrapper {

    @Shadow
    public EntityPlayer plr;

    public FixCrossDimensionTeleport(EntityGeneric argEnt) {
        super(argEnt);
    }

    @Overwrite
    @SneakyThrows
    public void teleport(MC_Location loc) {
        if (loc != null) {
            plr.setVehicle(null);
            _SerializableLocation sloc = new _SerializableLocation(loc.x, loc.y, loc.z, loc.dimension, loc.yaw, loc.pitch);
            if (loc.dimension != this.plr.dimension) {
                int newDimen = loc.dimension;
                MC_EventInfo ei = new MC_EventInfo();
                PlayerWrapper pWrap = new PlayerWrapper(this.plr);

                for (Object var2 : _JoeUtils.plugins) {
                    try {
                        ((PluginInfo) var2).ref.onAttemptPlayerChangeDimension(pWrap, newDimen, ei);
                    } catch (Throwable var8) {
                        var8.printStackTrace();
                    }
                }

                if (_JoeUtils.DebugMode) {
                    String var21 = String.format("JKC DEBUG: --- DimensionChange (%s) to --- (%s) --- %s @ %s", new Object[]{_JoeUtils.GetDimensionName(plr.dimension), _JoeUtils.GetDimensionName(newDimen), plr.getName(), _JoeUtils.GetEntityLocationDescription(plr)});
                    System.out.println(var21);
                }

                WorldServer localWorldFrom = MinecraftServer.getServer().getWorldServerByDimension(plr.dimension);
                localWorldFrom.getEntityTracker().removePlayer(plr);
                localWorldFrom.getPlayerChunkMap().removePlayer(plr);
                MinecraftServer.getServer().getThePlayerList().players.remove(plr);
                plr.worldEnt.removeEntity(plr);
                plr.dimension = newDimen;
                WorldServer localWorldTo = MinecraftServer.getServer().getWorldServerByDimension(plr.dimension);
                plr.setEntityWorld(localWorldTo);
                plr.dead = false;
                plr.setPositionRotation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
                localWorldTo.cachedChunks.getOrCreateChunk((int) plr.xCoord >> 4, (int) plr.zCoord >> 4);
                while (!localWorldTo.getNearbyEntities(plr, plr.funcAppliesIfCanBePushed()).isEmpty() && plr.yCoord < 256.0D) {
                    plr.setPositionRotation(plr.xCoord, plr.yCoord + 1.0D, plr.zCoord, plr.yaw, plr.pitch);
                }
                plr.dead = false;
                int packetDimen = plr.dimension;
                if (packetDimen > 1) {
                    Environment i = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(packetDimen).getEnvironment();
                    GenerationType generationType = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(packetDimen).getGenerationType();
                    if (generationType == GenerationType.END) {
                        packetDimen = 1;
                    } else if (generationType == GenerationType.NETHER || i == codecrafter47.multiworld.api.Environment.NETHER) {
                        packetDimen = -1;
                    } else if (i == codecrafter47.multiworld.api.Environment.END) {
                        packetDimen = 1;
                    } else {
                        packetDimen = 0;
                    }
                }
                plr.plrConnection.sendPacket(new Packet_Respawn(packetDimen, plr.worldEnt.getDifficulty(), plr.worldEnt.getWorldData().getLevelType(), plr.playerInteractManager.getGameMode()));
                plr.plrConnection.sendPacket(new Packet_SetExperience(plr.m_exp, plr.xpTotal, plr.xpLevel));
                plr.setSneaking(false);
                MinecraftServer.getServer().getThePlayerList().sendPlayerPacketsAboutNewDimension(plr, localWorldTo);
                localWorldTo.getPlayerChunkMap().addPlayer(plr);
                localWorldTo.addEntity(plr);
                MinecraftServer.getServer().getThePlayerList().players.add(plr);
                plr.playerInteractManager.setInnerWorld(localWorldTo);
                plr.setHealth(plr.getHealth());
                plr.updateInventory(plr.defaultContainer);
            } else {
                this.plr.TeleportSLoc(sloc);
            }
        }
    }

}
