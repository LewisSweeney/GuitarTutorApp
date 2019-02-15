package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

@Dao
public interface NotesDAO {

    @Insert
    void insertNotes(List<Note> notes);

    /**
     * Gets a single note from the room database, called as and when required
     * @param noteName
     * @return
     */
    @Query("SELECT * FROM notes WHERE noteName = :noteName")
    Note getNoteByName(String noteName);

    @Query("SELECT * FROM notes")
    List<Note> getAllNotes();
}
