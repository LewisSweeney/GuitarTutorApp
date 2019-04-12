package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.content.Context;
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
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

public class FileReader {
    AssetManager am;
    InputStream is;
    String filename;
    BufferedReader reader;
    String line = "";
    Context context;
    Note[] notes;
    List<Tuning> tunings = new ArrayList<>();
    Chord[] chords;

    public FileReader(Context context) throws IOException {
        am = context.getAssets();
        this.context = context;
        notes = new Note[108];
    }

    public void readNotes() throws IOException {
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
        for(int i = 0; i < numChords; i++){
            chords[i] = tempChords.get(i);
        }

    }

    public void readBadges() throws IOException {
        filename = context.getResources().getString(R.string.badges_file_name);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        
    }

    public Note[] getNotes() {
        return notes;
    }

    public List<Tuning> getTunings() {
        return tunings;
    }

    public Chord[] getChords() {
        return chords;
    }
}
