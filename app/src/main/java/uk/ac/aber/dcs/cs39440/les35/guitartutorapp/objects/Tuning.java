package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold the tunings available for instruments on the app
 */

//@Entity(tableName= "tunings")
public class Tuning {
    private int id;
    private String tuningName;
    private InstrumentType instrument;
    private List<Note> notes = new ArrayList<>();

    public Tuning(String tuningName, InstrumentType instrument, List<Note> notes) {
        this.tuningName = tuningName;
        this.instrument = instrument;
        this.notes.addAll(notes);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public InstrumentType getInstrument(){
        return instrument;
    }

    public String getTuningName() {
        return tuningName;
    }

    public void setTuningName(String tuningName) {
        this.tuningName = tuningName;
    }
}
