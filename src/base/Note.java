package base;

/**
 * Created by ekaterina on 11/7/17.
 */
public class Note {
    private int noteMIDI = 0;
    private int fitness;

    private Note myBest;

    private double velocity;

    public Note() {
    }

    public Note(Note note) {
        noteMIDI = note.getNoteMIDI();
        fitness = note.getFitness();
        myBest = note.getMyBest();
        velocity = note.getVelocity();
    }

    /**
     * Getters and Setters
     */

    public int getNoteMIDI() {
        return noteMIDI;
    }

    public void setNoteMIDI(int noteMIDI) {
        this.noteMIDI = noteMIDI;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public Note getMyBest() {
        return myBest;
    }

    public void setMyBest(Note myBest) {
        this.myBest = myBest;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public String toString(){
        return String.valueOf(noteMIDI);
    }
}
