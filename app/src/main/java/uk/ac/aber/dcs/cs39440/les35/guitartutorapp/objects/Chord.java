package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Chord objects stored in the room database, storing information about what notes need to be fretted,
 * at what fret the fretting starts and the chord's name
 */
@Entity(tableName = "chords")
public class Chord {
    @PrimaryKey(autoGenerate = true)
    Integer id;
    String name;
    String notes;
    Integer startFret;

    public Chord(String name, String notes, Integer startFret) {
        this.name = name;
        this.notes = notes;
        this.startFret = startFret;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getStartFret() {
        return startFret;
    }

    public void setStartFret(Integer startFret) {
        this.startFret = startFret;
    }
}
