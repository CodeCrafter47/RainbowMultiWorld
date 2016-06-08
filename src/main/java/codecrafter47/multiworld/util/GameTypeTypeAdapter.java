package codecrafter47.multiworld.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.GameType;

import java.io.IOException;

public class GameTypeTypeAdapter extends TypeAdapter<GameType> {
    @Override
    public void write(JsonWriter jsonWriter, GameType gameType) throws IOException {
        jsonWriter.value(gameType.toString());
    }

    @Override
    public GameType read(JsonReader jsonReader) throws IOException {
        String s = jsonReader.nextString();
        return GameType.valueOf(s);
    }
}
