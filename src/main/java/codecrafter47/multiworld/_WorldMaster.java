package codecrafter47.multiworld;


import PluginReference.ChatColor;
import PluginReference.MC_WorldSettings;
import joebkt._WorldRegistration;
import org.projectrainbow._DiwUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class _WorldMaster {

    public static ConcurrentHashMap<String, Integer> mapWorldNameToDimensionIdx = new ConcurrentHashMap();
    public static ConcurrentHashMap<Integer, String> mapDimensionToWorldName = new ConcurrentHashMap();
    public static Integer g_nextWorldIdx = Integer.valueOf(3);
    public static List<_WorldRegistration> worldRegs = new ArrayList();
    public static String DataFilename = "WorldRegistration.dat";

    static {
        LoadData();
    }

    public _WorldMaster() {}

    public static String GetWorldNameFromDimension(int dimenIdx) {
        String res = (String) mapDimensionToWorldName.get(
                Integer.valueOf(dimenIdx));

        return res != null ? res : "Dimension " + dimenIdx;
    }

    public static _WorldRegistration GetRegistrationFromDimension(int dimension) {
        Iterator var2 = worldRegs.iterator();

        while (var2.hasNext()) {
            _WorldRegistration entry = (_WorldRegistration) var2.next();

            if (entry.dimension == dimension) {
                return entry;
            }
        }

        return null;
    }

    public static void LoadData() {
        try {
            long entry = System.currentTimeMillis();
            File name = new File(_DiwUtils.RainbowDataDirectory + DataFilename);
            FileInputStream f = new FileInputStream(name);
            ObjectInputStream s = new ObjectInputStream(
                    new BufferedInputStream(f));

            worldRegs = (List) s.readObject();
            g_nextWorldIdx = (Integer) s.readObject();
            s.close();
            long msEnd = System.currentTimeMillis();
            String msg = String.format(
                    "%-20s: " + ChatColor.WHITE + "%5d worlds.   Took %3d ms",
                    new Object[] {
                "World Registrar",
                Integer.valueOf(worldRegs.size()), Long.valueOf(msEnd - entry)});

            _DiwUtils.ConsoleMsg(ChatColor.YELLOW + msg);
        } catch (Throwable var8) {
            _DiwUtils.ConsoleMsg("Starting new file: " + DataFilename);
            worldRegs = new ArrayList();
            g_nextWorldIdx = Integer.valueOf(3);
        }

        mapDimensionToWorldName.put(Integer.valueOf(0), "world");
        mapDimensionToWorldName.put(Integer.valueOf(-1), "world_nether");
        mapDimensionToWorldName.put(Integer.valueOf(1), "world_the_end");
        mapDimensionToWorldName.put(Integer.valueOf(2), "PlotWorld");
        Iterator var1 = worldRegs.iterator();

        while (var1.hasNext()) {
            _WorldRegistration entry1 = (_WorldRegistration) var1.next();
            String name1 = entry1.name.toLowerCase();

            mapWorldNameToDimensionIdx.put(name1,
                    Integer.valueOf(entry1.dimension));
            mapDimensionToWorldName.put(Integer.valueOf(entry1.dimension),
                    entry1.name);
        }

    }

    public static void SaveData() {
        try {
            long exc = System.currentTimeMillis();
            File file = new File(_DiwUtils.RainbowDataDirectory + DataFilename);
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(
                    new BufferedOutputStream(f));

            s.writeObject(worldRegs);
            s.writeObject(g_nextWorldIdx);
            s.close();
            long msEnd = System.currentTimeMillis();
            String msg = ChatColor.YELLOW
                    + String.format("%-20s: %5d worlds.      Took %3d ms",
                    new Object[] {
                "World Registrar",
                Integer.valueOf(worldRegs.size()), Long.valueOf(msEnd - exc)});

            _DiwUtils.ConsoleMsg(msg);
        } catch (Throwable var8) {
            var8.printStackTrace();
        }

    }

    public static int RegisterWorld(String worldName, MC_WorldSettings settings) {
        if (worldName != null && worldName.length() > 0) {
            String nameLower = worldName.toLowerCase();
            Integer curIdx = (Integer) mapWorldNameToDimensionIdx.get(nameLower);
            int dimen;
            _WorldRegistration info;

            if (curIdx == null) {
                Integer var10000 = g_nextWorldIdx;

                g_nextWorldIdx = Integer.valueOf(g_nextWorldIdx.intValue() + 1);
                dimen = var10000.intValue();
                mapWorldNameToDimensionIdx.put(nameLower, Integer.valueOf(dimen));
                mapDimensionToWorldName.put(Integer.valueOf(dimen), worldName);
                info = new _WorldRegistration(worldName, dimen, settings,
                        System.currentTimeMillis());
                worldRegs.add(info);
                SaveData();
                return dimen;
            } else {
                for (dimen = worldRegs.size() - 1; dimen >= 0; --dimen) {
                    info = (_WorldRegistration) worldRegs.get(dimen);
                    if (info.dimension == curIdx.intValue()) {
                        worldRegs.set(dimen,
                                new _WorldRegistration(worldName,
                                curIdx.intValue(), settings,
                                System.currentTimeMillis()));
                        break;
                    }
                }

                return curIdx.intValue();
            }
        } else {
            return -2;
        }
    }

    public static boolean UnregisterWorld(String worldName) {
        if (worldName != null && worldName.length() > 0) {
            String nameLower = worldName.toLowerCase();
            Integer curIdx = (Integer) mapWorldNameToDimensionIdx.get(nameLower);

            if (curIdx == null) {
                return false;
            } else {
                for (int i = worldRegs.size() - 1; i >= 0; --i) {
                    _WorldRegistration entry = (_WorldRegistration) worldRegs.get(
                            i);

                    if (entry.name.equalsIgnoreCase(worldName)) {
                        worldRegs.remove(i);
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }
}
