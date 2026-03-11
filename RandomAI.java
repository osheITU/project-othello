import java.util.List;
import java.util.Random;

/**
 * A simple OthelloAI-implementation. The method to decide the
 * next move just returns a random legal move that it finds. 
 * @author Oliver Flinck Sheye (oshe@itu.dk)
 * @version 10.3.2026
 */
public class RandomAI implements IOthelloAI {

    /**
	 * @return A random legal move.
	 */
    @Override
    public Position decideMove(GameState s) {
        List<Position> legalMoves = s.legalMoves();
        if (legalMoves.isEmpty()) return new Position(-1, -1);;

        int idx = new Random().nextInt(legalMoves.size());
        return legalMoves.get(idx);
    }
    
}
