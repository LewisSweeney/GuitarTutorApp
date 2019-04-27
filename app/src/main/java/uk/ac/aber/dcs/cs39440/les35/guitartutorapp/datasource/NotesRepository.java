package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.app.Application;
import android.os.AsyncTask;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

/**
 * Repository that acts as a middleman between the NotesViewModel available to the rest of the
 * code, and the NotesDAO that interfaces directly with the RoomDatabase
 */
public class NotesRepository {
    // Data access object to allow interaction with the Notes table/entity
    private NotesDAO noteDAO;
    private Note[] notesList;

    public NotesRepository(Application application){
        GuitarRoomDatabase db = GuitarRoomDatabase.getDatabase(application);
        noteDAO = db.getNotesDao();
        notesList = noteDAO.getAllNotes();
    }

    /**
     * Public call for inserting a note into the room database Notes table.
     * Creates a new instance of the corresponding extension of AsyncTask and
     * @param notes The notes that are being added to the table.
     */
    public void insert(Note[] notes){

        new InsertAsyncTask(noteDAO).execute(notes);
    }

    public Note[] getNotesList() {
        return notesList;
    }

    public Note getNoteByName(String noteName){
        return noteDAO.getNoteByName(noteName);
    }

    public Note getNoteBefore(int id){
        return noteDAO.getNoteBefore(id);
    }

    public Note getNoteAfter(int id){
        return noteDAO.getNoteAfter(id);
    }

    public Note getNoteById(int id){
        return noteDAO.getNoteById(id);
    }
    static class InsertAsyncTask extends AsyncTask<Note, Void, Void> {
        private NotesDAO mAsyncTaskDao;

        InsertAsyncTask (NotesDAO dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Note... params) {
            System.out.println("inserting at repo");
            // Calls the insert multiple notes method as it supports inserting just one item
            mAsyncTaskDao.insertNotes(params);
            return null;
        }
    }
}
