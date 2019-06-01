package solvers;

import clients.PlayerClient;
import com.sun.istack.internal.NotNull;
import model.Command;
import model.PartialBoardState;

public interface Solver {

    public abstract Command solve(@NotNull PartialBoardState partialBoardState, PlayerClient playerClient);

}
