package codecrafter47.multiworld.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.EnumDifficulty;

import java.io.IOException;

public class DifficultyTypeAdapter extends TypeAdapter<EnumDifficulty> {
    @Override
    public void write(JsonWriter jsonWriter, EnumDifficulty enumDifficulty) throws IOException {
        jsonWriter.value(enumDifficulty.toString());
    }

    @Override
    public EnumDifficulty read(JsonReader jsonReader) throws IOException {
        String s = jsonReader.nextString();
        return EnumDifficulty.valueOf(s);
    }
}
