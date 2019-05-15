package response;

import com.google.gson.JsonObject;

public class WarningResponse extends Response {

    public WarningResponse(String title, String message, JsonObject data) {
        super(ResponseStatus.WARNING, title, message, data);
    }

    public WarningResponse(String title, String message) {
        super(ResponseStatus.WARNING, title, message, null);
    }
}
