package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;

@Dao
public interface ChordDAO {

    @Insert
    void insertChords(Chord[] chords);

    /**
     * Gets a single note from the room database, called as and when required
     * @param name
     * @return
     */
    @Query("SELECT * FROM chords WHERE name = :name")
    Chord getChordByName(String name);

    @Query("SELECT * FROM chords")
    Chord[] getAllChords();
}
