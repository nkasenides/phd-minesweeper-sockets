package example;

import com.google.gson.Gson;
import model.FullBoardState;
import model.PartialBoardState;

public class Test {

    public static void main(String[] args) throws Exception {

        SuccessResponse successResponse = new SuccessResponse(Response.Status.OK, "hello");

        ErrorResponse errorResponse = new ErrorResponse("error1", "my error");

        Gson gson = new Gson();

        String a = gson.toJson(successResponse);
        System.out.println("a: " + a);

        PartialBoardState partialBoardState = new PartialBoardState(10, 10, 0, 0, new FullBoardState(10, 10));

        PartialStateResponse partialStateResponse = new PartialStateResponse("test11", partialBoardState);
        String s = gson.toJson(partialStateResponse);
        System.out.println("s: " + s);
        PartialStateResponse partialStateResponse1 = gson.fromJson(s, PartialStateResponse.class);
        System.out.println("regenerated: " + partialStateResponse1);
    }
}
