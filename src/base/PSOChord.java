package base;

import java.util.Random;


/**
 * Chord Swarm Optimization
 */
public class PSOChord {
    private Chord globalBest;
    private int populationSize = 10000;
    private Chord[] population;
    private int MAX_FITNESS = 5;

    private int MAX_ITER = 1000;
    private int tonality = 0;

    private int[] previousChord;
    private boolean isTonica = false;

    private int numberOfChords = 16;
    private Chord[] chords = new Chord[numberOfChords];

    private double m = 1.0;
    private double c1 = 0.8;
    private double c2 = 0.7;

    private int currentChord;

    public PSOChord(int tonality, int population) {
        this.tonality = tonality;
        this.populationSize = population;
        this.population = new Chord[population];
    }


    /**
     * Interface for chord generation
     * Calls the start, that is make generation
     */
    public void generateChords() {
        for (int i = 0; i < numberOfChords; i++) {
            currentChord = i;

            int[] previousChord = {0, 0, 0};
            if (i != 0) {
                previousChord = chords[i - 1].getChord();
            }
            start(previousChord);
            if (i == numberOfChords - 1) {//If it is the last chord
                start(previousChord, true);
            }

            chords[i] = getGlobalBest();

        }

    }

    public Chord[] getChords() {
        return chords;
    }

    /**
     * Generate chords
     *
     * @param previousChord
     */
    private void start(int[] previousChord) {
        this.previousChord = previousChord;
        generatePopulation();
        globalBest = new Chord(population[0]);

        initialization();

        PSOAlgorithm();
    }

    /**
     * General PSOChord algorithm
     * for each particle i = 1, ..., S do
     * Initialize the particle's position with a uniformly distributed random vector: xi ~ U(blo, bup)
     * Initialize the particle's best known position to its initial position: pi ← xi
     * if f(pi) < f(g) then
     * update the swarm's best known  position: g ← pi
     * Initialize the particle's velocity: vi ~ U(-|bup-blo|, |bup-blo|)
     * while a termination criterion is not met do:
     * for each particle i = 1, ..., S do
     * for each dimension d = 1, ..., n do
     * Pick random numbers: rp, rg ~ U(0,1)
     * Update the particle's velocity: vi,d ← ω vi,d + φp rp (pi,d-xi,d) + φg rg (gd-xi,d)
     * Update the particle's position: xi ← xi + vi
     * if f(xi) < f(pi) then
     * Update the particle's best known position: pi ← xi
     * if f(pi) < f(g) then
     * Update the swarm's best known position: g ← pi
     * <p>
     * wiki: https://en.wikipedia.org/wiki/Particle_swarm_optimization
     * Because I will not write this pseudocode better than this
     * <p>
     * In case global optimum goes out of bounds than the generation starts over again
     */
    private void PSOAlgorithm() {
        int iter = 0;
        while (globalBest.getFitness() != MAX_FITNESS) {
            if (iter >= MAX_ITER) {
                startOver();
                break;
            }
            for (int i = 0; i < populationSize; i++) {
                Random rand = new Random();
                double r1 = rand.nextDouble();
                double r2 = rand.nextDouble();

                double[] velocity = population[i].getVelocityVector();


                double[] newVelocity = new double[3];
                for (int j = 0; j < 3; j++) {
                    newVelocity[j] = m * velocity[j] +
                            c1 * r1 * (population[i].getMyBest()[j] - population[i].getChord()[j]) +
                            c2 * r2 * (globalBest.getChord()[j] - population[i].getChord()[j]);
                }

                population[i].setVelocityVector(newVelocity);

                int[] newChord = new int[3];
                for (int j = 0; j < 3; j++) {
                    newChord[j] = population[i].getChord()[j] + (int) newVelocity[j];
                }

                population[i].setChord(newChord);

                population[i].setFitness(countFitness(population[i]));

                if (population[i].getFitness() > globalBest.getFitness()) {
                    globalBest = new Chord(population[i]);
                }
                if (countFitness(population[i].getMyBest()) < countFitness(population[i].getChord())) {
                    population[i].setMyBest(population[i].getChord());
                }

            }
            check(globalBest);
            iter++;
        }
    }

    /**
     * Initialization of parameters for the algorithm
     */
    private void initialization() {
        for (int i = 0; i < populationSize; i++) {
            population[i].setFitness(countFitness(population[i]));
            double[] velocity = {0, 0, 0};
            population[i].setVelocityVector(velocity);

            if (population[i].getFitness() > globalBest.getFitness()) {
                globalBest = population[i];
            }

            if (countFitness(population[i].getMyBest()) < countFitness(population[i])) {
                population[i].setMyBest(population[i].getChord());
            }
        }
    }

    /**
     * Generation of population in range 48 to 96 for value of note in midi
     */
    private void generatePopulation() {
        for (int i = 0; i < population.length; i++) {
            population[i] = new Chord();
            int[] chord = new int[3];
            Random random = new Random();
            for (int j = 0; j < 3; j++) {
                chord[j] = random.nextInt(84 - 48) + 48;
            }
            population[i].setChord(chord);
        }
    }

    private int countFitness(Chord chord) {
        return countFitness(chord.getChord());
    }

    /**
     * Dynamic fitness function, depends on previous chords
     * If previous chord is equal to 0 than fitness increase
     * If difference between chords less than one than fitness increase
     * If previous chords was different than fitness increase (if not enough chords before, always increase)
     * Different for major and minor, because chord structure is different
     * First chord must be divisible to 12 to be tonica, so if it happens fitness increase
     * And if we don't want tonica chord than check for dominant and subdominant chord, that is (chord first note - 5 )
     * they must be divisible to 12 and ( chord first note - 7 ) to 12
     *
     * @param chord
     * @return
     */
    private int countFitness(int[] chord) {
        int fitness = 0;

        if (previousChord[0] == 0) {
            fitness++;
        }

        if (Math.abs(chord[0] - previousChord[0]) <= 12) {
            fitness++;
        }

        int n = tonality / 2;

        if (currentChord > 4) {
            int firstChordNote = chords[currentChord - 1].getChord()[0];
            int secondChordNote = chords[currentChord - 2].getChord()[0];
            int thirdChordNote = chords[currentChord - 3].getChord()[0];
            int fourthChordNote = chords[currentChord - 4].getChord()[0];
            int fifthChordNote = chords[currentChord - 5].getChord()[0];

            if (!((firstChordNote == secondChordNote) && (thirdChordNote == fourthChordNote)
                    && (fifthChordNote == firstChordNote) && (secondChordNote == thirdChordNote))) {
                fitness++;
            }
        } else {
            fitness++;
        }


        if (tonality % 2 == 0) {//MAJOR
            if (chord[1] == chord[0] + 4) {
                fitness++;
            }
            if (chord[2] == chord[1] + 3) {
                fitness++;
            }
            if ((chord[0] - n) % 12 == 0) {
                fitness++;
            } else if (((chord[0] - n - 5) % 12 == 0) && (!isTonica)) {
                fitness++;
            } else if (((chord[0] - n - 7) % 12 == 0) && (!isTonica)) {
                fitness++;
            }
        } else {//MINOR
            if (chord[1] == chord[0] + 3) {
                fitness++;
            }
            if (chord[2] == chord[1] + 4) {
                fitness++;
            }
            if ((chord[0] - n) % 12 == 0) {
                fitness++;
            } else if (((chord[0] - n - 5) % 12 == 0) && (!isTonica)) {
                fitness++;
            } else if (((chord[0] - n - 7) % 12 == 0) && (!isTonica)) {
                fitness++;
            }
        }

        return fitness;
    }

    /**
     * @return global best
     */
    public Chord getGlobalBest() {
        return globalBest;
    }

    /**
     * Start generation all over again
     */
    private void startOver() {
        start(previousChord);
    }

    /**
     * Checks if particle is in the range
     *
     * @param particle to check
     */
    private void check(Chord particle) {
        int chord[] = particle.getChord();

        for (int i = 0; i < 3; i++) {
            if ((chord[i] > 96) || (chord[i] < 48)) {
                startOver();
                break;
            }
        }
    }

    /**
     * Overload of method start
     * Generates, but sets tonica to true so PSOChord will only generate tonica chords
     */
    public void start(int[] previousChord, boolean b) {
        isTonica = b;
        start(previousChord);
    }
}
