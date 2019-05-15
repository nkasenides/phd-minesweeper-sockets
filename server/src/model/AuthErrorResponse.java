package model;

import respondx.Response;
import respondx.ResponseStatus;

public class AuthErrorResponse extends Response {

    public AuthErrorResponse() {
        super(ResponseStatus.ERROR, "Authentication error", "The credentials you provided are not valid.");
    }

}
