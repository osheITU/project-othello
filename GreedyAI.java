import java.util.ArrayList;
import java.util.Random;

/**
 * OthelloAI-implementation. The method to decide the
 * next move returns a random entry from the set of highest-capturing legal moves.
 * @author Oliver Flinck Sheye (oshe@itu.dk)
 * @version 10.3.2026
 */
public class GreedyAI implements IOthelloAI {
    /**
     * Data Transfer Object representing a tuple containing a Move and how many tokens it would capture.
     */
    private record GreedDTO(Position move, int captures) {}

    /**
     * <h4>Shamelessly stolen from GameState (added GameState parameter).</h4>
     * Checks how many tokens of the opponent the player can capture in the direction given
     * by deltaX and deltaY if the player puts a token at the given position.
     * @param s A state of a game.
     * @param p A position on the board.
     * @param deltaX The step to be taken in the x-direction. Should be -1 (left), 0 (none), or 1 (right).
     * @param deltaY The step to be taken in the delta direction. Should be -1 (up), 0 (none), or 1 (down).
     */
    private int captureInDirection(GameState s, Position p, int deltaX, int deltaY) {
        int currentPlayer = s.getPlayerInTurn();
        int[][] board = s.getBoard();
        int size = board.length;

    	int opponent = (currentPlayer == 1 ? 2 : 1); 
        
    	int captured = 0;
    	int cc = p.col;
    	int rr = p.row;
        while (0 <= cc+deltaX && cc+deltaX < size && 0 <= rr+deltaY && rr+deltaY < size
                && board[cc+deltaX][rr+deltaY] == opponent) { 
        	cc = cc + deltaX;
        	rr = rr + deltaY;
        	captured++;
        }
        if (0 <= cc+deltaX && cc+deltaX < size && 0 <= rr+deltaY && rr+deltaY < size &&
                board[cc+deltaX][rr+deltaY] == currentPlayer && captured > 0 )
        	return captured;
        
        return 0;
    }

    /**
     * <h4>Reworked version of <code>GameState.legalMoves()</code>.</h4>
     * Sorts legal moves by amount of captures, then deletes entries with less than the highest amount.
     * @param s A state of a game.
     * @return The list of highest-capturing legal moves.
     */
    public ArrayList<Position> greedyMoves(GameState s) {
        int[][] board = s.getBoard();
        int size = board.length;

    	ArrayList<Position> posPlaces = new ArrayList<Position>();
    	for (int i = 0; i < size; i++) {
    		for (int j = 0; j < size; j++) {
    			if (board[i][j] == 0)
    				posPlaces.add(new Position(i,j));
    		}
    	}
    	ArrayList<GreedDTO> legalPlaces = new ArrayList<GreedDTO>();
    	for (Position p : posPlaces){
    		for (int deltaX = -1; deltaX <= 1; deltaX++){
    			for (int deltaY = -1; deltaY <= 1; deltaY++){
                    int captures = captureInDirection(s, p, deltaX, deltaY);
    				if (captures > 0){
    	    			legalPlaces.add(new GreedDTO(p, captures));
    				}
    			}
    		}
    	}
        legalPlaces.sort((a, b) -> Integer.compare(b.captures(), a.captures()));
        legalPlaces.removeIf(a -> a.captures() < legalPlaces.getFirst().captures());
        
        ArrayList<Position> greedOrderedMoves = new ArrayList<>();
        for (GreedDTO a : legalPlaces) greedOrderedMoves.add(a.move());

        return greedOrderedMoves;
    }	

    /**
     * @return A random move from the set of highest-capturing legal moves.
     */
    @Override
    public Position decideMove(GameState s) {
        ArrayList<Position> moves = greedyMoves(s);
        if (moves.isEmpty()) return new Position(-1, -1);

        int idx = new Random().nextInt(moves.size());
        return moves.get(idx);
    }
}
