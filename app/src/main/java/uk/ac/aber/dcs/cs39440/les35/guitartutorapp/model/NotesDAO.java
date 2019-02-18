package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

@Dao
public interface NotesDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNotes(Note[] notes);

    /**
     * Gets a single note from the room database, called as and when required
     * @param noteName
     * @return
     */
    @Query("SELECT * FROM notes WHERE noteName = :noteName")
    Note getNoteByName(String noteName);

    @Query("SELECT * FROM notes")
    Note[] getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id-1")
    Note getNoteBefore(int id);

    @Query("SELECT * FROM notes WHERE id = :id+1")
    Note getNoteAfter(int id);

}
