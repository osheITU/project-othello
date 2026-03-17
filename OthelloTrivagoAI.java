import java.util.List;

/**
 * Our IOthelloAI-implementation. Jesus take the wheel.
 * @author Andreas John-Holaus (andjo@itu.dk)
 * @author Carl Christian David Dam (cacd@itu.dk)
 * @author Oliver Flinck Sheye (oshe@itu.dk)
 * @author Philip Bay Quorning (phqu@itu.dk)
 * @author Rasmus Tornvig Nordquist (ratn@itu.dk)
 * @version 11.3.2026
 */
public class OthelloTrivagoAI implements IOthelloAI {
    /**
     * Data Transfer Object representing a tuple of a Utility-value and a Move.
     */
    private record UtilMoveDTO(float util, Position move) {}
    private static final int MAX_DEPTH = 6;
    private static final int MOBILITY_THRESHOLD = 8;

    /**
     * TODO IMPLEMENT (book, p. 204)
     * Given a GameState, determine whether the current state is a cut-off point for the current search.
     * @param state A state of a game.
     * @return <code>true</code> if the current state is a cut-off point, <code>false</code> otherwise.
     */
    private boolean isCutoff(GameState state, int depth) {
        // Terminal state
        if (state.legalMoves().isEmpty()) {
            return true;
        }
        // Depth limit
        if (depth >= MAX_DEPTH) {
            return true;
        }
        // Heuristic adjustment
        int mobility = state.legalMoves().size();
        if (mobility > MOBILITY_THRESHOLD && depth >= MAX_DEPTH - 2) {
            return true;
        }

        return false;
    }

    /**
     * Determine a resulting state of when a given action is performed at a given state.
     * @param state A state of a game.
     * @param action An action to be performed at <code>state</code>.
     * @return The resulting state.
     */
    private GameState result(GameState state, Position action) {
        int[][] board = state.getBoard();
        int player = state.getPlayerInTurn();

        GameState newState = new GameState(board, player);
        newState.insertToken(action);

        return newState;
    }

    /**
     * TODO IMPLEMENT (book, p. 202)
     * Determine an estimate of the expected utility of a given state to a given player
     * @param state A state of a game.
     * @param player A player of the game. Value should be 1 (MAX/black) or 2 (MIN/white).
     * @return An estimate of the expected utility of <code>state</code> to <code>player</code>. 
     */
    private UtilMoveDTO eval(GameState state, int player) {
        int[] tokens = state.countTokens();
        // tokens[0] = black (player 1), tokens[1] = white (player 2)
        int my_discs  = (player == 1) ? tokens[0] : tokens[1];
        int opp_discs = (player == 1) ? tokens[1] : tokens[0];
        float disc_diff = (my_discs + opp_discs) > 0
                ? (float)(my_discs - opp_discs) / (my_discs + opp_discs)
                : 0f;

        int my_moves  = state.legalMoves().size();
        state.changePlayer();
        int opp_moves = state.legalMoves().size();
        state.changePlayer();
        float mobility = (my_moves + opp_moves) > 0
                ? (float)(my_moves - opp_moves) / (my_moves + opp_moves)
                : 0f;

        float game_value = 0.5f * disc_diff + 0.5f * mobility;
        return new UtilMoveDTO(game_value, null);
    }

    /** 
     * Search for the best available move for player 1 (MAX/black).
     * @param state A state of a game.
     * @param alpha The best estimated utility for player 1 (MAX/black) found so far.
     * @param beta The best estimated utility for player 2 (MIN/white) found so far.
     * @param depth The depth of the current search.
     * @return The estimated best move for player 1 (MAX/black).
     */
    private UtilMoveDTO maxValue(GameState state, float alpha, float beta, int depth) {
        if (isCutoff(state, depth)) return eval(state, 1);
        
        float v = Float.NEGATIVE_INFINITY;
        Position move = new Position(-1, -1);

        for (Position a : state.legalMoves()) {
            UtilMoveDTO dto = minValue(result(state, a), alpha, beta, depth + 1);
            float v2 = dto.util();
            // Position a2 = dto.move(); // Bliver ikke brugt 💔

            if (v2 > v) {
                v = v2;
                move = a;
                alpha = Math.max(alpha, v);
            }

            if (v >= beta) break;
        }

        return new UtilMoveDTO(v, move);
    }

    /** 
     * Search for the best available move for player 2 (MIN/white).
     * @param state A state of a game.
     * @param alpha The best estimated utility for player 1 (MAX/black) found so far.
     * @param beta The best estimated utility for player 2 (MIN/white) found so far.
     * @param depth The depth of the current search.
     * @return The estimated best move for player 2 (MIN/white).
     */
    private UtilMoveDTO minValue(GameState state, float alpha, float beta, int depth) {
        if (isCutoff(state, depth)) return eval(state, 1);

        float v = Float.NEGATIVE_INFINITY;
        Position move = new Position(-1, -1);

        for (Position a : state.legalMoves()) {
            UtilMoveDTO dto = maxValue(result(state, a), alpha, beta, depth + 1);
            float v2 = dto.util();
            // Position a2 = dto.move(); // Bliver heller aldrig brugt 😭

            if (v2 < v) {
                v = v2;
                move = a;
                beta = Math.min(beta, v);
            }

            if (v <= alpha) break;
        }

        return new UtilMoveDTO(v, move);
    }

    /**
     * Performs Alpha-Beta search (with cut-off and pruning) to
     * estimate the best possible move for the current player.
     * @return The estimated best possible move for <code>s</code>'s current player in turn.
     */
    @Override
    public Position decideMove(GameState s) {
        float negInf = Float.NEGATIVE_INFINITY;
        float posInf = Float.POSITIVE_INFINITY;

        int player = s.getPlayerInTurn();
        UtilMoveDTO dto = new UtilMoveDTO(0, new Position(-1, -1));
        
        switch (player) {
            case 1 -> dto = maxValue(s, negInf, posInf, 0);
            case 2 -> dto = minValue(s, negInf, posInf, 0);
            default -> throw new IllegalStateException(
                "\"currentPlayer\" evaluated to " + player +
                "; should be either 1 (MAX/black) or 2 (MIN/white)"
            );
        }
    
        return dto.move();
    }
}
