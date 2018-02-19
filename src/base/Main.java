package base;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;

import java.io.*;
import java.util.Random;

public class Main {
    static int numberOfChords = 16;
    static int numberOfNotes = 32;

    static Chord chords[] = new Chord[numberOfChords];
    static Note notes[] = new Note[numberOfNotes];

    static int tonality;

    static int PSOChordPopulation = 1100;
    static int PSONotePopulation = 30;

    /**
     * First initialize all variables
     * Generate tonality for song
     * Generate notes for song
     * Generate string for song
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        initialize();
        generateTonality();

        for (int i = 997; i < 1000; i++) {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream("time_chord_note.csv", true));
            long start = System.nanoTime();

            generateChords(PSOChordPopulation);

            long end = System.nanoTime();
            long delta = end - start;
            printWriter.print(i + "," + delta);

            start = System.nanoTime();
            generateNotes(PSONotePopulation);
            end = System.nanoTime();
            delta = end - start;
            printWriter.println("," + delta);
            printWriter.close();
        }





        String musicString = createMIDIString(chords, notes);
        System.out.println("Sorry, native Java MIDI sounds awful and also mess some notes. Please use external emulator\n\n\n" + musicString);

        createMidiFile(musicString, 120);
        playMusic(musicString);
    }

    /**
     * Just an initalization of variables
     */
    private static void initialize() {
        chords = new Chord[numberOfChords];
        notes = new Note[numberOfNotes];
    }

    /**
     * Calls PSO for notes
     * @param population of PSO notes
     */
    private static void generateNotes(int population) {
        PSONotes psoNotes = new PSONotes(population);
        boolean flag = true;
        int chordCounter = 0;
        for (int i = 0; i < numberOfNotes; i++) {
            int prevNote = -1;
            if (i != 0) {
                prevNote = notes[i - 1].getNoteMIDI();
            }
            psoNotes.start(chords[chordCounter], flag, tonality, prevNote);

            notes[i] = psoNotes.getGlobalBest();

            if ((i != numberOfNotes - 1) && (i != numberOfNotes) && (!flag)) {
                while (notes[i].getNoteMIDI() < chords[chordCounter + 1].getChord()[0]) {
                    psoNotes.start(chords[chordCounter], flag, tonality, prevNote);
                    notes[i] = psoNotes.getGlobalBest();
                }
            }


            if (i % 2 != 0) {
                chordCounter++;
            }
            flag = !flag;
        }
    }


    /**
     * Calls PSO for chords
     * @param population of chord PSO
     * @throws FileNotFoundException
     */
    private static void generateChords(int population) throws FileNotFoundException {
        PSOChord pso = new PSOChord(tonality, population);
        pso.generateChords();
        chords = pso.getChords();
    }

    /**
     * Generates right music string for later use in file
     * @param chords in our song
     * @param notes in our song
     * @return right midi music string
     */
    private static String createMIDIString(Chord[] chords, Note[] notes) {
        String result = "";
        int noteCounter = 0;
        for (int i = 0; i < numberOfChords; i++) {
            for (int j = 0; j < chords[0].getChord().length; j++) {
                result += chords[i].getChord()[j];
                result += "q+";
            }

            result += notes[noteCounter].getNoteMIDI() + "i ";
            noteCounter++;
            result += notes[noteCounter].getNoteMIDI() + "i ";
            noteCounter++;
        }

        return result;
    }

    /**
     * For all chord play it with the two notes
     */
    private static void playMusic(String musicLine) {
        Player player = new Player();

        player.play(musicLine);
    }

    /**
     * Generates tonality of the song randomly
     */
    private static void generateTonality() {
        Random random = new Random();
        tonality = random.nextInt(24);
    }

    /**
     * Writes *.mid file
     * @param musicString to write
     * @param tempo with what temp song must be played
     */
    private static void createMidiFile(String musicString, int tempo) // throws IOException, InvalidMidiDataException
    {
        String midiFileNameEnd = ".mid";
        Pattern pattern = new Pattern(musicString).setVoice(0).setInstrument("Piano").setTempo(tempo);
        try {
            File file = new File("EkaterinaLevchenko" + midiFileNameEnd);
            MidiFileManager.savePatternToMidi(pattern, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
