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
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.InstrumentType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

import static java.lang.Short.valueOf;

public class CsvReader {
    AssetManager am;
    InputStream is;
    String filename;
    BufferedReader reader;
    String line = "";
    Context context;
    List<Note> notes = new ArrayList<>();
    List<Tuning> tunings = new ArrayList<>();

    public CsvReader(String filename, Context context) throws IOException {
        this.filename = filename;
        am = context.getAssets();
        is = am.open(filename);
        this.context = context;
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));


    }

    public void readFile(){
        if (filename.equals(context.getResources().getString(R.string.notes_file_name))) {
            try {
                while ((line = reader.readLine()) != null) {
                    String[] lineSplit = line.split(",");
                    Note note = new Note(lineSplit[0], Float.valueOf(lineSplit[1]));
                    notes.add(note);
                }
            } catch (IOException exception) {
                Log.e("CSV Reader", "Error " + line, exception);
                exception.printStackTrace();
            }
        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Tuning> getTunings() {
        return tunings;
    }
}
