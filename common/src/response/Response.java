package response;

import com.google.gson.JsonObject;

import java.util.Date;

public class Response {

    public static final String STATUS_TAG = "status";
    public static final String TITLE_TAG = "title";
    public static final String MESSAGE_TAG = "message";
    public static final String DATETIME_TAG = "datetime";
    public static final String DATA_TAG = "data";

    private ResponseStatus status;
    private String title;
    private String message;
    private Date date;
    private JsonObject data;

    public Response(ResponseStatus status, String title, String message, JsonObject data) {
        this.status = status;
        this.title = title;
        this.message = message;
        date = new Date();
        this.data = data;
    }

    public Response(ResponseStatus status, String title, String message) {
        this(status, title, message, null);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public JsonObject getData() {
        return data;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public String toJSON() {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty(STATUS_TAG, status.toString());
        responseJson.addProperty(TITLE_TAG, title);
        responseJson.addProperty(MESSAGE_TAG, message);
        responseJson.addProperty(DATETIME_TAG, ResponseDateTimeFormat.STANDARD.format(date));
        if (data != null) {
            responseJson.add(DATA_TAG, data);
        }
        return responseJson.toString();
    }

}
