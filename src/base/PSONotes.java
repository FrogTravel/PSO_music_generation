package base;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by ekaterina on 11/7/17.
 */
public class PSONotes {
    private Note globalBest;
    private int populationSize;
    private Note[] notes;
    private Chord chord;
    private boolean isFirst;
    private static int MAX_FITNESS = 2;
    private static int MAX_ITER = 1000;
    private int tonality;
    private Set<Integer> possibleNotes = new HashSet<>();
    private int previousNote;

    private double m = 1.0;
    private double c1 = 1.7;
    private double c2 = 2.6;

    public PSONotes(int population) {
        this.populationSize = population;
        notes = new Note[populationSize];
    }

    /**
     * Generate chords
     */
    public void start(Chord chord, boolean isFirst, int tonality, int previousNote) {
        this.tonality = tonality;
        this.chord = chord;
        this.isFirst = isFirst;
        this.previousNote = previousNote;
        generatePopulation();
        globalBest = notes[0];
        generateTonalityNotes();

        initialization();

        PSOAlgorithm();
    }

    /**
     * General PSOChord algorithm
     *  for each particle i = 1, ..., S do
     *      Initialize the particle's position with a uniformly distributed random vector: xi ~ U(blo, bup)
     *      Initialize the particle's best known position to its initial position: pi ← xi
     *      if f(pi) < f(g) then
     *          update the swarm's best known  position: g ← pi
     *      Initialize the particle's velocity: vi ~ U(-|bup-blo|, |bup-blo|)
     *  while a termination criterion is not met do:
     *      for each particle i = 1, ..., S do
     *          for each dimension d = 1, ..., n do
     *              Pick random numbers: rp, rg ~ U(0,1)
     *              Update the particle's velocity: vi,d ← ω vi,d + φp rp (pi,d-xi,d) + φg rg (gd-xi,d)
     *          Update the particle's position: xi ← xi + vi
     *          if f(xi) < f(pi) then
     *              Update the particle's best known position: pi ← xi
     *              if f(pi) < f(g) then
     *                  Update the swarm's best known position: g ← pi
     *
     * wiki: https://en.wikipedia.org/wiki/Particle_swarm_optimization
     * Because I will not write this pseudocode better than this
     *
     * In case global optimum goes out of bounds than the generation starts over again
     */
    private void PSOAlgorithm(){
        int iter = 0;
        while (globalBest.getFitness() != MAX_FITNESS) {
            if (iter >= MAX_ITER) {
                start(chord, isFirst, tonality, previousNote);
                break;
            }
            for (int i = 0; i < populationSize; i++) {
                Random rand = new Random();
                double r1 = rand.nextDouble();
                double r2 = rand.nextDouble();

                double velocity = notes[i].getVelocity();


                double newVelocity = m * velocity + c1 * r1 * (notes[i].getMyBest().getNoteMIDI() - notes[i].getNoteMIDI()) +
                        c2 * r2 * (globalBest.getNoteMIDI() - notes[i].getNoteMIDI());

                notes[i].setVelocity(newVelocity);

                int newNote = notes[i].getNoteMIDI() + (int) newVelocity;

                notes[i].setNoteMIDI(newNote);
                notes[i].setFitness(countFitness(notes[i]));

                if (notes[i].getFitness() > globalBest.getFitness()) {
                    globalBest = notes[i];
                }
                if (notes[i].getMyBest().getFitness() < notes[i].getFitness()) {
                    notes[i].setMyBest(notes[i]);
                }

            }
            iter++;

            check(globalBest);
        }
    }

    /**
     * Initialization for PSOChord parameters
     */
    private void initialization(){
        for (int i = 0; i < populationSize; i++) {
            notes[i].setFitness(countFitness(notes[i]));
            notes[i].setVelocity(0);

            if (notes[i].getFitness() > globalBest.getFitness()) {
                globalBest = notes[i];
            }

            if (countFitness(notes[i].getMyBest()) < countFitness(notes[i])) {
                notes[i].setMyBest(notes[i]);
            }
        }

    }

    /**
     * Check if note is in the bounds
     * @param globalBest
     */
    private void check(Note globalBest) {
        if ((globalBest.getNoteMIDI() > 96) || (globalBest.getNoteMIDI() < 48)) {
            start(chord, isFirst, tonality, previousNote);
        }
    }

    /**
     * @return best note that was ever found
     */
    public Note getGlobalBest() {
        return globalBest;
    }

    /**
     * Generates new population as random integers in given range
     */
    private void generatePopulation() {
        Random rand = new Random();
        for (int i = 0; i < populationSize; i++) {
            notes[i] = new Note();
            notes[i].setNoteMIDI(rand.nextInt(96 - 48) + 48);
            notes[i].setMyBest(notes[i]);
        }
    }

    /**
     * Dynamic fitness function, because result depends on the previous Note
     * If there was not any previous notes before than fitness increase else it increase
     * only if difference is less than 12
     * If it is first note for the chord than we check if this note is any of current chord name
     * If this is second note than if this note in possible notes of this tonality fitness increase, also this note must be
     * bigger than ast note of chord
     * @param note for what we want to count fitness
     * @return fitness from several criteria
     */
    private int countFitness(Note note) {
        int fitness = 0;
        if (previousNote == -1) {
            fitness++;
        } else if (Math.abs(note.getNoteMIDI() - previousNote) <= 12) {
            fitness++;
        }

        if (isFirst) {
            for (int N = 1; N <= 3; N++) {
                if ((note.getNoteMIDI() == chord.getChord()[0] + 12 * N) ||
                        (note.getNoteMIDI() == chord.getChord()[1] + 12 * N) ||
                        (note.getNoteMIDI() == chord.getChord()[2] + 12 * N)) {
                    fitness++;
                }
            }

        } else {
            if ((possibleNotes.contains(note.getNoteMIDI())) && (note.getNoteMIDI() > chord.getChord()[2])) {
                fitness++;
            }
        }

        return fitness;
    }

    /**
     * Okay, in requirments you only asked readable code, not beautiful. This is works, but looks terrible.
     * It is already 3 am so I will just leave it like this. Sorry for it ^^
     * This giant shit generates all possible notes for the current tonality.
     * It just iterates in 'while' while not out of any bound. Because there are different rules for major and
     * minor tonalities I made if statement.
     */
    private void generateTonalityNotes() {
        int n = tonality / 2;
        int basicNote = 48 + n;
        if (tonality % 2 == 0) {//MAJOR
            while (basicNote > 48) {
                basicNote -= 1;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 1;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);
            }

            basicNote = 48 + n;

            while (basicNote < 96) {
                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 1;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 1;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
            }
        } else {//MINOR

            while (basicNote > 48) {
                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 1;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 1;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);

                basicNote -= 2;

                if (basicNote < 48)
                    break;

                possibleNotes.add(basicNote);
            }

            basicNote = 48 + n;
            while (basicNote < 96) {
                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 1;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 1;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
                basicNote += 2;
                if (basicNote > 96)
                    break;

                possibleNotes.add(basicNote);
            }
        }


    }
}
