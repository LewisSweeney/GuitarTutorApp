package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.TuningDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

public class TuningRepository {
    // Data access object to allow interaction with the Notes table/entity
    private TuningDAO tuningDAO;
    private List<Tuning> tuningsAsList;

    public TuningRepository(Application application){
        //GuitarRoomDatabase db = GuitarRoomDatabase.getDatabase(application);
    }

    /**
     * Public call for inserting a note into the room database Notes table.
     * Creates a new instance of the corresponding extension of AsyncTask and
     * @param tunings The tunings that are being added to the table.
     */
    public void insert(Tuning[] tunings){
        new TuningRepository.InsertAsyncTask(tuningDAO).execute(tunings);
    }

    private static class InsertAsyncTask extends AsyncTask<Tuning, Void, Void> {
        private TuningDAO mAsyncTaskDao;

        InsertAsyncTask (TuningDAO dao){
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(Tuning... params) {
            // Calls the insert multiple words method as it supports inserting just one item
           // mAsyncTaskDao.insertTunings(params);
            return null;
        }
    }
}
