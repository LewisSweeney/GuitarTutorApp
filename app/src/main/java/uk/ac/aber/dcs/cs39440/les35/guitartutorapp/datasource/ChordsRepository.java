package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.app.Application;
import android.os.AsyncTask;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;

public class ChordsRepository {

    // Data access object to allow interaction with the Chords table/entity
    private ChordDAO chordDAO;
    private Chord[] chordsList;


    public ChordsRepository(Application application){
        GuitarRoomDatabase db = GuitarRoomDatabase.getDatabase(application);
        chordDAO = db.getChordsDao();
        chordsList = chordDAO.getAllChords();
    }

    /**
     * Public call for inserting a note into the room database Chords table.
     * Creates a new instance of the corresponding extension of AsyncTask and
     * @param chords The chords that are being added to the table.
     */
    public void insert(Chord[] chords){

        new ChordsRepository.InsertAsyncTask(chordDAO).execute(chords);
    }

    public Chord[] getChordsList() {
        return chordsList;
    }

    public Chord getChordByName(String name){
        Chord chord = chordDAO.getChordByName(name);
        return chord;
    }

    static class InsertAsyncTask extends AsyncTask<Chord, Void, Void> {
        private ChordDAO mAsyncTaskDao;

        InsertAsyncTask (ChordDAO dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Chord... params) {
            System.out.println("inserting at repo");
            // Calls the insert multiple words method as it supports inserting just one item
            mAsyncTaskDao.insertChords(params);
            return null;
        }
    }
}
