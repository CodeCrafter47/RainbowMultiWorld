package codecrafter47.multiworld.mixins;

import MultiWorld.mixin.Mixin;
import MultiWorld.mixin.Overwrite;
import MultiWorld.mixin.Shadow;
import codecrafter47.multiworld.PluginMultiWorld;
import joebkt.DamageCause;
import joebkt.EntityGeneric;
import joebkt.World;
import joebkt.WorldServer;
import net.minecraft.server.MinecraftServer;

/**
 * Created by florian on 26.11.14.
 */
@Mixin(joebkt.EntityGeneric.class)
public abstract class LinkNetherPortals {

	@Shadow
	public World worldEnt;

	@Shadow
	public double lastX;

	@Shadow
	public double lastY;

	@Shadow
	public double lastZ;

	@Shadow
	public double xCoord;

	@Shadow
	public double yCoord;

	@Shadow
	public double zCoord;

	@Shadow
	public EntityGeneric vehicle;

	@Shadow
	public float M;

	@Shadow
	public float length;

	@Shadow
	public float A;

	@Shadow
	public float B;

	@Shadow
	public float yaw;

	@Shadow
	public float pitch;

	@Shadow
	protected boolean fShouldPortal;

	@Shadow
	protected int al;

	@Shadow
	public int portalCooldown;

	@Shadow
	protected boolean ab;

	@Shadow
	public int fireTicks;

	@Shadow
	protected boolean aa;

	@Shadow
	public float fallDistance;

	@Shadow
	public abstract int L();

	@Shadow
	abstract int getPortalCooldown();

	@Shadow
	public abstract void handleChangeDimension(int var1);

	@Shadow
	public abstract void Y();

	@Shadow
	public abstract boolean landInWaterMaybe();

	@Shadow
	public abstract boolean damageEntity(DamageCause var1, float var2);

	@Shadow
	public abstract boolean ab();

	@Shadow
	protected abstract void M();

	@Shadow
	protected abstract void killEntityWrapper();

	@Shadow
	protected abstract void b(int var1, boolean var2);

	@Overwrite
	public void entityBaseTickMethod() {
		this.worldEnt.methodProfiler.capture("entityBaseTick");
		if(this.vehicle != null && this.vehicle.dead) {
			this.vehicle = null;
		}

		this.length = this.M;
		this.lastX = this.xCoord;
		this.lastY = this.yCoord;
		this.lastZ = this.zCoord;
		this.B = this.pitch;
		this.A = this.yaw;
		if(!this.worldEnt.isStatic && this.worldEnt instanceof WorldServer) {
			WorldServer localWorldServer = (WorldServer)this.worldEnt;
			if(localWorldServer.dimensionSetAtCreate <= 1) {
				// legacy portal handling
				this.worldEnt.methodProfiler.capture("portal");
				MinecraftServer var1 = ((WorldServer)this.worldEnt).getServer();
				int var2 = this.L();
				if(this.fShouldPortal) {
					if(var1.getAllowNether()) {
						if(this.vehicle == null && this.al++ >= var2) {
							this.al = var2;
							this.portalCooldown = this.getPortalCooldown();
							byte var3;
							if(this.worldEnt.worldProvider.getDimenIdx() == -1) {
								var3 = 0;
							} else {
								var3 = -1;
							}

							this.handleChangeDimension(var3);
						}

						this.fShouldPortal = false;
					}
				} else {
					if(this.al > 0) {
						this.al -= 4;
					}

					if(this.al < 0) {
						this.al = 0;
					}
				}

				if(this.portalCooldown > 0) {
					--this.portalCooldown;
				}

				this.worldEnt.methodProfiler.checkIfTakingTooLong();
			} else {
				// portal handling in custom worlds
				this.worldEnt.methodProfiler.capture("portal");
				int var2 = this.L();
				if(this.fShouldPortal) {
					PluginMultiWorld.getInstance().getLogger().info("custom portal handling");
					PluginMultiWorld pluginMultiWorld = PluginMultiWorld.getInstance();
					int newDimension = pluginMultiWorld.getStorageManager().getCustomConfig(worldEnt.worldProvider.getDimenIdx()).getNetherPortalTarget();
					if(newDimension > -2 && pluginMultiWorld.getWorldManager().isLoaded(newDimension)) {
						PluginMultiWorld.getInstance().getLogger().info("from " + worldEnt.worldProvider.getDimenIdx() + " to " + newDimension);
						if(this.vehicle == null && this.al++ >= var2) {
							this.al = var2;
							this.portalCooldown = this.getPortalCooldown();
							this.handleChangeDimension(newDimension);
						}
						this.fShouldPortal = false;
					}
				} else {
					if(this.al > 0) {
						this.al -= 4;
					}

					if(this.al < 0) {
						this.al = 0;
					}
				}

				if(this.portalCooldown > 0) {
					--this.portalCooldown;
				}

				this.worldEnt.methodProfiler.checkIfTakingTooLong();
			}
		}

		this.Y();
		this.landInWaterMaybe();
		if(this.worldEnt.isStatic) {
			this.fireTicks = 0;
		} else if(this.fireTicks > 0) {
			if(this.ab) {
				this.fireTicks -= 4;
				if(this.fireTicks < 0) {
					this.fireTicks = 0;
				}
			} else {
				if(this.fireTicks % 20 == 0) {
					this.damageEntity(DamageCause.onFire, 1.0F);
				}
				--this.fireTicks;
			}
		}

		if(this.ab()) {
			this.M();
			this.fallDistance *= 0.5F;
		}

		if(this.yCoord < -64.0D) {
			this.killEntityWrapper();
		}

		if(!this.worldEnt.isStatic) {
			this.b(0, this.fireTicks > 0);
		}

		this.aa = false;
		this.worldEnt.methodProfiler.checkIfTakingTooLong();
	}
}
