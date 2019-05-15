package response;

import com.google.gson.JsonObject;

public class AuthErrorResponse extends Response {

    public AuthErrorResponse() {
        super(ResponseStatus.ERROR, "Authentication error", "The credentials you provided are not valid.");
    }

}
