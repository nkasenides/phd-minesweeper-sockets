package example;

public class Response {

    enum Status { OK, ERROR }

    private Status status;
    private String title;

    public Response(Status status, String title) {
        this.status = status;
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
