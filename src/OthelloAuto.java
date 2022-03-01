import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OthelloAuto {
    public OthelloAuto(int size, String ai1,String ai2){
        long start = System.nanoTime();

        System.out.println("Generating value sets");
        ArrayList<ValueSet> valueSets = generateValueSet(0,10,0,10,0,30);
        System.out.println("Value sets generated");
        var done = false;

        var counter = 1;
        while (!done){
            System.out.println("Generating tasks set" + counter);
            ArrayList<Game> tasks = generateTasks(valueSets,size,ai1,ai2);
            System.out.println("Task set " + counter + " generated");

            System.out.println("Simulating game set " + counter);
            valueSets = simulate(tasks);
            System.out.println("Finished " + counter +". simulation");
            counter++;
            if(valueSets.size() < 2) done = true;
        }
        System.out.println("Time : " + (System.nanoTime()-start)/1_000_000 + "\nDone best sets = ");
        for (ValueSet vs : valueSets){
            System.out.printf("\n ma : %s  dd:%s  cc:%s",vs.get()[0],vs.get()[1],vs.get()[2]);
        }
    }

    private ArrayList<ValueSet> generateValueSet(int maLower,int maUpper,int ddLower,int ddUpper,int ccLower,int ccUpper){
        ArrayList<ValueSet> valueSets = new ArrayList<>();

        for (int i = maLower; i < maUpper+1; i ++) {
            for (int j = ddLower; j < ddUpper+1; j ++) {
                for (int k = ccLower; k < ccUpper+1; k ++) {
                    valueSets.add(new ValueSet(i,j,k));
                }
            }
        }

        return valueSets;
    }

    private ArrayList<Game> generateTasks(ArrayList<ValueSet> valueSets,int size, String ai1,String ai2){
        ArrayList<ValueSet> disposableValueSet = new ArrayList<>(List.copyOf(valueSets));

        if (disposableValueSet.size() % 2 == 1){
            disposableValueSet.add(disposableValueSet.get(0));
            valueSets.add(disposableValueSet.get(0));
        }

        int count = disposableValueSet.size()/2;

        ArrayList<Game> tasks = new ArrayList<>();

        for (int id = 0; id < valueSets.size() / 2; id++) {
            var rand = new Random();
            var first = disposableValueSet.get(rand.nextInt(disposableValueSet.size()));
            disposableValueSet.remove(first);
            var second = disposableValueSet.get(rand.nextInt(disposableValueSet.size()));
            disposableValueSet.remove(second);
            tasks.add(new Game(size, ai1, ai2, id, first, second,count));
        }
        return tasks;
    }
    private ArrayList<ValueSet> simulate(ArrayList<Game> tasks){
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(),24));

        List<CompletableFuture<ValueSet>> futures =
                tasks.stream()
                        .map(t -> CompletableFuture.supplyAsync(t::start, executor)).toList();

        ArrayList<ValueSet> results = new ArrayList<>(futures.stream().map(CompletableFuture::join).toList());
        executor.shutdown();
        return results;
    }

    private class Game {
        private GameState state;
        private IOthelloAI ai1;
        private IOthelloAI ai2;
        private boolean done;
        private int size;
        public int id;
        private ValueSet ai1ValueSet;
        private ValueSet ai2ValueSet;
        private int count;
        public Game(int size, String ai1, String ai2,int id,ValueSet ai1ValueSet, ValueSet ai2ValueSet,int count){
            this.state = new GameState(size,1);
            this.count = count;
            this.size = size;
            this.id = id;
            this.ai1ValueSet = ai1ValueSet;
            this.ai2ValueSet = ai2ValueSet;
            try {
                this.ai1 = (IOthelloAI)Class.forName(ai1).getConstructor().newInstance();
                this.ai2 = (IOthelloAI)Class.forName(ai2).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                System.out.println("AI not found");
                System.exit(1);
            }
        }

        public ValueSet start (){
            while(!done) takeTurn();
            return getWinnerValueSet();
        }

        private void takeTurn(){
            if ( !state.isFinished() ){
                Position place = getPlaceForNextToken();
                if ( state.insertToken(place)){ // Chosen move is legal
                    boolean nextPlayerCannotMove = state.legalMoves().isEmpty();
                    if ( nextPlayerCannotMove ) state.changePlayer();
                }
            }else{
                System.out.println("game " + id + " finished. Total :" + count);
                done = true;
            }
        }

        private Position getPlaceForNextToken(){
            return state.getPlayerInTurn() == 2 ? ai2.decideMove(state, ai2ValueSet) : ai1.decideMove(state, ai1ValueSet);
        }

        private ValueSet getWinnerValueSet(){
            int[] tokens = state.countTokens();
            if ( tokens[0] > tokens[1] ) return ai1ValueSet;
            else if ( tokens[0] < tokens[1] ) return ai2ValueSet;
            else {
                return new ValueSet(
                        (ai1ValueSet.get()[0] + ai2ValueSet.get()[0])/2,
                        (ai1ValueSet.get()[1] + ai2ValueSet.get()[1])/2,
                        (ai1ValueSet.get()[2] + ai2ValueSet.get()[2])/2
                );
            }
        }
    }
    }


