package codecrafter47.multiworld.mixins;

import codecrafter47.multiworld.CustomWorldServer;
import codecrafter47.multiworld.PluginMultiWorld;
import codecrafter47.multiworld.ChatPlayer;
import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityPlayerMP.class, priority = 900)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements ICommandSender, ChatPlayer {

    @Shadow
    private boolean invulnerableDimensionChange;
    @Shadow
    public boolean playerConqueredTheEnd;
    @Shadow
    public NetHandlerPlayServer connection;
    @Shadow
    @Final
    public MinecraftServer mcServer;
    @Shadow
    private int lastExperience = -99999999;
    @Shadow
    private float lastHealth = -1.0E8F;
    @Shadow
    private int lastFoodLevel = -99999999;

    public MixinEntityPlayerMP(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        addChatMessage(TextComponentString.Serializer.jsonToComponent(ComponentSerializer.toString(components)));
    }

    @Overwrite
    public Entity changeDimension(int var1) {
        if (this.world instanceof CustomWorldServer) {
            if (var1 == 1) {
                int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
                if (endPortalTarget < -1) {
                    return this;
                }
            } else {
                int netherPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
                if (netherPortalTarget < -1) {
                    return this;
                }
            }
        }
        this.invulnerableDimensionChange = true;
        if (this.world.provider.getDimensionType().getId() == 1 && var1 == 1) {
            this.world.removeEntity(this);
            if (!this.playerConqueredTheEnd) {
                this.playerConqueredTheEnd = true;
                if (this.hasAchievement(AchievementList.THE_END2)) {
                    this.connection.sendPacket(new SPacketChangeGameState(4, 0.0F));
                } else {
                    this.addStat(AchievementList.THE_END2);
                    this.connection.sendPacket(new SPacketChangeGameState(4, 1.0F));
                }
            }

            return this;
        } else {
            if (this.world.provider.getDimensionType().getId() == 0 && var1 == 1) {
                this.addStat(AchievementList.THE_END);
                var1 = 1;
            } else {
                this.addStat(AchievementList.PORTAL);
            }

            if (this.world instanceof CustomWorldServer) {
                if (var1 == 1) {
                    int endPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getEndPortalTarget();
                    if (endPortalTarget < -1) {
                        return this;
                    }
                    var1 = endPortalTarget;
                } else {
                    int netherPortalTarget = PluginMultiWorld.getInstance().getStorageManager().getCustomConfig(this.dimension).getNetherPortalTarget();
                    if (netherPortalTarget < -1) {
                        return this;
                    }
                    var1 = netherPortalTarget;
                }
            }

            this.mcServer.getPlayerList().changePlayerDimension((EntityPlayerMP) (Object) this, var1);
            this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
            return this;
        }
    }
}
