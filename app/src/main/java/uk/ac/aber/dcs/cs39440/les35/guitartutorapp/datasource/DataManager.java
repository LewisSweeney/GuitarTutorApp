package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.InstrumentType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.StatType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

/**
 * DataManager is a class that manages csv, txt and SharedPreference reading.
 * Allows for a central class for retrieval and writing of important app data
 */
public class DataManager {

    // SharedPreferences you used to store scores for the note recognition and palyback games
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;


    private AssetManager am;
    private InputStream is;
    private String filename;
    private BufferedReader reader;
    private String line = "";
    private Context context;
    private Note[] notes;
    private List<Tuning> tunings = new ArrayList<>();
    private Chord[] chords;
    private List<String> badgeStrings = new ArrayList<>();
    private int[][] stats = new int[2][2];

    // Keys for accessing the correct SharedPreferences
    private final String REP_SCORE_KEY = "repscore";
    private final String REP_TOTAL_KEY = "reptotal";
    private final String REC_SCORE_KEY = "recscore";
    private final String REC_TOTAL_KEY = "rectotal";

    /**
     * Constructor takes context as a parameter to allow for the DataManager to access things such as
     * the AssetManager class that requires an activity context
     * @param context
     */
    public DataManager(Context context) {
        am = context.getAssets();
        this.context = context;
        notes = new Note[108];

        prefs = context.getSharedPreferences(REC_SCORE_KEY, Context.MODE_PRIVATE);

    }

    /**
     * Reads musical notes from the notes.csv file. Used only once on initial app start up to
     * populate the Room Database
     * @throws IOException
     */
    void readNotes() throws IOException {
        filename = context.getResources().getString(R.string.notes_file_name);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        try {
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(",");
                Note note = new Note(lineSplit[0], Float.valueOf(lineSplit[1]));
                notes[i] = note;
                i++;
            }
        } catch (IOException exception) {
            Log.e("CSV Reader", "Error " + line, exception);
            exception.printStackTrace();
        }
    }

    /**
     * Reads all tunings from the tunings.csv file for retrieval by the Tuning activity
     * Determines the instrument type from the file. Has ability to read ukulele tunings, which
     * are currently not included in the app
     * Each tuning is made into an object and stored in an Arraylist
     * @param notesFromDB
     * @throws IOException
     */
    public void readTunings(Note[] notesFromDB) throws IOException {
        filename = context.getResources().getString(R.string.tuning_file_name);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        try {
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(",");

                // Get tuning name
                String tuningName = lineSplit[0];

                // Get instrument type for tuning
                String instrumentTypeAsString = lineSplit[1];
                InstrumentType instrumentType = InstrumentType.GUITAR;
                switch (instrumentTypeAsString) {
                    case "GUITAR":
                        instrumentType = InstrumentType.GUITAR;
                        break;
                    case "BASS":
                        instrumentType = InstrumentType.BASS;
                        break;
                    case "UKULELE":
                        instrumentType = InstrumentType.UKULELE;
                        break;
                    default:
                        break;
                }

                // Get notes for tuning

                List<Note> notesForTuning = new ArrayList<>();
                for (int j = 2; j < lineSplit.length; j++) {
                    String currentNoteName = lineSplit[j];
                    if (!currentNoteName.equals("N/A")) {
                        for (Note aNotesFromDB : notesFromDB) {
                            if (aNotesFromDB.getNoteName().equals(currentNoteName)) {
                                notesForTuning.add(aNotesFromDB);
                            }
                        }
                    }
                }

                Tuning tuning = new Tuning(tuningName, instrumentType, notesForTuning);
                tunings.add(tuning);


            }
        } catch (IOException exception) {
            Log.e("CSV Reader", "Error " + line, exception);
            exception.printStackTrace();
        }
    }

    /**
     * Reads chords in from the chords.csv and stores them for reading by the Chords activity
     * Each chord is stored as its own Chord object and stored in an Arraylist
     * @throws IOException
     */
    public void readChords() throws IOException {
        filename = context.getResources().getString(R.string.chords_file_name);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        List<Chord> tempChords = new ArrayList<>();
        int numChords = 0;

        try {
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(",");

                String chordName = lineSplit[0];
                String chordNotes = lineSplit[1];
                int chordStartingFret = Integer.parseInt(lineSplit[2].trim());

                Chord chord = new Chord(chordName, chordNotes, chordStartingFret);
                tempChords.add(chord);
                numChords++;

            }
        } catch (IOException exception) {
            Log.e("CSV Reader", "Error " + line, exception);
            exception.printStackTrace();
        }

        chords = new Chord[numChords];
        for (int i = 0; i < numChords; i++) {
            chords[i] = tempChords.get(i);
        }

    }

    /**
     * Simple read of the badges/achievements, creating an object for each of them
     * @throws IOException
     */
    public void readBadges() throws IOException {
        filename = context.getResources().getString(R.string.badges_file_name);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        try {
            while ((line = reader.readLine()) != null) {
                badgeStrings.add(line);
            }
        } catch (IOException exception) {
            Log.e("CSV Reader", "Error " + line, exception);
            exception.printStackTrace();
        }
    }

    /**
     * Reads the game stats, using the static keys for access
     * @throws IOException
     */
    public void readStats() throws IOException {

        int defaultValue = context.getResources().getInteger(R.integer.saved_stats_default_key);
        stats[0][0] = prefs.getInt(REP_SCORE_KEY, defaultValue);
        stats[0][1] = prefs.getInt(REP_TOTAL_KEY, defaultValue);
        stats[1][0] = prefs.getInt(REC_SCORE_KEY, defaultValue);
        stats[1][1] = prefs.getInt(REC_TOTAL_KEY, defaultValue);

    }

    /**
     * Takes a stat type and integer "stat" increment, which is then fed into the "writeStatsToPrefs" method*
     * @param statType StatType required for determining which stat type is being written to
     * @param stat Integer to increment the specified stat by
     * @throws IOException
     */
    public void writeStats(StatType statType, int stat) throws IOException {
        int defaultValue = context.getResources().getInteger(R.integer.saved_stats_default_key);
        prefEditor = prefs.edit();
        String key;
        int currentScore;
        int newScore;
        switch (statType) {
            case RECTOT:
                key = REC_TOTAL_KEY;
                currentScore = prefs.getInt(key, defaultValue);
                newScore = currentScore + stat;
                writeStatsToPrefs(key, newScore);
                break;
            case REPTOT:
                key = REP_TOTAL_KEY;
                currentScore = prefs.getInt(key, defaultValue);
                newScore = currentScore + stat;
                writeStatsToPrefs(key, newScore);
                break;
            case RECSCORE:
                key = REC_SCORE_KEY;
                currentScore = prefs.getInt(key, defaultValue);
                newScore = currentScore + stat;
                writeStatsToPrefs(key, newScore);
                break;
            case REPSCORE:
                key = REP_SCORE_KEY;
                currentScore = prefs.getInt(key, defaultValue);
                newScore = currentScore + stat;
                writeStatsToPrefs(key, newScore);
                break;
        }
    }

    /**
     * Clears the stats for the note games, called by SettingsActivity
     * @throws IOException
     */
    public void clearStats() throws IOException {
        writeStatsToPrefs(REP_SCORE_KEY, 0);
        writeStatsToPrefs(REP_TOTAL_KEY, 0);
        writeStatsToPrefs(REC_SCORE_KEY, 0);
        writeStatsToPrefs(REC_TOTAL_KEY, 0);
    }

    /**
     *
     * @param key The key for which stat should be written to
     * @param stat The stat to be written, no increment, final value
     * @throws IOException
     */
    private void writeStatsToPrefs(String key, int stat) throws IOException {
        prefEditor = prefs.edit();
        prefEditor.putInt(key, stat);
        prefEditor.apply();
    }

    /**
     * Notes read by readNotes()
     * @return
     */
    public Note[] getNotes() {
        return notes;
    }

    /**
     * Tunings read by readTunings()
     * @return
     */
    public List<Tuning> getTunings() {
        return tunings;
    }

    /**
     * Chords read by readChords()
     * @return
     */
    public Chord[] getChords() {
        return chords;
    }

    /**
     * Badges read by readBadges()
     * @return
     */
    public List<String> getBadges() {
        return badgeStrings;
    }

    /**
     * Stats read by readStats()
     * @return
     */
    public int[][] getStats() {
        return stats;
    }
}
