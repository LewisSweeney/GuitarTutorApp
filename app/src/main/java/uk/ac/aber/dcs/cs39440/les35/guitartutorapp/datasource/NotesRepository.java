package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.app.Application;
import android.os.AsyncTask;

import java.util.Arrays;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.MyApplication;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

public class NotesRepository {
    // Data access object to allow interaction with the Notes table/entity
    private NotesDAO noteDAO;
    private List<Note> notesList;

    public NotesRepository(Application application){
        GuitarRoomDatabase db = GuitarRoomDatabase.getDatabase(application);
        notesList = noteDAO.getAllNotes();
    }

    /**
     * Public call for inserting a note into the room database Notes table.
     * Creates a new instance of the corresponding extension of AsyncTask and
     * @param notes The notes that are being added to the table.
     */
    public void insert(Note notes){
        new InsertAsyncTask(noteDAO).execute(notes);
    }

    public List<Note> getNotesList() {
        return notesList;
    }

    static class InsertAsyncTask extends AsyncTask<Note, Void, Void> {
        private NotesDAO mAsyncTaskDao;

        InsertAsyncTask (NotesDAO dao){
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(Note... params) {
            // Calls the insert multiple words method as it supports inserting just one item
            mAsyncTaskDao.insertNotes(Arrays.asList(params));
            return null;
        }
    }
}
