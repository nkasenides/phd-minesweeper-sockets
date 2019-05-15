package response;

import java.text.SimpleDateFormat;

public class ResponseDateTimeFormat extends SimpleDateFormat {

    ResponseDateTimeFormat() {
        super("yyyy-MM-dd HH:mm:ss");
    }

    public static ResponseDateTimeFormat STANDARD = new ResponseDateTimeFormat();

}
