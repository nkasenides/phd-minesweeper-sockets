package example;

import model.PartialBoardState;

public class PartialStateResponse extends Response {

    PartialBoardState partialBoardState;

    public PartialStateResponse(String title, PartialBoardState partialBoardState) {
        super(Status.OK, title);
        this.partialBoardState = partialBoardState;
    }

    public PartialBoardState getPartialBoardState() {
        return partialBoardState;
    }
}
