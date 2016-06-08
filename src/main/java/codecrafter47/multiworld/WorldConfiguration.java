package codecrafter47.multiworld;

import codecrafter47.multiworld.api.Environment;
import codecrafter47.multiworld.api.GenerationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;

/**
 * Created by florian on 23.11.14.
 */
public class WorldConfiguration {
	GenerationType generationType = GenerationType.SINGLE_BIOME;
	boolean spawnMonsters = true;
	boolean spawnAnimals = true;
	String worldGeneratorOptions = "";
	boolean loadOnStartup = true;
	boolean keepSpawnInMemory = true;
    GameType gameMode = GameType.SURVIVAL;
    EnumDifficulty difficulty = EnumDifficulty.NORMAL;
	Environment environment = Environment.OVERWORLD;
	BlockPos spawn = null;
	int respawnWorld = 0;
	int netherPortalTarget = -2;
	int endPortalTarget = -2;

    public WorldConfiguration() {
    }

    public GenerationType getGenerationType() {
        return this.generationType;
    }

    public boolean isSpawnMonsters() {
        return this.spawnMonsters;
    }

    public boolean isSpawnAnimals() {
        return this.spawnAnimals;
    }

    public String getWorldGeneratorOptions() {
        return this.worldGeneratorOptions;
    }

    public boolean isLoadOnStartup() {
        return this.loadOnStartup;
    }

    public boolean isKeepSpawnInMemory() {
        return this.keepSpawnInMemory;
    }

    public GameType getGameMode() {
        return this.gameMode;
    }

    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }

    public Environment getEnvironment() {
        if (getGenerationType() == GenerationType.NETHER) {
            return Environment.NETHER;
        } else if (getGenerationType() == GenerationType.END) {
            return Environment.END;
        }
        return this.environment;
    }

    public BlockPos getSpawn() {
        return this.spawn;
    }

    public int getRespawnWorld() {
        return this.respawnWorld;
    }

    public int getNetherPortalTarget() {
        return this.netherPortalTarget;
    }

    public int getEndPortalTarget() {
        return this.endPortalTarget;
    }

    public void setGenerationType(GenerationType generationType) {
        this.generationType = generationType;
    }

    public void setSpawnMonsters(boolean spawnMonsters) {
        this.spawnMonsters = spawnMonsters;
    }

    public void setSpawnAnimals(boolean spawnAnimals) {
        this.spawnAnimals = spawnAnimals;
    }

    public void setWorldGeneratorOptions(String worldGeneratorOptions) {
        this.worldGeneratorOptions = worldGeneratorOptions;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public void setKeepSpawnInMemory(boolean keepSpawnInMemory) {
        this.keepSpawnInMemory = keepSpawnInMemory;
    }

    public void setGameMode(GameType gameMode) {
        this.gameMode = gameMode;
    }

    public void setDifficulty(EnumDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setSpawn(BlockPos spawn) {
        this.spawn = spawn;
    }

    public void setRespawnWorld(int respawnWorld) {
        this.respawnWorld = respawnWorld;
    }

    public void setNetherPortalTarget(int netherPortalTarget) {
        this.netherPortalTarget = netherPortalTarget;
    }

    public void setEndPortalTarget(int endPortalTarget) {
        this.endPortalTarget = endPortalTarget;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof WorldConfiguration)) return false;
        final WorldConfiguration other = (WorldConfiguration) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$generationType = this.generationType;
        final Object other$generationType = other.generationType;
        if (this$generationType == null ? other$generationType != null : !this$generationType.equals(other$generationType))
            return false;
        if (this.spawnMonsters != other.spawnMonsters) return false;
        if (this.spawnAnimals != other.spawnAnimals) return false;
        final Object this$worldGeneratorOptions = this.worldGeneratorOptions;
        final Object other$worldGeneratorOptions = other.worldGeneratorOptions;
        if (this$worldGeneratorOptions == null ? other$worldGeneratorOptions != null : !this$worldGeneratorOptions.equals(other$worldGeneratorOptions))
            return false;
        if (this.loadOnStartup != other.loadOnStartup) return false;
        if (this.keepSpawnInMemory != other.keepSpawnInMemory) return false;
        final Object this$gameMode = this.gameMode;
        final Object other$gameMode = other.gameMode;
        if (this$gameMode == null ? other$gameMode != null : !this$gameMode.equals(other$gameMode))
            return false;
        final Object this$difficulty = this.difficulty;
        final Object other$difficulty = other.difficulty;
        if (this$difficulty == null ? other$difficulty != null : !this$difficulty.equals(other$difficulty))
            return false;
        final Object this$environment = this.environment;
        final Object other$environment = other.environment;
        if (this$environment == null ? other$environment != null : !this$environment.equals(other$environment))
            return false;
        final Object this$spawn = this.spawn;
        final Object other$spawn = other.spawn;
        if (this$spawn == null ? other$spawn != null : !this$spawn.equals(other$spawn))
            return false;
        if (this.respawnWorld != other.respawnWorld) return false;
        if (this.netherPortalTarget != other.netherPortalTarget) return false;
        if (this.endPortalTarget != other.endPortalTarget) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $generationType = this.generationType;
        result = result * PRIME + ($generationType == null ? 0 : $generationType.hashCode());
        result = result * PRIME + (this.spawnMonsters ? 79 : 97);
        result = result * PRIME + (this.spawnAnimals ? 79 : 97);
        final Object $worldGeneratorOptions = this.worldGeneratorOptions;
        result = result * PRIME + ($worldGeneratorOptions == null ? 0 : $worldGeneratorOptions.hashCode());
        result = result * PRIME + (this.loadOnStartup ? 79 : 97);
        result = result * PRIME + (this.keepSpawnInMemory ? 79 : 97);
        final Object $gameMode = this.gameMode;
        result = result * PRIME + ($gameMode == null ? 0 : $gameMode.hashCode());
        final Object $difficulty = this.difficulty;
        result = result * PRIME + ($difficulty == null ? 0 : $difficulty.hashCode());
        final Object $environment = this.environment;
        result = result * PRIME + ($environment == null ? 0 : $environment.hashCode());
        final Object $spawn = this.spawn;
        result = result * PRIME + ($spawn == null ? 0 : $spawn.hashCode());
        result = result * PRIME + this.respawnWorld;
        result = result * PRIME + this.netherPortalTarget;
        result = result * PRIME + this.endPortalTarget;
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorldConfiguration;
    }

    public String toString() {
        return "codecrafter47.multiworld.WorldConfiguration(generationType=" + this.generationType + ", spawnMonsters=" + this.spawnMonsters + ", spawnAnimals=" + this.spawnAnimals + ", worldGeneratorOptions=" + this.worldGeneratorOptions + ", loadOnStartup=" + this.loadOnStartup + ", keepSpawnInMemory=" + this.keepSpawnInMemory + ", gameMode=" + this.gameMode + ", difficulty=" + this.difficulty + ", environment=" + this.environment + ", spawn=" + this.spawn + ", respawnWorld=" + this.respawnWorld + ", netherPortalTarget=" + this.netherPortalTarget + ", endPortalTarget=" + this.endPortalTarget + ")";
    }
}
