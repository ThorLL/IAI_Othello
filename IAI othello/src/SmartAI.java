/**
 * A simple OthelloAI-implementation. The method to decide the next move just
 * returns the first legal move that it finds.
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public class SmartAI implements IOthelloAI{

    private int playerNumber;
    private int enemyNumber;

    private static final double maMulti = 1;
    private static final double nMulti = 1;
    private static final double ddMulti = 0.7;
    private static final double cMulti = 3;

    private static final int MAXDEPTH = 8;
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
        ValueMovePair vmp = MaxValue(s,s,Integer.MIN_VALUE,Integer.MAX_VALUE,0);
        System.out.println("Time to get move: " + (System.nanoTime()-past)/1000000);
        return vmp.move;
    }

    private ValueMovePair MaxValue(GameState s,GameState lastState,int a,int b,int depth){
        if (depth == MAXDEPTH || s.legalMoves().isEmpty()){
            return new ValueMovePair(eval(s,lastState),null);
        }
        int v = Integer.MIN_VALUE;
        Position move = null;

        for (Position pos : s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),s.getPlayerInTurn());
            game.insertToken(pos);
            ValueMovePair vmp = MinValue(game,s,a,b,depth+1);
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
        if(move == null){
            System.out.println("max:" + depth);
            System.out.println(s.legalMoves());
        }
        return new ValueMovePair(v,move);
    }

    private ValueMovePair MinValue(GameState s,GameState lastState,int a,int b,int depth){
        if (depth == MAXDEPTH){
            return new ValueMovePair(eval(s,lastState),null);
        }
        int v = Integer.MAX_VALUE;
        Position move = null;
        for (Position pos :s.legalMoves()) {
            GameState game = new GameState(s.getBoard(),s.getPlayerInTurn());
            game.insertToken(pos);
            ValueMovePair vmp = MaxValue(game,s,a,b,depth+1);
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
        if(move == null){System.out.println("min:" + depth);
            System.out.println(s.legalMoves());}
        return new ValueMovePair(v,move);
    }

    private int eval(GameState current,GameState last){
        float moveAdvantage = getMovesAdvantage(current);
        int discDifference = discDifferenceFromLastState(current,last);
        int corners = cornerCount(current);
        int n = current.countTokens()[playerNumber-1];


        return (int) (maMulti * moveAdvantage * (nMulti * n + ddMulti * discDifference + cMulti * corners));
    }

    private float getMovesAdvantage(GameState current){
        int blackMoves = current.legalMoves().size();
        current.changePlayer();
        int whiteMoves = current.legalMoves().size();
        return ((float) blackMoves) / (blackMoves + whiteMoves);
    }

    private int discDifferenceFromLastState(GameState current, GameState last){
        int blackDisc = current.countTokens()[playerNumber-1];
        int whiteDisc = current.countTokens()[playerNumber-1];
        int boardSize = (int) Math.pow(current.getBoard().length,2);
        int lastBlack = last.countTokens()[playerNumber-1];
        int diff = blackDisc - lastBlack;
        if((blackDisc + whiteDisc)<boardSize/2){
            diff = diff * -1;
        }
        return diff;
    }

    private int cornerCount(GameState s){
        int tl = s.getBoard()[0][0];
        int tr = s.getBoard()[0][s.getBoard().length-1];
        int bl = s.getBoard()[s.getBoard().length-1][0];
        int br = s.getBoard()[s.getBoard().length-1][s.getBoard().length-1];

        int count = 0;
        if(tl == playerNumber) {count ++;}
        if(tr == playerNumber) {count ++;}
        if(bl == playerNumber) {count ++;}
        if(br == playerNumber) {count ++;}
        return count;
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
