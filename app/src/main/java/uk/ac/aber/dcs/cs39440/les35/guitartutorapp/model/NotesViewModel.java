package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.NotesRepository;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

/**
 * ViewModel for accessing Notes data from the RoomDatabase, through multiple layers of abstraction
 */
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
        repo.insert(note);
    }

    public Note[] getAllNotesAsList() {
        return noteList;
    }

    public Note getNoteByName(String noteName) {
        return repo.getNoteByName(noteName);
    }

    public Note getNoteBefore(int id){
        return repo.getNoteBefore(id);
    }

    public Note getNoteAfter(int id){
        return repo.getNoteAfter(id);
    }

    public Note getNoteById(int id){
        return repo.getNoteById(id);
    }
}
