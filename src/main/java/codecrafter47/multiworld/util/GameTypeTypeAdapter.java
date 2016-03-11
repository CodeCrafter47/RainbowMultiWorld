package codecrafter47.multiworld.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

import java.io.IOException;

public class GameTypeTypeAdapter extends TypeAdapter<WorldSettings.GameType> {
    @Override
    public void write(JsonWriter jsonWriter, WorldSettings.GameType gameType) throws IOException {
        jsonWriter.value(gameType.toString());
    }

    @Override
    public WorldSettings.GameType read(JsonReader jsonReader) throws IOException {
        String s = jsonReader.nextString();
        return WorldSettings.GameType.valueOf(s);
    }
}
