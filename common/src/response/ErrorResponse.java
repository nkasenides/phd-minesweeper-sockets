package response;

import com.google.gson.JsonObject;

public class ErrorResponse extends Response {

    public ErrorResponse(String title, String message, JsonObject data) {
        super(ResponseStatus.ERROR, title, message, data);
    }

    public ErrorResponse(String title, String message) {
        super(ResponseStatus.ERROR, title, message, null);
    }
}
