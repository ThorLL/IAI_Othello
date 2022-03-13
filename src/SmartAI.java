public class SmartAI implements IOthelloAI{

    private int playerNumber;
    private int enemyNumber;

    private static final int maMulti = 6;     //Move advantage
    private static final int ddMulti = 4;     //Disc difference
    private static final int cMulti = 10;     //Corners

    private static final int MAXDEPTH = 7;
    public Position decideMove(GameState s){
        playerNumber = s.getPlayerInTurn();         // Player number for MAX
        enemyNumber = playerNumber == 1 ? 2 : 1;    // Player number for MIN
        Position move = alphaBetaSearch(s);         // Calls alphaBetaSearch to find best move at current game state

        return move == null ? new Position(-1, -1) : move;
    }

    private Position alphaBetaSearch(GameState s) {
        var past = System.nanoTime();         // Calculate time of search

        var vmp = MaxValue(s,Integer.MIN_VALUE,Integer.MAX_VALUE,0); // Runs algorithm

        System.out.println("" + (System.nanoTime()-past)/1000000);
        return vmp.move;    // returns best move found
    }

    private ValueMovePair MaxValue(GameState s,int a,int b,int depth) {
        // If max depth is hit or there are no longer any legal moves, then evaluates game state
        if (depth == MAXDEPTH || s.legalMoves().isEmpty()) return new ValueMovePair(eval(s),null);

        int v = Integer.MIN_VALUE;
        Position move = null;

        /*
            Loops through all possible legal moves at the current game state for max.
            At first, initializes a copy of the game state and afterwards takes actions on the current legal move.
            Then calls MinValue on the new game state.
            Returns the highest value (returned by MinValue).
         */
        for (Position pos : s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),playerNumber);
            game.insertToken(pos);
            int value = MinValue(game, a, b, depth + 1);
            if(value > v){
                v = value;
                move = pos;         // updates move to the best possible move (highest value).
                a = Math.max(a,v);  // updates alpha.
            }
            if(v >= b) break;       // performs a beta cut if v is greater or equal to beta.
        }
        return new ValueMovePair(v,move);
    }

    // Same as MaxValue except returns the lowest value and no move
    private int MinValue(GameState s,int a,int b,int depth){
        if (depth == MAXDEPTH) return eval(s);

        int v = Integer.MAX_VALUE;

        for (Position pos :s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),enemyNumber);
            game.insertToken(pos);
            int value = MaxValue(game,a,b,depth+1).value;
            if(value<v){
                v = value;
                b = Math.min(b,v);
            }
            if(v <= a) break;
        }
        return v;
    }

    // Evaluation function.
    private int eval(GameState current) {
        int ma = getMovesAdvantage(current);
        int dd = discDifference(current);
        int cc = cornerCount(current);
        return maMulti * ma + ddMulti * dd + cMulti * cc;
    }

    // Returns how many moves Max has compared to Min taken into account the total amount of moves.
    private int getMovesAdvantage(GameState current){
        if (current.getPlayerInTurn() == enemyNumber) current.changePlayer();
        int playerMoves = current.legalMoves().size();
        current.changePlayer();
        int enemyMoves = current.legalMoves().size();
        return 100 * (playerMoves - enemyMoves) / (enemyMoves + playerMoves + 1);
    }

    // Returns how many disc Max has compared to Min taken into account the total amount of discs.
    private int discDifference(GameState current){
        int playerDiscs = current.countTokens()[playerNumber-1];
        int enemyDiscs = current.countTokens()[enemyNumber-1];
        return 100 * (playerDiscs - enemyDiscs) / (enemyDiscs + playerDiscs);
    }

    // Returns how many corners Max has compared to Min taken into account the total amount of corners.
    private int cornerCount(GameState s){
        int tl = s.getBoard()[0][0];
        int tr = s.getBoard()[0][s.getBoard().length-1];
        int bl = s.getBoard()[s.getBoard().length-1][0];
        int br = s.getBoard()[s.getBoard().length-1][s.getBoard().length-1];

        var myCorners = getPlayersCorners(tl, tr, bl, br, playerNumber);
        var opCorners = getPlayersCorners(tl, tr, bl, br, enemyNumber);

        return 100 * (myCorners - opCorners) / (myCorners + opCorners + 1);
    }

    // Counts player's corners.
    private int getPlayersCorners(int topLeft, int topRight, int bottomLeft, int bottomRight, int playerNumber) {
        int cornerCount = 0;
        if(topLeft == playerNumber) {cornerCount ++;}
        if(topRight == playerNumber) {cornerCount ++;}
        if(bottomLeft == playerNumber) {cornerCount ++;}
        if(bottomRight == playerNumber) {cornerCount ++;}
        return cornerCount;
    }

    // A simple tuple that contains (value, move) pairs
    private class ValueMovePair{
        int value;
        Position move;
        public ValueMovePair(int value, Position move){
            this.value = value;
            this.move = move;
        }
    }
}
