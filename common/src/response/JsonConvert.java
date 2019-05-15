package response;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonConvert {

    public static JsonArray listToJsonArray(List<?> list) {

        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(list, new TypeToken<List<?>>() {}.getType());

        if (! element.isJsonArray()) {
            throw new JsonSyntaxException("Could not find a valid JSON array in list.");
        }

        return element.getAsJsonArray();
    }

}
