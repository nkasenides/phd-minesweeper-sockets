package response;

public enum ResponseStatus {

    OK("ok"),
    ERROR("error"),
    WARNING("warning")

    ;

    private String name;

    ResponseStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ResponseStatus fromString(String text) {
        if (text.equals(OK.name)) return OK;
        if (text.equals(WARNING.name)) return WARNING;
        if (text.equals(ERROR.name)) return ERROR;
        return null;
    }

}
