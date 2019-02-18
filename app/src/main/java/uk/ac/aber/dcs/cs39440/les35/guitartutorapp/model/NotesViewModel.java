package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.NotesRepository;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

public class NotesViewModel extends AndroidViewModel {
    // Repository for DB access
    private NotesRepository repo;
    // List that holds all currently stored words
    private Note[] noteList;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        repo = new NotesRepository(application);
        noteList = repo.getNotesList();
    }

    /**
     * Inserts notes into the DB
     *
     * @param note
     */
    public void insert(Note[] note) {
        System.out.println("inserting at view model");
        repo.insert(note);
    }

    public Note[] getAllNotesAsList() {
        return noteList;
    }

    public Note getNoteByName(String noteName) {
        Note note = repo.getNoteByName(noteName);
        return note;
    }

    public Note getNoteBefore(int id){
        Note note = repo.getNoteBefore(id);
        return note;
    }

    public Note getNoteAfter(int id){
        Note note = repo.getNoteAfter(id);
        return note;
    }
}
