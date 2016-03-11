package joebkt;


import PluginReference.MC_WorldSettings;
import org.projectrainbow._DiwUtils;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;


public class _WorldRegistration implements Serializable {

    public int dimension;
    public String name;
    public long msCreated;
    public int internal_loadedWorldIdx = 0;
    public MC_WorldSettings settings;
    public ConcurrentHashMap<String, String> extensibleProperties = new ConcurrentHashMap();

    public _WorldRegistration(String argName, int argDimension, MC_WorldSettings argSettings, long argMSCreated) {
        this.name = argName;
        this.dimension = argDimension;
        this.msCreated = argMSCreated;
        this.settings = argSettings;
    }

    public String toString() {
        return String.format(
                "World \'%s\', Dimension \'%d\', Props: %d, Created: %s %s",
                new Object[] {
            this.name, Integer.valueOf(this.dimension),
            Integer.valueOf(this.extensibleProperties.size()),
            _DiwUtils.GetDateStringFromLong(this.msCreated),
                        _DiwUtils.GetTimeStringFromLong(this.msCreated)});
    }
}
