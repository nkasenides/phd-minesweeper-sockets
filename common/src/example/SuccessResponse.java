package example;

public class SuccessResponse extends Response {

    String message;

    public SuccessResponse(Status status, String title) {
        super(status, title);
    }

    public String getMessage() {
        return message;
    }
}
