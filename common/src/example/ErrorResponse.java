package example;

public class ErrorResponse extends Response {

    String error;

    public ErrorResponse(String title, String error) {
        super(Status.ERROR, title);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
