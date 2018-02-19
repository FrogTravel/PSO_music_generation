package base;

/**
 * Created by ekaterina on 10/18/17.
 */
public class Chord {
    private int[] chord = new int[3];
    private double[] velocityVector = new double[3];
    private int[] myBest = new int[3];

    private int fitness;

    public Chord(){

    }

    public Chord(Chord chord) {
        this.chord = chord.getChord();
        velocityVector = chord.getVelocityVector();
        myBest = chord.getMyBest();
    }

    /**
     * Getters and Setters
     */

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int[] getChord() {
        return chord;
    }

    public void setChord(int[] chord) {
        this.chord = chord;
    }

    public double[] getVelocityVector() {
        return velocityVector;
    }

    public void setVelocityVector(double[] velocityVector) {
        this.velocityVector = velocityVector;
    }

    public int[] getMyBest() {
        return myBest;
    }

    public void setMyBest(int[] myBest) {
        this.myBest = myBest;
    }

    @Override
    public String toString(){
        return chord[0] + " " + chord[1] + " " + chord[2];
    }
}
