package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

import android.arch.persistence.room.Entity;

/**
 * Class to hold the tunings available for instruments on the app
 */

//@Entity(tableName= "tunings")
public class Tuning {
    private int id;
    private String tuningName;
    private InstrumentType instrument;
    private Note[] notes;

    public Tuning(String tuningName, InstrumentType instrument, Note[] notes){

    }
}
