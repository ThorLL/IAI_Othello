import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SmartAI implements IOthelloAI{

    private int playerNumber;
    private int enemyNumber;

    private ArrayList<Long> times = new ArrayList<>();

    private static final int maMulti = 200;     //Move advantage
    private static final int ddMulti = 1;       //Disc difference
    private static final int cMulti = 1000;     //Corners

    private static final int MAXDEPTH = 8;
    public Position decideMove(GameState s){
        playerNumber = s.getPlayerInTurn();
        enemyNumber = playerNumber == 1 ? 2 : 1;

        Position move = alphaBetaSearch(s);
        return move == null ? new Position(-1, -1) : move;
    }

    private Position alphaBetaSearch(GameState s) {
        var past = System.nanoTime();

        var vmp = MaxValue(s,Integer.MIN_VALUE,Integer.MAX_VALUE,0);

        var elapsed = (System.nanoTime()-past)/1000000;
        times.add(elapsed);
        var sum = 0L;
        for (var time : times) sum += time;
        System.out.println("(Smart AI : player " + playerNumber +") \n Time to get move: " + elapsed + "\n Average time to get move : " + sum / times.size()+"\n");
        return vmp.move;
    }

    private ValueMovePair MaxValue(GameState s,int a,int b,int depth) {
        if (depth == MAXDEPTH || s.legalMoves().isEmpty()) return new ValueMovePair(eval(s),null);

        int v = Integer.MIN_VALUE;
        Position move = null;

        for (Position pos : s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),playerNumber);
            game.insertToken(pos);
            int value = MinValue(game, a, b, depth + 1).value;
            if(value > v){
                v = value;
                move = pos;
                a = Math.max(a,v);
            }
            if(v >= b) break;
        }
        return new ValueMovePair(v,move);
    }

    private ValueMovePair MinValue(GameState s,int a,int b,int depth){
        if (depth == MAXDEPTH) return new ValueMovePair(eval(s),null);

        int v = Integer.MAX_VALUE;
        Position move = null;

        for (Position pos :s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),enemyNumber);
            game.insertToken(pos);
            int value = MaxValue(game,a,b,depth+1).value;
            if(value<v){
                v = value;
                move = pos;
                b = Math.min(b,v);
            }
            if(v <= a) break;
        }
        return new ValueMovePair(v,move);
    }

    private int eval(GameState current) {
        int ma = getMovesAdvantage(current);
        int dd = discDifference(current);
        int cc = cornerCount(current);
        return maMulti * ma + ddMulti * dd + cMulti * cc;
    }

    private int getMovesAdvantage(GameState current){
        if (current.getPlayerInTurn() == enemyNumber) current.changePlayer();
        int playerMoves = current.legalMoves().size();
        current.changePlayer();
        int enemyMoves = current.legalMoves().size();
        return 100 * (playerMoves - enemyMoves) / (enemyMoves + playerMoves + 1);
    }

    private int discDifference(GameState current){
        int playerDiscs = current.countTokens()[playerNumber-1];
        int enemyDiscs = current.countTokens()[enemyNumber-1];
        return 100 * (playerDiscs - enemyDiscs) / (enemyDiscs + playerDiscs);
    }

    private int cornerCount(GameState s){
        int tl = s.getBoard()[0][0];
        int tr = s.getBoard()[0][s.getBoard().length-1];
        int bl = s.getBoard()[s.getBoard().length-1][0];
        int br = s.getBoard()[s.getBoard().length-1][s.getBoard().length-1];

        var myCorners = getPlayersCorners(tl, tr, bl, br, playerNumber);
        var opCorners = getPlayersCorners(tl, tr, bl, br, enemyNumber);

        return 100 * (myCorners - opCorners) / (myCorners + opCorners + 1);
    }

    private int getPlayersCorners(int topLeft, int topRight, int bottomLeft, int bottomRight, int playerNumber) {
        int cornerCount = 0;
        if(topLeft == playerNumber) {cornerCount ++;}
        if(topRight == playerNumber) {cornerCount ++;}
        if(bottomLeft == playerNumber) {cornerCount ++;}
        if(bottomRight == playerNumber) {cornerCount ++;}
        return cornerCount;
    }

    private class ValueMovePair{
        int value;
        Position move;
        public ValueMovePair(int value, Position move){
            this.value = value;
            this.move = move;
        }
    }
}
