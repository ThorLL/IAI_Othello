/**
 * A simple OthelloAI-implementation. The method to decide the next move just
 * returns the first legal move that it finds.
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public class SmartAI implements IOthelloAI{

    private int playerNumber;
    private int enemyNumber;

    private static final double maMulti = 2;    //Move advantage
    private static final double ddMulti = 1;   //Disc difference
    private static final double cMulti = 1000;     //Corners

    private static final int MAXDEPTH = 10;
    public Position decideMove(GameState s){
        playerNumber = s.getPlayerInTurn();
        if (playerNumber == 1){
            enemyNumber = 2;
        }else {
            enemyNumber = 1;
        }

        Position move = alphaBetaSearch(s);
        if( move == null){
            return new Position(-1,-1);
        }
        return move;
    }

    private Position alphaBetaSearch(GameState s){
        var past = System.nanoTime();
        ValueMovePair vmp = MaxValue(s,Integer.MIN_VALUE,Integer.MAX_VALUE,0);
        System.out.println("Time to get move: " + (System.nanoTime()-past)/1000000);
        return vmp.move;
    }

    private ValueMovePair MaxValue(GameState s,int a,int b,int depth){
        if (depth == MAXDEPTH || s.legalMoves().isEmpty()){
            return new ValueMovePair(eval(s),null);
        }
        int v = Integer.MIN_VALUE;
        Position move = null;

        for (Position pos : s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),playerNumber);
            game.insertToken(pos);
            ValueMovePair vmp = MinValue(game,a,b,depth+1);
            vmp.move = pos;
            if(vmp.value>v){
                v = vmp.value;
                move = vmp.move;
                a = Math.max(a,v);
            }
            if(v >= b){
                return new ValueMovePair(v,pos);
            }
        }
        return new ValueMovePair(v,move);
    }

    private ValueMovePair MinValue(GameState s,int a,int b,int depth){
        if (depth == MAXDEPTH){
            return new ValueMovePair(eval(s),null);
        }
        int v = Integer.MAX_VALUE;
        Position move = null;
        for (Position pos :s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),enemyNumber);
            game.insertToken(pos);
            ValueMovePair vmp = MaxValue(game,a,b,depth+1);
            vmp.move = pos;
            if(vmp.value<v){
                v = vmp.value;
                move = vmp.move;
                b = Math.min(b,v);
            }
            if(v <= a){
                return new ValueMovePair(v,pos);
            }

        }
        return new ValueMovePair(v,move);
    }

    private int eval(GameState current){
        double moveAdvantage = getMovesAdvantage(current);
        double discDifference = discDifference(current);
        double corners = cornerCount(current);

        return (int)( maMulti * moveAdvantage + ddMulti * discDifference + cMulti * corners);
    }

    private double getMovesAdvantage(GameState current){
        if (current.getPlayerInTurn() == enemyNumber) current.changePlayer();
        double playerMoves = current.legalMoves().size();
        current.changePlayer();
        double  enemyMoves = current.legalMoves().size();
        current.changePlayer();
        return (playerMoves - enemyMoves) / (enemyMoves + playerMoves + 1);
    }

    private double discDifference(GameState current){
        double playerDiscs = current.countTokens()[playerNumber-1];
        double enemyDiscs = current.countTokens()[enemyNumber-1];
        return (playerDiscs - enemyDiscs) / (enemyDiscs + playerDiscs);
    }

    private double cornerCount(GameState s){
        int tl = s.getBoard()[0][0];
        int tr = s.getBoard()[0][s.getBoard().length-1];
        int bl = s.getBoard()[s.getBoard().length-1][0];
        int br = s.getBoard()[s.getBoard().length-1][s.getBoard().length-1];

        var myCorners = 0.0;
        var opCorners = 0.0;
        if(tl == playerNumber) {myCorners ++;}
        if(tr == playerNumber) {myCorners ++;}
        if(bl == playerNumber) {myCorners ++;}
        if(br == playerNumber) {myCorners ++;}

        if(tl == enemyNumber) {opCorners ++;}
        if(tr == enemyNumber) {opCorners ++;}
        if(bl == enemyNumber) {opCorners ++;}
        if(br == enemyNumber) {opCorners ++;}

        return (myCorners - opCorners) / (myCorners + opCorners + 1);
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
