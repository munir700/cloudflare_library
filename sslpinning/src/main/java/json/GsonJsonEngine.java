package json;

import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;

public class GsonJsonEngine extends JsonEngine {

    private static final JsonProvider jsonProvider = new GsonJsonProvider(new GsonBuilder().disableHtmlEscaping().create());

    @Override
    public JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    @Override
    public Object parse(String string) {
        return jsonProvider.parse(string);
    }
}
