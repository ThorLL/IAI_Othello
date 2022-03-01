public class ValueSet{
    private final int maMulti;
    private final int ddMulti;
    private final int cMulti;

    public ValueSet(int maMulti, int ddMulti, int cMulti) {
        this.maMulti = maMulti;
        this.ddMulti = ddMulti;
        this.cMulti = cMulti;
    }
    public int[] get(){
        return new int[]{maMulti,ddMulti,cMulti};
    }
}